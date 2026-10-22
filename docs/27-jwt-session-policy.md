# JWT và thời hạn phiên Android

## Cấu hình chung

Backend dùng `SessionPolicy` cho cả JWT, dữ liệu refresh token và response:

| Biến môi trường | Mặc định | Ý nghĩa |
| --- | --- | --- |
| `ACCESS_TOKEN_SECONDS` | 900 | Access token 15 phút, không vượt quá giờ hết phiên |
| `SESSION_SECONDS` | 7200 | Phiên/refresh token 2 giờ |
| `SESSION_WARNING_SECONDS` | 300 | Cảnh báo trước 5 phút |

Không cần thay đổi schema hiện có. Thời hạn phiên lưu trong `refresh_tokens.expires_at`.

## Luồng xử lý

1. Login cấp access/refresh token. Android lưu cả cặp và metadata trong một bản ghi được mã hóa AES-GCM bằng Android Keystore.
2. Android gửi `PDA_TYPE: ANDROID` trong HTTP header. Response login/refresh cho client này bổ sung `serverTime`, `accessExpiresAt`, `sessionExpiresAt` (epoch milliseconds), `warningSeconds`.
3. Trước API có xác thực, Android kiểm tra thời hạn access token để refresh nếu cần. Server vẫn kiểm tra chữ ký, issuer, audience, loại token, thời hạn và trạng thái phiên; thời gian phía client chỉ phục vụ lịch refresh/UX.
4. Refresh tự động xoay token nhưng giữ nguyên giờ hết phiên. Refresh token cũ chỉ dùng được một lần. Dùng lại token đã tiêu thụ sẽ thu hồi cả family; access token mới có `sessionId` cũng bị từ chối khi family không còn hoạt động.
5. `BaseActivity` hiển thị thời gian còn lại bằng bong bóng nổi dùng chung cho các màn hình có phiên, gồm Home/các Fragment và thiết lập âm thanh khi đang đăng nhập. Có thể kéo đổi vị trí, chạm xem thông tin và chọn **Extend session**. Trước khi hết phiên 5 phút, bong bóng đổi màu và `BaseActivity` hiện popup cho phép gia hạn.
6. Gia hạn gọi cùng `/auth/refresh` với `extendSession: true`. Chỉ refresh token còn hợp lệ mới được gia hạn; hạn mới là thời điểm xử lý + 2 giờ. Tất cả refresh trong ứng dụng dùng chung khóa để tránh dùng cùng token hai lần.
7. Hết phiên hoặc token/phiên bị vô hiệu hóa: xóa phiên local, về Login và hiện thông báo. Lỗi mạng/server khi refresh giữ phiên local để thử lại trong thời hạn còn hiệu lực.

Các mã lỗi xác thực nằm trong `code`, đi kèm HTTP status và `detail`:

| Code | HTTP | Xử lý Android |
| --- | --- | --- |
| `ACCESS_TOKEN_EXPIRED` | 401 | Refresh rồi retry request tối đa một lần |
| `ACCESS_TOKEN_INVALID` | 401 | Xóa phiên và yêu cầu đăng nhập |
| `REFRESH_TOKEN_INVALID` | 401 | Xóa phiên và yêu cầu đăng nhập |
| `SESSION_EXPIRED` | 401 | Xóa phiên và yêu cầu đăng nhập |
| `SESSION_REVOKED` | 401 | Xóa phiên và yêu cầu đăng nhập |
| `TOKEN_REFRESH_FAILED` | 500 | Giữ phiên, báo lỗi và cho thử lại |

Luồng dùng device secret (`JobInfo`, ACK, polling) hoạt động độc lập với phiên người dùng.

## Rollout và phạm vi tương thích

Triển khai backend trước rồi cập nhật Android. Client không gửi header opt-in tiếp tục nhận ba trường login/refresh cũ (`accessToken`, `refreshToken`, `expiresIn`). Token access cũ chưa có claim `sessionId` vẫn dùng được tới thời điểm hết hạn đã ký; cơ chế kiểm tra family áp dụng với token mới. Refresh token đã cấp trước triển khai giữ hạn đã cấp cho tới khi gia hạn chủ động/login lại. Android mới có thể đọc `exp` làm gợi ý thời gian nếu backend cũ chưa gửi metadata; chính sách 2 giờ cần backend mới.

**Chưa xác minh WinCE:** repo không chứa nhánh xác thực WinCE hoặc contract `PDA_TYPE=PDA` được mô tả trong ảnh. Không thêm nhánh bỏ qua JWT dựa trên giá trị do client tự gửi. Trước khi đưa backend này vào hệ thống dùng chung thực tế, cần cung cấp luồng xác thực WinCE và kiểm thử các request/response cũ trên cùng endpoint. Việc giữ response login ba trường không đủ để khẳng định toàn bộ WinCE tương thích.

## Kiểm tra

- Android unit tests: refresh chủ động, refresh đồng thời, retry một lần, phiên hết hạn, mã revoked, payload gia hạn, tính thời gian và fallback JWT.
- Backend integration tests: refresh xoay vòng, dùng lại token thu hồi access token, hạn phiên cố định khi refresh, gia hạn chủ động, phản hồi client cũ và lỗi token giả mạo.
- Kiểm thử thủ công trên thiết bị: countdown, popup, từ nền quay lại khi hết phiên, mất mạng lúc gia hạn, xoay màn hình trong lúc refresh. Có thể giảm cấu hình trên môi trường thử nghiệm để kiểm tra nhanh.

Tham khảo: [RFC 9700 – refresh token rotation](https://www.rfc-editor.org/rfc/rfc9700.html#section-4.14.2).
