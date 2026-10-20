# Authentication trên Management backend

Nếu cần biết bắt đầu triển khai từ đâu và làm theo thứ tự nào, xem [Hướng dẫn làm authentication backend từng bước](30-backend-authentication-implementation-guide.md).

Tài liệu mô tả Spring backend trong `pda-management`, nơi cấp và xác thực token cho PDA. Không mô tả một luồng đăng nhập riêng của frontend quản trị. Luồng Android nằm tại [Authentication trên PDA](28-pda-authentication.md).

Thư viện xác thực chỉ gồm `spring-boot-starter-security` và JJWT **0.11.5**: `jjwt-api` (compile), `jjwt-impl` và `jjwt-jackson` (runtime). Không dùng Spring OAuth2 Resource Server hoặc các API JWT của OAuth2. Các dependency được khai báo trong `pda-management/pom.xml`.

## 1. Thành phần và trách nhiệm

Các đường dẫn Java bên dưới nằm trong `pda-management/src/main/java/com/company/pda/`.

| Thành phần | Trách nhiệm |
| --- | --- |
| `presentation/rest/auth/AuthController.java` | Nhận `/auth/login`, `/auth/refresh`, `/auth/register` |
| `application/auth/service/AuthenticationService.java` | Kiểm tra tài khoản, cấp token, xoay refresh token và gia hạn |
| `application/auth/service/SessionPolicy.java` | Dùng chung cấu hình thời gian |
| `infrastructure/security/JwtTokenProvider.java` | Ký và kiểm tra access/refresh JWT bằng JJWT |
| `infrastructure/security/SecurityConfig.java` | Cấu hình khóa HS256, endpoint và filter chain stateless |
| `infrastructure/security/JwtAuthenticationFilter.java` | Xác thực Bearer và chuyển roles thành Spring authorities |
| `infrastructure/security/SessionValidator.java` | Kiểm tra hạn access token và trạng thái phiên |
| `infrastructure/security/JsonAuthenticationEntryPoint.java` | Trả lỗi xác thực từ filter dưới dạng JSON |
| `domain/auth/repository/AuthRepository.java` | Cổng truy cập user, roles và refresh session |

SQL liên quan nằm trong `pda-management/src/main/resources/mapper/UserMapper.xml`.

## 2. Chính sách thời gian

`SessionPolicy` đọc cấu hình từ `application.yml`:

| Biến môi trường | Property | Mặc định |
| --- | --- | --- |
| `ACCESS_TOKEN_SECONDS` | `app.auth.access-seconds` | 900 giây / 15 phút |
| `SESSION_SECONDS` | `app.auth.session-seconds` | 7200 giây / 2 giờ |
| `SESSION_WARNING_SECONDS` | `app.auth.warning-seconds` | 300 giây / 5 phút |

Các giá trị phải dương, session phải dài hơn access token và warning phải ngắn hơn session. Cấu hình không hợp lệ làm khởi tạo `SessionPolicy` thất bại.

JWT dùng HS256. `app.jwt-secret` phải là Base64 của khóa ít nhất 32 bytes; `app.issuer` được dùng khi cấp và kiểm tra token.

## 3. Đăng nhập

```http
POST /auth/login
Content-Type: application/json
PDA_TYPE: ANDROID

{"username":"user","password":"password"}
```

`AuthController` validate request rồi gọi `AuthenticationService.login()` trong transaction:

1. Tìm user theo username.
2. So khớp mật khẩu qua `PasswordHasher`. Nếu user không tồn tại vẫn so khớp với dummy hash.
3. Sai mật khẩu, không có user hoặc user không active: từ chối bằng `InvalidCredentialsException`.
4. Ghi audit `LOGIN`.
5. Tạo UUID cho family của phiên và đặt hạn phiên bằng thời điểm hiện tại cộng `sessionSeconds`.
6. `issue()` tạo access token, refresh token và lưu bản ghi refresh token.

Hạn access token là giá trị nhỏ hơn giữa `now + accessSeconds` và hạn phiên.

## 4. Token và response

| Token | Claims chính |
| --- | --- |
| Access | `iss`, `sub` là user ID, `aud=retail-api`, `iat`, `exp`, `type=access`, `sessionId` là family ID, `storeId`, `roles` |
| Refresh | `iss`, `sub`, `aud=retail-refresh`, `iat`, `exp`, `type=refresh`, `jti` là ID bản ghi refresh |

Database lưu hash của refresh token qua `SecretHash.hash()`, không lưu chuỗi token gốc. Bản ghi có `id`, `family_id`, `user_id`, `token_hash`, `expires_at`, `consumed_at`, `revoked`.

Login/refresh luôn trả `accessToken`, `refreshToken`, `expiresIn` (giây). Với header `PDA_TYPE: ANDROID`, response bổ sung:

| Trường | Ý nghĩa |
| --- | --- |
| `serverTime` | Thời điểm cấp token, epoch milliseconds |
| `accessExpiresAt` | Hạn access token, epoch milliseconds |
| `sessionExpiresAt` | Hạn phiên / refresh token, epoch milliseconds |
| `warningSeconds` | Khoảng thời gian cảnh báo trước hạn phiên |

`PDA_TYPE` chỉ chọn metadata response trong luồng này; không phải bằng chứng xác thực hoặc quyền bỏ qua JWT.

## 5. Xác thực API nghiệp vụ

Client gửi `Authorization: Bearer <accessToken>`.

1. `JwtAuthenticationFilter` kế thừa `OncePerRequestFilter`, đọc Bearer token từ header và gọi `JwtTokenProvider.verifyAccess()`; filter được đăng ký trước `UsernamePasswordAuthenticationFilter`.
2. JJWT kiểm tra chữ ký, issuer và thời gian; provider yêu cầu thuật toán HS256, claim `exp`, `sub`, `type=access` và audience `retail-api`. Audience chấp nhận cả chuỗi và mảng để đọc token cũ.
3. `SessionValidator` kiểm tra access token chưa hết hạn. Với token có `sessionId`, truy vấn `activeSession(familyId, userId)`.
4. Phiên active cần có refresh token trong family chưa bị revoke, chưa consume, chưa hết hạn và user vẫn active.
5. Claim `roles` được chuyển thành authorities có tiền tố `ROLE_`, rồi lưu `UsernamePasswordAuthenticationToken` với principal là JJWT `Claims` vào `SecurityContext` để kiểm tra quyền. `SecurityCurrentActor` đọc user/cửa hàng từ claims và kiểm tra lại dữ liệu tài khoản.

Backend dùng `SessionCreationPolicy.STATELESS`: không tạo HTTP session để lưu trạng thái đăng nhập. Tuy nhiên trạng thái family vẫn được kiểm tra trong database đối với access token mới.

Token cũ không có `sessionId` được bỏ qua bước kiểm tra family trong `SessionValidator`; vẫn phải qua các kiểm tra JWT còn lại.

## 6. Refresh token và chống dùng lại

```http
POST /auth/refresh
Content-Type: application/json
PDA_TYPE: ANDROID

{"refreshToken":"<current-refresh-token>"}
```

`AuthenticationService.refresh()` chạy trong transaction với `noRollbackFor = DomainException.class` để thao tác revoke không bị rollback khi trả lỗi nghiệp vụ.

1. `JwtTokenProvider.verifyRefresh()` kiểm tra chữ ký, issuer, hạn, `type=refresh`, audience và lấy `jti`.
2. Đọc bản ghi refresh theo ID bằng `SELECT ... FOR UPDATE`.
3. So sánh hash token nhận được với hash đã lưu bằng `MessageDigest.isEqual`.
4. Nếu bản ghi đã revoke, đã consume hoặc hết hạn: revoke cả family và trả lỗi tương ứng.
5. Kiểm tra user còn tồn tại và active; nếu không, revoke family.
6. Đánh dấu token cũ đã dùng bằng UPDATE có điều kiện `consumed_at IS NULL AND revoked=false`. Kết quả khác 1: revoke family và từ chối.
7. Ghi audit `TOKEN_REFRESH`.
8. `issue()` cấp cặp token mới trong cùng family, lưu hash refresh token mới và trả response.

Refresh thông thường giữ nguyên `expires_at` của phiên. Ví dụ đăng nhập lúc 08:00, phiên hết lúc 10:00; tự refresh lúc 08:15 vẫn giữ hạn phiên 10:00.

Refresh token cũ chỉ được dùng một lần. Nếu token đã consume còn vượt qua kiểm tra JWT và được gửi lại, backend revoke cả family; access token có `sessionId` thuộc family đó cũng bị từ chối trong các request tiếp theo. Token hết hạn ngay tại bước verify sẽ bị từ chối trước khi đọc database.

## 7. Gia hạn phiên có chủ đích

```json
{
  "refreshToken": "<current-refresh-token>",
  "extendSession": true
}
```

Vẫn gọi `/auth/refresh` và đi qua toàn bộ kiểm tra refresh. Điểm khác là hạn phiên mới bằng `now + sessionSeconds`, thay vì giữ hạn cũ.

Ví dụ phiên cũ hết 10:00, người dùng gia hạn thành công lúc 09:56 thì phiên mới hết khoảng 11:56 với cấu hình 2 giờ. Refresh token đã hết hạn hoặc bị thu hồi không thể dùng để gia hạn; người dùng phải đăng nhập lại.

## 8. Endpoint và quyền truy cập

| Endpoint | Cơ chế |
| --- | --- |
| `/auth/login`, `/auth/refresh` | Không yêu cầu access JWT; kiểm tra tài khoản hoặc refresh token trong service |
| `/auth/register` | Yêu cầu xác thực và role `MANAGER` qua `@PreAuthorize` |
| `/devices/token`, `/pda/events`, `/pda/commands`, `/pda/fcm-health` | Được permit ở filter chain; controller/service xử lý device credentials |
| `/actuator/health`, `/web/finder/**` | Được permit ở filter chain này |
| Các endpoint khác | Yêu cầu xác thực, có thể thêm điều kiện quyền tại method |

`permitAll()` không có nghĩa request mang Bearer sai sẽ được bỏ qua kiểm tra token. Client device-secret không nên gắn access token người dùng vào các request này.

## 9. Mã lỗi xác thực

Lỗi từ filter được `JsonAuthenticationEntryPoint` trả HTTP 401, `Content-Type: application/problem+json`, header `WWW-Authenticate: Bearer`, cùng `status`, `code`, `detail`.

| Code | Nguồn / ý nghĩa |
| --- | --- |
| `AUTH_REQUIRED` | Endpoint cần xác thực nhưng không có Authorization |
| `ACCESS_TOKEN_INVALID` | Bearer token không hợp lệ |
| `ACCESS_TOKEN_EXPIRED` | Access token đã hết hạn |
| `REFRESH_TOKEN_INVALID` | Refresh JWT sai hoặc không khớp bản ghi/hash |
| `SESSION_EXPIRED` | Refresh token / phiên hết hạn |
| `SESSION_REVOKED` | Family không còn active, token bị dùng lại hoặc tài khoản không active trong luồng refresh |

Lỗi refresh được service ném dưới dạng `DomainException`; không đi qua cùng entry point của filter. Android dùng HTTP status và `code` để quyết định refresh, yêu cầu đăng nhập lại hoặc giữ session khi lỗi mạng/server.

## 10. Phạm vi hiện tại và kiểm tra

- Không có endpoint logout trong `AuthController` hiện tại. Logout trên PDA xóa session local; không tự gọi revoke trên backend.
- Header Android duy trì response ba trường cho client không opt-in metadata, nhưng chưa đủ để xác nhận tương thích WinCE. Repo không cung cấp luồng WinCE để kiểm tra toàn bộ contract.
- Khi thay đổi authentication, kiểm tra login sai/đúng, account inactive, access hết hạn, refresh rotation, dùng lại refresh token, concurrent refresh, hạn phiên cố định, gia hạn và response có/không có header Android.

Xem thêm cấu hình và phạm vi triển khai tại [JWT và thời hạn phiên](27-jwt-session-policy.md).
