# Polling dự phòng cho PDA finder

PDA đã đăng ký nhận FIND/STOP qua hai đường: FCM và `GET /pda/commands`.
Polling chạy song song với FCM vì FCM nhận gửi thành công không chứng minh PDA đã nhận lệnh.

## Luồng hoạt động

1. Mở Home trên PDA đã đăng ký, hoặc đăng ký PDA thành công. App khởi động `FinderPollingService` với thông báo thường trực. Không cần FCM token để đăng ký.
2. Service gọi API bằng `X-Device-Id` và `X-Device-Secret`, không phụ thuộc phiên đăng nhập người dùng. Sau mỗi response thành công, chờ 5 giây rồi gọi tiếp; lỗi mạng/server giãn nhịp 10, 20, 40, tối đa 60 giây. Chỉ có một request đang chạy; timeout HTTP là 30 giây.
3. Backend xác thực secret và chỉ đọc yêu cầu thuộc đúng thiết bị/cửa hàng, còn thời hạn. QUEUED/SENT/RINGING trả FIND; trạng thái kết thúc trả STOP. Response không được cache.
4. `FinderCommandHandler` dùng chung cho FCM và polling, bỏ FIND hết hạn/đã xử lý/đang reo. STOP lưu tombstone nên FIND trễ không khởi động lại chuông. `PdaAlarmService` kiểm tra lại tombstone khi nhận start intent.
5. Token hỏng hoặc hết lượt retry chỉ kết thúc việc gửi push; yêu cầu vẫn chờ polling tới hạn. Nếu không hoàn thành, scheduler chuyển thành EXPIRED. RINGING/STOPPED/FAILED vẫn được gửi qua hàng chờ `SyncWorker`.

API mẫu:

```http
GET /pda/commands
X-Device-Id: 1
X-Device-Secret: <secret đã lưu khi đăng ký>
```

```json
[{"requestId":"12345678-1234-1234-1234-123456789012","command":"FIND","expiresAt":"2026-09-28T14:00:00Z"}]
```

Lệnh được trả lại cho tới hết hạn; không xóa lệnh khi GET để tránh mất lệnh nếu response bị mất. Màn hình người quản lý vẫn refresh trạng thái thủ công; đây là polling nhận lệnh trên PDA đích.

## Chạy nền và khóa màn hình trên PDA được quản lý

- Service sử dụng foreground-service type `specialUse`, thông báo thường trực và partial wake lock có lease 10 phút được gia hạn trong vòng polling. Wake lock được giải phóng khi service dừng. Thiết kế liên tục này tiêu thụ pin/mạng, phù hợp PDA doanh nghiệp cần khả năng tìm máy.
- Cấu hình EMM/OEM cho phép Store PDA chạy nền và miễn tối ưu pin/Doze; giữ kết nối Wi-Fi khi khóa màn hình. Foreground service riêng lẻ không đủ để vượt Doze. Miễn tối ưu pin cũng cần cho khả năng khởi động alarm service từ nền trên Android áp dụng hạn chế foreground-service start.
- Service tiếp tục sau khi rời app/đăng xuất; device secret độc lập với tài khoản. Android có thể khởi động lại service `START_STICKY` sau khi hủy process, nhưng không bảo đảm thời điểm. Sau reboot, force-stop hoặc người dùng dừng ứng dụng, mở Home lại (hoặc EMM mở app). Không tự khởi động khi boot.
- HTTP 401/403 dừng polling; kiểm tra đăng ký/credential và mở Home lại sau khi khắc phục. Không có mạng vẫn không thể nhận lệnh; lệnh quá hạn không phát chuông muộn.
- `specialUse` phải được khai báo/đánh giá phù hợp nếu phân phối qua Google Play. Không sử dụng `dataSync` để giả định dịch vụ có thể chạy vô hạn.

Tham khảo Android: [foreground service types](https://developer.android.com/develop/background-work/services/fgs/service-types), [Doze](https://developer.android.com/training/monitoring-device-state/doze-standby), [background start restrictions](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start).

## Kiểm tra triển khai

1. Bật backend scheduler, đặt `FCM_ENABLED=false`; build app trỏ về backend thử nghiệm, đăng ký PDA (có thể không cấu hình Firebase).
2. Mở Home, xác nhận có thông báo Finder availability; cấu hình chính sách pin như trên, đưa app về nền và khóa màn hình.
3. Từ PDA quản lý khác gửi FIND: máy đích phải reo qua HTTP; kiểm tra trạng thái RINGING. Gửi STOP: máy đích dừng ở lượt polling tiếp theo. Trong điều kiện mạng tốt, chu kỳ là 5 giây cộng thời gian HTTP.
4. Thử mất mạng, khôi phục mạng trước/sau hạn lệnh; thử FCM cùng polling, STOP trước FIND, token FCM hỏng và secret không hợp lệ.
5. Lặp lại trên từng model PDA/phiên bản Android, bao gồm Doze thực tế. Build/test tự động không chứng minh hoạt động khóa màn hình trên thiết bị thật.

## Kết quả kiểm tra ngày 2026-09-28

- Backend `mvn verify`: 24 integration tests đạt, bao gồm sai secret/cách ly thiết bị, token FCM hỏng, hết lượt retry, STOP, hết hạn và đăng ký không có Firebase token.
- Android `assembleDebug`, `assembleDebugAndroidTest`, `testDebugUnitTest`, `lintDebug`: đạt; 3 unit tests đạt, lint không có error (18 warnings).
- `FinderCommandTest` trên emulator Pixel_10a / Android 17: 3 tests đạt, kiểm tra STOP trước FIND, dữ liệu hết hạn/sai định dạng và polling service còn foreground sau khi activity đóng.
- Kiểm tra cấu trúc module và OpenAPI đạt. Chưa kiểm thử luồng HTTP tới phát chuông khi khóa màn hình/Doze trên PDA thật, hoặc FCM trực tiếp.
