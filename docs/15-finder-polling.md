# Polling dự phòng cho PDA finder

PDA đã đăng ký nhận FIND/STOP qua hai đường: FCM và `GET /pda/commands`.
Polling **lệnh** chỉ chạy khi đã ghi nhận lỗi FCM tại PDA hoặc backend. Khi FCM không có lỗi được ghi nhận, app không gọi `/pda/commands`; vẫn kiểm tra trạng thái `/pda/fcm-health` mỗi 15 giây để biết lỗi gửi ở backend.

Điều kiện nằm trong `FinderPollingCycle.run()`:

```java
if (!localFcmFailed && !backendFailed) {
  delaySeconds = 15;
  return List.of(); // Không gọi API lấy lệnh
}
delaySeconds = 5;
return transport.commands();
```

## Luồng hoạt động

1. Mở Home trên PDA đã đăng ký, hoặc đăng ký PDA thành công. App khởi động `FinderPollingService` với thông báo thường trực. Không cần FCM token để đăng ký.
2. Service gọi API bằng `X-Device-Id` và `X-Device-Secret`, không phụ thuộc phiên đăng nhập người dùng. Kiểm tra trạng thái FCM mỗi 15 giây; chỉ khi có lỗi FCM mới lấy lệnh và chờ 5 giây sau mỗi lượt thành công. Lỗi HTTP giãn nhịp tối đa 60 giây. Chỉ có một request đang chạy; timeout HTTP là 30 giây, nên chu kỳ thực tế còn phụ thuộc thời gian HTTP/backoff.
3. Backend xác thực secret và chỉ đọc yêu cầu thuộc đúng thiết bị/cửa hàng, còn thời hạn. QUEUED/SENT/RINGING trả FIND; trạng thái kết thúc trả STOP. Response không được cache.
4. `FinderCommandHandler` dùng chung cho FCM và polling, bỏ FIND hết hạn/đã xử lý/đang reo. STOP lưu tombstone nên FIND trễ không khởi động lại chuông. `PdaAlarmService` kiểm tra lại tombstone khi nhận start intent.
5. Token hỏng hoặc hết lượt retry chỉ kết thúc việc gửi push; yêu cầu vẫn chờ polling tới hạn. Nếu không hoàn thành, scheduler chuyển thành EXPIRED. RINGING/STOPPED/FAILED vẫn được gửi qua hàng chờ `SyncWorker`.

## Điều kiện bật và dừng polling lệnh

| Tín hiệu | Kết quả |
| --- | --- |
| PDA chưa cấu hình Firebase, thiếu token hoặc lấy token thất bại | Bật polling lệnh |
| FIND nhận qua FCM bị hạ priority | Ghi `DELIVERY_ERROR`, bật polling lệnh |
| Backend `FCM_ENABLED=false`, thiếu token, hoặc lần gửi gần nhất lỗi (`RETRY`/`INVALID_TOKEN`/`NO_TOKEN`) | Bật polling lệnh |
| Cả PDA và backend không còn lỗi FCM | Dừng gọi `/pda/commands`, tiếp tục kiểm tra trạng thái 15 giây |
| HTTP kiểm tra trạng thái thất bại | Không tự kết luận FCM lỗi; giữ trạng thái FCM đã biết trước đó |
| HTTP 401/403 | Dừng service; cần xử lý credential |

Lỗi lấy token được thử lại mỗi 60 giây khi service chạy. Lấy token thành công xóa lỗi token, nhưng không xóa `DELIVERY_ERROR`; lỗi delivery chỉ được xóa khi PDA nhận lại message FCM priority HIGH. Backend dùng kết quả gửi push gần nhất theo đúng thiết bị/cửa hàng; gửi thành công về sau xóa trạng thái lỗi gửi ở lần kiểm tra tiếp theo. Sự kiện RINGING gửi qua HTTP không được coi là FCM đã phục hồi.

`GET /pda/fcm-health` dùng cùng device credential, không cache, ví dụ `{"fallbackRequired":true,"reason":"PUSH_FAILED"}`. Các reason khác: `FCM_DISABLED`, `NO_TOKEN`, `READY`.

Giới hạn có chủ đích: READY là không có lỗi đã ghi nhận, không bảo đảm FCM đã tới máy. Nếu FCM nhận gửi thành công nhưng âm thầm không giao message, cơ chế này không tự phát hiện qua việc thiếu message; không bật polling chỉ vì lâu không nhận push. Cũng không tự bật polling chỉ do Android chặn khởi động alarm hoặc do loa lỗi. Nếu backend đang cấu hình `FCM_ENABLED=false`, polling sẽ luôn được bật vì kênh FCM đã bị tắt.

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

Nếu bấm Find lần nữa khi PDA còn yêu cầu `QUEUED`/`SENT`/`RINGING` chưa hết hạn, backend trả HTTP 409 với thông báo yêu cầu dừng hoặc chờ lần tìm hiện tại. Mỗi PDA chỉ có một yêu cầu tìm hoạt động. Khi yêu cầu cũ đã hết hạn, lần Find mới tự chuyển nó sang `EXPIRED` và ghi log trong cùng transaction trước khi tạo yêu cầu mới; không cần đợi scheduler. Cơ chế này xử lý cả profile dev tắt scheduler. Hai lần Find đồng thời vẫn chỉ tạo một yêu cầu và một sự kiện outbox. Không xóa dữ liệu hoặc bỏ unique index `one_active_find` để xử lý lỗi trùng.

1. Bật backend scheduler, đặt `FCM_ENABLED=false`; build app trỏ về backend thử nghiệm, đăng ký PDA (có thể không cấu hình Firebase).
2. Mở Home, xác nhận có thông báo Finder availability; cấu hình chính sách pin như trên, đưa app về nền và khóa màn hình.
3. Từ PDA quản lý khác gửi FIND: máy đích phải reo qua HTTP; kiểm tra trạng thái RINGING. Gửi STOP: máy đích dừng ở lượt polling tiếp theo. Trong điều kiện mạng tốt, chu kỳ là 5 giây cộng thời gian HTTP.
4. Với Firebase được cấu hình và `FCM_ENABLED=true`, xác nhận FCM tốt chỉ gọi `/pda/fcm-health`, không gọi `/pda/commands`. Gây lỗi gửi FCM rồi kiểm tra lệnh bắt đầu được poll; khôi phục FCM và gửi push thành công để kiểm tra polling lệnh dừng. Thử mất mạng trước/sau hạn lệnh, STOP trước FIND, token FCM hỏng và secret không hợp lệ.
5. Lặp lại trên từng model PDA/phiên bản Android, bao gồm Doze thực tế. Build/test tự động không chứng minh hoạt động khóa màn hình trên thiết bị thật.

## Kết quả kiểm tra bản polling có điều kiện — 2026-09-29

- Backend `mvn verify`: 8 unit tests và 25 integration tests đạt. Kiểm tra endpoint health có xác thực/no-cache, truy vấn đúng thiết bị/cửa hàng, phân biệt lỗi gửi với sự kiện RINGING và phục hồi sau lần gửi thành công.
- Android `assembleDebug`, `assembleDebugAndroidTest`, `testDebugUnitTest`, `lintDebug`: đạt. 7 unit tests đạt, gồm các ca không gọi API lệnh khi FCM tốt, bật/tắt theo lỗi/phục hồi, lỗi HTTP không tự bật fallback và 401 chặn lấy lệnh. Lint không có error (18 warnings).
- `FinderCommandTest` trên emulator Pixel_10a / Android 17: 4 tests đạt. Ca mới kiểm tra lấy lại token không xóa nhầm lỗi delivery, và nhận FCM lại mới xóa lỗi đó. Lượt instrumentation đầu bị hệ thống dừng vì `LOW_MEMORY`; chạy lại sau khi emulator ổn định đạt cả 4 tests.
- Kiểm tra cấu trúc module, OpenAPI và diff whitespace đạt. Chưa xác nhận FCM trực tiếp hoặc luồng khóa màn hình/Doze trên PDA thật.

## Kết quả bản polling song song trước đây — 2026-09-28

- Backend `mvn verify`: 24 integration tests đạt, bao gồm sai secret/cách ly thiết bị, token FCM hỏng, hết lượt retry, STOP, hết hạn và đăng ký không có Firebase token.
- Android `assembleDebug`, `assembleDebugAndroidTest`, `testDebugUnitTest`, `lintDebug`: đạt; 3 unit tests đạt, lint không có error (18 warnings).
- `FinderCommandTest` trên emulator Pixel_10a / Android 17: 3 tests đạt, kiểm tra STOP trước FIND, dữ liệu hết hạn/sai định dạng và polling service còn foreground sau khi activity đóng.
- Kiểm tra cấu trúc module và OpenAPI đạt. Chưa kiểm thử luồng HTTP tới phát chuông khi khóa màn hình/Doze trên PDA thật, hoặc FCM trực tiếp.
