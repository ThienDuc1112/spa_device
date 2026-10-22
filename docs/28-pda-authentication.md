# Authentication trên PDA Android

Tài liệu mô tả code hiện tại trong `pda-android`. Phía server được giải thích tại [Authentication trên Management](29-management-authentication.md).

## 1. Thành phần và trách nhiệm

Các đường dẫn Java bên dưới nằm trong `pda-android/app/src/main/java/com/company/pda/`.

| Thành phần | Trách nhiệm |
| --- | --- |
| `presentation/auth/LoginActivity.java` | Nhận tài khoản, gọi login, lưu token, mở Home |
| `data/remote/api/AuthApi.java` | Khai báo endpoint và kiểu dữ liệu login/refresh |
| `common/util/ApiCalls.java` | Gửi HTTP, gắn access token, kiểm tra hạn, refresh và retry |
| `data/local/preferences/TokenStorage.java` | Lưu session được mã hóa trên thiết bị |
| `common/util/SessionTime.java` | Tính thời gian phiên phục vụ UI và quyết định refresh |
| `common/util/ScreenTasks.java` | Chạy workflow, quản lý busy/retry và chuyển kết quả về UI |
| `common/util/BaseActivity.java` | Nhận kết quả qua `httpReceiver`, theo dõi phiên, hiện popup gia hạn và chuyển về Login |
| `common/util/HttpReceiver.java` | Callback `onSuccess` / `onError` của màn hình |

## 2. Đăng nhập và lưu token

1. `LoginActivity.login()` kiểm tra username/password không trống.
2. `ScreenTasks.runAsync()` gọi `modules.authApi.login(...)`.
3. `AuthApi` gọi `ApiCalls.callApi()` với `POST auth/login`. Request có `PDA_TYPE: ANDROID`, không tự gắn Bearer token vì đường dẫn bắt đầu bằng `auth/`.
4. Server trả về `accessToken`, `refreshToken`, `expiresIn` và metadata của phiên.
5. `thenApplyAsync(..., modules.io)` lưu token bằng `TokenStorage.tokens()` trước khi báo thành công.
6. Kết quả đi qua `BaseActivity.httpReceiver()` tới callback đăng ký tại Login; callback mở Home.

`TokenStorage` lưu toàn bộ session dưới key `session` trong SharedPreferences `secure`. Dữ liệu được mã hóa bằng `AES/GCM/NoPadding`; khóa nằm trong Android Keystore với alias `retail-pda-v1`. Khi lưu, ứng dụng thêm `receivedAt` để tính độ lệch thời gian với server.

Nếu đã có token local, Login mở Home; đây chỉ là kiểm tra tồn tại session. Home kiểm tra hạn phiên, còn server xác thực token khi nhận HTTP.

## 3. Gọi API bằng `callApi`

```java
return api.callApi(
    "GET",
    "products/barcode/" + ApiCalls.path(barcode),
    null,
    ProductDto.class,
    Map.of(),
    executor);
```

`callApi(method, path, body, type, headers, executor)` trả về `CompletableFuture<T>` và chạy HTTP bằng `CompletableFuture.supplyAsync`. Executor truyền vào từ các API client là pool HTTP trong `AppModule`.

Luồng bên trong:

1. Xác định request dùng JWT: đường dẫn không bắt đầu bằng `auth/` và headers không chứa `X-Device-Secret`.
2. Với request dùng JWT, gọi `accessTokens()` để lấy token và kiểm tra hạn.
3. Tạo `HttpURLConnection`, gắn `PDA_TYPE: ANDROID`, các header được truyền vào và `Authorization: Bearer <accessToken>` nếu có token.
4. Serialize body bằng Gson, gửi request, đọc response.
5. Thành công: decode JSON về kiểu `type`; body rỗng hoặc `Void.class` trả về `null`.
6. Thất bại: tạo `ApiException` chứa HTTP status, `code` và `detail`. Lỗi IO được bọc trong `CompletionException`.
7. Đóng kết nối trong `finally`.

Connect timeout là 10 giây, read timeout là 20 giây; không tự theo redirect. Hàm không có xử lý cancel riêng: hủy future không chủ động ngắt HTTP đang chạy.

Nếu không có token local, `accessTokens()` trả `null`; request vẫn được gửi không có Bearer và server quyết định quyền truy cập.

## 4. Access token hết hạn và tự refresh

`accessTokens()` kiểm tra **hạn phiên trước**, rồi mới kiểm tra hạn access token:

- Phiên hết hạn: xóa session phù hợp và báo `SESSION_EXPIRED`, không gửi API nghiệp vụ.
- Access token hết hạn nhưng phiên còn hạn: gọi `refresh(current, false)` trước khi gửi API.
- Access token còn hạn: gửi API ngay.

Nếu API vẫn trả 401 sau khi gửi:

| Tình huống | Xử lý |
| --- | --- |
| Có token đã gửi, lần đầu lỗi, code là `ACCESS_TOKEN_EXPIRED` hoặc không có code | Refresh rồi gửi lại API một lần |
| 401 có code khác khi đã gửi token | Xóa session phù hợp và trả lỗi |
| Request đã retry mà vẫn 401 | Xóa session phù hợp, không retry tiếp |
| Request auth hoặc device-secret | Không tự refresh JWT |
| Lỗi khác 401 | Trả lỗi về callback |

`refresh()` gửi `POST auth/refresh` với refresh token hiện tại. Refresh thông thường không gửi `extendSession: true`, nên server giữ nguyên hạn phiên.

`refreshLock` tuần tự hóa refresh trong cùng instance `ApiCalls`. Sau khi lấy khóa, hàm đọc lại token: nếu request khác đã đổi access token thì dùng token mới, tránh dùng refresh token cũ hai lần. Sau response, chỉ lưu token mới nếu refresh token trong storage vẫn khớp với phiên đang xử lý; logout trong lúc chờ không bị ghi đè.

Refresh bị 401 sẽ xóa session phù hợp. Lỗi mạng, 5xx hoặc response token không hợp lệ giữ token local để có thể thử lại. `invalidate()` kiểm tra refresh token trước khi logout để tránh xóa một phiên mới khác.

## 5. Thời gian và gia hạn phiên

| Thông số mặc định từ server | Giá trị |
| --- | --- |
| Access token | 15 phút, tối đa đến hạn phiên |
| Phiên / refresh token | 2 giờ |
| Cảnh báo trước khi hết phiên | 5 phút |

`SessionTime` ưu tiên `accessExpiresAt` và `sessionExpiresAt` do server trả về, đơn vị milliseconds. Thời gian hiện tại được ước tính bằng `serverTime + (localNow - receivedAt)`. Nếu thiếu metadata, đọc claim `exp` trong JWT làm gợi ý; việc đọc này không xác minh chữ ký JWT.

Ở Activity yêu cầu phiên (`requiresSession()` trả `true`), `BaseActivity` kiểm tra mỗi giây khi Activity đang resumed:

- Cập nhật countdown `HH:mm:ss` trong `SessionBubble`, một bong bóng nổi trên nội dung màn hình; không cuộn theo danh sách.
- Khi còn dưới `warningSeconds` (fallback 300 giây), hiện popup gia hạn.
- Người dùng chọn **Extend session**: gọi `renewSession()` → `ApiCalls.extendSession(executor)` → `refresh(current, true)`.
- Server chấp nhận gia hạn: lưu cặp token mới và cập nhật countdown. Hạn phiên mới là thời điểm xử lý trên server cộng 2 giờ theo cấu hình.
- Gia hạn lỗi mạng: hiện thông báo và nút Retry; người dùng cần thử lại trước khi phiên hết hạn.

Bong bóng dùng chung trong `BaseActivity`: kéo để đổi vị trí, thả để bám cạnh trái/phải; vị trí được giữ khi đổi màn hình hoặc xoay máy. Chạm bong bóng để xem thời gian còn lại và chọn **Extend session**. Khi gần hết phiên, bong bóng đổi màu và nhãn thành **Expiring**, đồng thời giữ popup cảnh báo. Đây là thời gian còn lại của phiên, không phải thời gian access token.

Các Fragment sản phẩm, ảnh, tồn kho và disposal dùng bong bóng của `HomeActivity`, không tạo timer riêng. `FinderSoundActivity` cũng dùng `BaseActivity` và theo dõi phiên nếu mở khi đang đăng nhập; màn hình thiết lập âm thanh vẫn có thể mở độc lập khi chưa có phiên. Màn hình Login không hiển thị bong bóng.

Để thêm màn hình nghiệp vụ mới, kế thừa `BaseActivity` (hoặc `AlarmAwareActivity`) và trả `true` từ `requiresSession()`. Base gắn bong bóng sau khi Activity đã dựng nội dung (`onPostResume`), dừng timer và tháo bong bóng khi pause; quay lại màn hình sẽ tính lại từ hạn phiên thực tế. Không cần thêm countdown vào layout từng màn hình. Bong bóng nằm trong cửa sổ ứng dụng, không cần quyền hiển thị đè lên app khác; các dialog và màn hình hệ thống vẫn có lớp hiển thị riêng.

Tự refresh access token không kéo dài phiên. Chỉ yêu cầu gia hạn rõ ràng mới gửi `extendSession: true`. Nếu một refresh khác vừa hoàn tất, `refresh()` có thể dùng lại token mới và chưa gửi yêu cầu gia hạn; UI tiếp tục dùng hạn thực tế trong token nhận được.

## 6. Trả kết quả và kết thúc phiên

```java
tasks.request(
    "Loading product...",
    () -> modules.productsApi.product(barcode),
    httpReceiver);
```

Supplier giúp chỉ tạo request sau kiểm tra busy/vòng đời và tạo request mới khi Retry. `ScreenTasks` chuyển kết quả về main thread, chờ lifecycle STARTED nếu cần và bỏ callback khi màn hình bị hủy.

`BaseActivity.httpReceiver()` kiểm tra session trước khi gọi callback. Nếu màn hình yêu cầu đăng nhập, có lỗi và session đã bị xóa, ứng dụng chuyển về Login với thông báo. Timer cũng kết thúc phiên khi không còn token hoặc đã hết hạn.

`TokenStorage.logout()` chỉ xóa session người dùng local; không xóa device credentials và không gọi endpoint thu hồi phiên trên server.

## 7. Xác thực thiết bị độc lập

Polling, cập nhật FCM token và gửi ACK dùng `X-Device-Id` / `X-Device-Secret` qua `PdaFinderApi`. `ApiCalls` không gắn JWT hay tự refresh với các request này. Vì vậy cần phân biệt lỗi device credentials với lỗi phiên đăng nhập người dùng. Tác vụ nền đồng bộ được lên lịch bằng `SyncJobService` / `JobInfo`.

## 8. Kiểm tra khi thay đổi luồng

- `ApiCallsTest`: refresh trước request, refresh sau 401, retry một lần, request đồng thời, lỗi refresh, logout trong lúc refresh, payload gia hạn.
- `SessionTimeTest`: tính thời gian và fallback `exp`.
- `SessionBubbleTest` (instrumentation): hiển thị qua điều hướng, tái tạo Activity và màn hình âm thanh; ẩn trên Login; kéo, giữ vị trí và chạm mở thông tin phiên.
- Trên thiết bị: đăng nhập, countdown, popup gia hạn, quay lại app sau khi hết phiên và mất mạng lúc gia hạn.

Chạy kiểm thử tự động từ thư mục `pda-android`: `gradlew.bat :app:testDebugUnitTest :app:lintDebug`.
