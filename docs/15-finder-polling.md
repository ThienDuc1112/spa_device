# Polling theo cấu hình cho PDA finder

Mặc định Android dùng **FCM**, không chạy service polling và không gọi định kỳ `/pda/fcm-health`. Chỉ APK có `finderTransport=polling` mới nhận FIND/STOP bằng HTTP. Thiếu token, lỗi gửi FCM, priority bị hạ hoặc backend tắt FCM không tự chuyển chế độ.

## Cấu hình trên PDA đích

Trong [gradle.properties](../pda-android/gradle.properties):

```properties
finderTransport=fcm
```

Đổi thành `polling`, Gradle Sync rồi build/cài lại app từ Android Studio. Hoặc ghi đè cho một lần build, từ thư mục `pda-android`:

```powershell
.\gradlew.bat :app:assembleDebug -PfinderTransport=polling
# Trở lại FCM:
.\gradlew.bat :app:assembleDebug -PfinderTransport=fcm
```

Cài APK `app/build/outputs/apk/debug/app-debug.apk` lên **PDA cần tìm**, rồi mở Home. Thay cấu hình chỉ trên máy quản lý không đổi chế độ của PDA đích. Đây là cấu hình lúc build; không có chuyển chế độ từ xa/runtime. Giá trị không phải `fcm` hoặc `polling` làm build thất bại.

Backend `FCM_ENABLED` chỉ điều khiển gửi push. Có thể đặt false khi toàn bộ PDA dùng polling; nếu còn PDA dùng FCM thì phải giữ true và cấu hình Firebase hợp lệ. Bật scheduler backend để xử lý outbox và hết hạn; profile dev mặc định bật scheduler (có thể tắt bằng `APP_SCHEDULER_ENABLED=false`).

## Luồng hoạt động

1. Mở Home trên PDA đã đăng ký hoặc đăng ký thành công. `FinderPollingService.start()` chỉ chạy foreground service khi cấu hình là polling. Với FCM, nó dừng service polling nếu có rồi trả về.
2. Service gọi `GET /pda/commands` bằng `X-Device-Id` và `X-Device-Secret`. Không cần token Firebase và không gọi health để quyết định lấy lệnh.
3. Mỗi lượt thành công chờ 5 giây. Thời gian thực tế gồm HTTP và khoảng chờ; lỗi giãn nhịp tối đa 60 giây, thành công đưa nhịp về 5 giây. HTTP 401/403 dừng service; cần sửa credential rồi mở Home lại.
4. Backend chỉ trả request đúng thiết bị/cửa hàng, chưa hết hạn. QUEUED/SENT/RINGING trả FIND; trạng thái kết thúc trả STOP. GET không xóa lệnh, response không cache.
5. `FinderCommandHandler` kiểm tra UUID, deadline, phiên đang reo và lệnh đã xử lý. STOP ghi tombstone để FIND trễ không khởi động lại. Lệnh hợp lệ dùng cùng `PdaAlarmService` như FCM.
6. APK polling bỏ qua FIND/STOP đến qua FCM ngay trong receiver. Lỗi hoặc phục hồi FCM không đổi chế độ. Token Firebase vẫn có thể được SDK đồng bộ; backend vẫn có thể gửi push nếu đang bật.
7. RINGING/STOPPED/FAILED vẫn được gửi qua Room và `SyncWorker` bằng HTTP trong cả hai chế độ.

```http
GET /pda/commands
X-Device-Id: 1
X-Device-Secret: <secret nhận khi đăng ký PDA>
```

`GET /pda/fcm-health` vẫn tồn tại để chẩn đoán/tương thích client cũ. Trường `fallbackRequired` không còn là tín hiệu chuyển transport của Android hiện tại.

## Chạy nền và khóa màn hình

- Polling dùng foreground service loại `specialUse`, thông báo thường trực và partial wake lock có lease 10 phút được gia hạn; service giải phóng khi dừng. FCM mặc định không chạy service/wake lock polling này.
- Cấu hình EMM/OEM cho phép app chạy nền, chính sách pin/Doze và kết nối Wi-Fi phù hợp. Polling không vượt giới hạn khởi động alarm service hoặc DND.
- Service có thể tiếp tục sau khi đóng activity/đăng xuất vì credential thiết bị độc lập với tài khoản. Android vẫn có thể dừng tiến trình; `START_STICKY` không bảo đảm luôn sống.
- Sau force-stop hoặc reboot, mở lại Home; chưa có boot receiver tự khởi động polling. Không có mạng không nhận được lệnh; FIND hết hạn không phát chuông muộn.
- Website React refresh trạng thái khoảng 5 giây/lần. Nút Tìm trên web không quyết định transport; chế độ thuộc APK của PDA đích.

## Kiểm thử

1. Build/cài APK mặc định FCM, đăng ký PDA, mở Home. Không có service/thông báo polling và không gọi `/pda/commands` hoặc `/pda/fcm-health` định kỳ.
2. Với bản FCM, thử thiếu token hoặc tắt backend FCM: không tự xuất hiện polling. Muốn nhận lệnh FCM phải khôi phục Firebase client/backend, bật FCM và scheduler.
3. Build/cài APK polling, mở Home: có thông báo nhận lệnh qua polling, gọi commands khoảng 5 giây một lượt ngay cả khi FCM khỏe.
4. Bấm Tìm/Dừng từ website React, kiểm tra tiếng chuông và ACK; thử mất mạng, credential sai, hết hạn, STOP trước FIND. Gửi thêm FCM tới bản polling và xác nhận receiver bỏ qua.
5. Cài lại bản FCM, mở Home và kiểm tra polling dừng. Test riêng nền/khóa màn hình trên PDA thật.

Unit tests nằm trong [FinderPollingCycleTest](../pda-android/app/src/test/java/com/company/pda/infrastructure/firebase/FinderPollingCycleTest.java); test vòng đời service theo từng build nằm trong [FinderCommandTest](../pda-android/app/src/androidTest/java/com/company/pda/FinderCommandTest.java). Test instrumentation cần chạy riêng với cả hai giá trị `finderTransport`; build test APK chưa phải chạy test trên thiết bị.

Xem [hướng dẫn polling tới chuông/rung PDA](26-polling-to-pda-alarm-guide.md), [cách chọn FCM/polling](16-fcm-to-polling-fallback.md) và [luồng FCM bật âm thanh](20-fcm-to-pda-alarm-guide.md).
