# Chọn FCM hoặc polling bằng cấu hình

Từ thay đổi ngày 03/10/2026, **FCM là chế độ mặc định; không còn tự fallback theo lỗi FCM**. Giữ tên file để các liên kết cũ vẫn hoạt động.

## Hai chế độ riêng

| Cấu hình Android | Nhận FIND/STOP | Service polling | Kiểm tra FCM health định kỳ |
| --- | --- | --- | --- |
| `finderTransport=fcm` (mặc định) | FCM data message | Không chạy | Không |
| `finderTransport=polling` | HTTP `GET /pda/commands` | Chạy, chờ 5 giây sau lượt thành công | Không |

Cả hai dùng chung xử lý lệnh, service âm thanh, kiểm tra DND, chống lệnh trùng và ACK qua HTTP. FCM mode vẫn có HTTP đăng ký, đồng bộ token và sự kiện; chỉ bỏ HTTP polling lệnh/health.

## Cách đổi

Sửa `finderTransport` trong [pda-android/gradle.properties](../pda-android/gradle.properties), Gradle Sync, build và cài lại APK trên PDA đích. Hoặc chạy trong thư mục Android:

```powershell
.\gradlew.bat :app:assembleDebug -PfinderTransport=polling
# Đổi lại:
.\gradlew.bat :app:assembleDebug -PfinderTransport=fcm
```

Mỗi lệnh tạo APK ở cùng đường dẫn; cài đúng bản vừa build. Cài cập nhật cùng applicationId/chữ ký giúp giữ đăng ký thiết bị. Mở Home sau khi cài. Thay đổi chỉ áp dụng sau build/cài lại, không phải toggle runtime.

[app/build.gradle](../pda-android/app/build.gradle) kiểm tra giá trị cấu hình và sinh `BuildConfig.FINDER_TRANSPORT`. Không truyền property thì dùng `fcm`; gõ sai làm build thất bại, không âm thầm chọn transport khác.

## Các điểm chặn trong code

- [FinderPollingService](../pda-android/app/src/main/java/com/company/pda/infrastructure/firebase/FinderPollingService.java): `start()` không khởi động và dừng service cũ ở FCM mode; `onStartCommand()` kiểm tra lại để chặn direct start/sticky restart.
- [FinderPollingCycle](../pda-android/app/src/main/java/com/company/pda/infrastructure/firebase/FinderPollingCycle.java): chỉ gọi commands nếu bật polling, không còn health check hoặc trạng thái lỗi FCM.
- [PdaFirebaseMessagingService](../pda-android/app/src/main/java/com/company/pda/infrastructure/firebase/PdaFirebaseMessagingService.java): bỏ qua message ở polling mode; vẫn giữ callback token.
- `HomeActivity.checkRegistration()` gọi health một lần khi Home resume để kiểm tra credential còn hiệu lực sau khi xóa PDA trên web; không dựa vào trạng thái FCM để đổi transport.
- [HomeActivity](../pda-android/app/src/main/java/com/company/pda/presentation/home/HomeActivity.java): khi resume thử lại lấy token nếu có `TOKEN_ERROR`, độc lập với polling.
- [FinderCommandHandler](../pda-android/app/src/main/java/com/company/pda/infrastructure/firebase/FinderCommandHandler.java): tiếp tục dùng chung cho cả hai transport; không tự chọn/chuyển transport.

## Lỗi FCM và cấu hình backend

Thiếu Firebase/token, `TOKEN_ERROR`, `DELIVERY_ERROR`, backend `NO_TOKEN`/`INVALID_TOKEN`/`RETRY`, thiếu ACK hoặc `FCM_ENABLED=false` đều **không tự bật polling**. FIND bị hạ priority hiển thị notification dự phòng; “fallback notification” không có nghĩa “fallback transport”.

Backend vẫn giữ request chưa hết hạn để endpoint commands có thể trả cho thiết bị chọn polling. Backend không biết chế độ build của mỗi PDA. Nút Tìm trên website React tạo request nhưng không quyết định transport của máy đích; xem [luồng website mới](21-react-device-finder.md).

`FCM_ENABLED=true` và credential Firebase hợp lệ là điều kiện gửi push phía backend, không phải cấu hình chọn transport Android. Mặc định backend vẫn tắt gửi khi chưa cấu hình credential. Muốn chạy FCM đầy đủ cần bật cờ này và scheduler. Chỉ tắt FCM toàn backend nếu không còn PDA nào cần push.

API `/pda/fcm-health` vẫn dùng được để chẩn đoán/client cũ. `fallbackRequired=true` không điều khiển Android hiện tại. Polling mode cũng không tự về FCM khi health trở lại READY.

## Kiểm tra trước triển khai

Chạy unit tests và build cho cả hai giá trị; test thiết bị phải xác nhận bản FCM không chạy polling khi thiếu token, bản polling nhận lệnh không phụ thuộc Firebase, và receiver FCM bị bỏ qua ở bản polling. Tiếp tục kiểm thử FIND/STOP, hết hạn, lỗi credential, khóa màn hình và DND trên thiết bị thực.

Chi tiết triển khai tại [15-finder-polling.md](15-finder-polling.md); luồng FCM kèm code tại [20-fcm-to-pda-alarm-guide.md](20-fcm-to-pda-alarm-guide.md).
