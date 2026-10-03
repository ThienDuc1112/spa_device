# 23. Cấu hình Firebase, backend và Android để tìm PDA bằng âm thanh

Hướng dẫn cho code hiện tại: tìm thiết bị trên React; Android đăng ký sau đăng nhập, nhận FCM và phát âm thanh. Thực hiện phần backend một lần và phần Android trên từng PDA.

```text
React bấm Find → Backend lưu request/outbox → Scheduler gửi Firebase
→ Android nhận data message → PdaAlarmService phát âm thanh → Android gửi ACK về backend
```

## 1. Chuẩn bị

| Thành phần | Điều kiện |
| --- | --- |
| Backend | JDK 8, Maven, PostgreSQL hoạt động; có Internet để kết nối Firebase |
| Android Studio | Mở thư mục `pda-android`; Gradle JDK 21, SDK 35; app hỗ trợ Android 8/API 26 trở lên |
| PDA/emulator | Có Google Play services tương thích, Internet và kết nối được API backend |
| Firebase | Backend và Android dùng cùng Firebase project |
| React | Chạy `pda-web`, proxy trỏ tới backend đang chạy |

Với emulator, tạo AVD có system image **Google Play** để có Google Play services. PDA không có Google Play services không phù hợp với luồng FCM hiện tại; nếu cần polling phải cấu hình và build riêng, app không tự chuyển. Xem [điều kiện Firebase Android](https://firebase.google.com/docs/android/setup).

## 2. Tạo hai file Firebase đúng vai trò

### File cho Android

1. Mở Firebase Console → chọn project → Project settings → Your apps.
2. Thêm/chọn Android app có package **`com.company.pda`**, khớp `applicationId` trong `app/build.gradle`.
3. Tải `google-services.json`, đặt tại:

   ```text
   pda-android/app/google-services.json
   ```

4. Sync Gradle và build/cài lại APK. Chỉ chép file sau khi đã cài APK không cập nhật cấu hình của app trên thiết bị.

Dự án đã có Firebase Messaging SDK và chỉ áp dụng Google Services plugin khi file tồn tại. Vì vậy build thành công khi thiếu file chưa chứng minh FCM đã được cấu hình. Xem [thiết lập FCM Android](https://firebase.google.com/docs/cloud-messaging/android/get-started).

### File cho backend

1. Trong cùng Firebase project: Project settings → Service accounts → Firebase Admin SDK.
2. Chọn **Generate new private key**, tải service-account JSON.
3. Lưu ngoài source, ví dụ `C:\secrets\pda-firebase-admin.json`.
4. Kiểm tra `project_id` trong file Admin khớp `project_info.project_id` trong file Android.

Tên file Admin tùy chọn. File này chứa private key: không commit, không đưa vào APK/React và không gửi nội dung qua chat. `google-services.json` không thay thế được credential Admin. Xem [Firebase Admin setup](https://firebase.google.com/docs/admin/setup).

## 3. Cấu hình và chạy backend

### Chạy từ IntelliJ

Vào **Run → Edit Configurations → PdaApplication → Modify options → Environment variables**. Thêm từng biến:

| Name | Value ví dụ |
| --- | --- |
| `GOOGLE_APPLICATION_CREDENTIALS` | `C:\secrets\pda-firebase-admin.json` |
| `FCM_ENABLED` | `true` |
| `APP_SCHEDULER_ENABLED` | `true` |

Thay bằng đường dẫn thật, không thêm dấu nháy trong bảng biến. Không nhập vào Active profiles. Bấm Apply/OK, dừng backend cũ rồi chạy lại; chờ `Started PdaApplication`.

Backend đọc credential bằng `GoogleCredentials.getApplicationDefault()`. Đặt một khóa tên `GOOGLE_APPLICATION_CREDENTIALS` trong YAML không tự tạo biến môi trường cho phương thức này.

### Chạy từ PowerShell thay cho IntelliJ

Từ thư mục gốc dự án, dùng JDK 8 và Maven trong PATH:

```powershell
mvn -f pda-management/pom.xml package
powershell -NoProfile -ExecutionPolicy Bypass -File .\pda-management\run-fcm.ps1 -CredentialPath 'C:\secrets\pda-firebase-admin.json'
```

Script đặt ba biến trên và kiểm tra loại file/project. Chỉ chạy một backend trên cổng 8080. PostgreSQL và cấu hình datasource của profile đang dùng phải đúng.

### Kiểm tra backend

Mở `http://localhost:8080/web/finder/configuration`, cần nhận:

```json
{"fcmEnabled":true,"schedulerEnabled":true}
```

Hai cờ chỉ xác nhận cấu hình, không chứng minh worker còn hoạt động hoặc FCM đã giao thành công. Giữ cấu hình `NetHttpTransport` và timeout 5 giây trong [FirebaseConfig](../pda-management/src/main/java/com/company/pda/infrastructure/firebase/FirebaseConfig.java): đã dùng để xử lý lần worker kẹt tại transport Apache HTTP/2 trên môi trường Java 8 này. Xem [chẩn đoán chi tiết](22-delete-device-and-finder-troubleshooting.md).

## 4. Cấu hình Android trước khi build

Trong [gradle.properties](../pda-android/gradle.properties):

```properties
finderTransport=fcm
```

Đây là cấu hình lúc build. Thay đổi cần build và cài lại; FCM lỗi không tự chuyển sang polling.

### Địa chỉ API

| Thiết bị chạy app | `apiUrl` |
| --- | --- |
| Android Studio emulator, backend cùng máy | `http://10.0.2.2:8080/` (mặc định) |
| PDA thật cùng mạng LAN | Ví dụ `http://192.168.1.10:8080/`, thay bằng IP máy backend |
| Bản release | URL HTTPS backend, kết thúc bằng `/` |

`localhost` trên PDA là chính PDA. Với máy thật, kiểm tra cùng mạng, firewall cho phép kết nối vào backend và cổng 8080 truy cập được. Bản debug cho phép HTTP để test local; bản release hiện chặn cleartext, nên dùng HTTPS.

Build từ thư mục `pda-android`:

```powershell
# Emulator
.\gradlew.bat assembleDebug -PfinderTransport=fcm

# Hoặc PDA thật: thay IP ví dụ bằng IP máy backend
.\gradlew.bat assembleDebug -PfinderTransport=fcm -PapiUrl=http://192.168.1.10:8080/
```

Cài `app/build/outputs/apk/debug/app-debug.apk` bằng Android Studio hoặc `adb install -r`. Nếu báo chữ ký không khớp, dùng APK cùng signing key để giữ dữ liệu; không tự gỡ app vì sẽ mất đăng ký cục bộ.

## 5. Thiết lập trên từng PDA sau khi cài

1. Mở app **Store PDA** ít nhất một lần.
2. Đăng nhập tài khoản thuộc cửa hàng cần quản lý.
3. Ở Home, hoàn tất form đăng ký thiết bị: mã thiết bị duy nhất và tên dễ nhận biết. Nếu bỏ qua, dùng nút đăng ký trên Home.
4. App tự lấy FCM token và đồng bộ lên backend; không cần nhập token bằng tay.
5. Mở React, kiểm tra đúng cửa hàng, đúng thiết bị và có push token. Token tồn tại chỉ là điều kiện ban đầu, chưa chứng minh đã nhận lệnh.

Sau khi xóa thiết bị ở web, quay lại Home để app kiểm tra đăng ký và đăng ký lại khi credential cũ không còn hợp lệ. Không tiếp tục Find một bản ghi cũ sau khi đã gỡ/cài lại hoặc xóa dữ liệu app.

### Quyền thông báo và chạy nền

- Android 13 trở lên: cho phép thông báo khi app hỏi; nếu đã từ chối, vào **Settings → Apps → Store PDA → Notifications → Allow** để thấy thông báo và nút Stop alarm.
- Không Force stop app khi thử nhận FCM; nếu đã Force stop, mở lại app trước khi test.
- Khi test màn hình khóa/chạy nền trên PDA thật, kiểm tra Battery/Background activity và chính sách quản lý thiết bị. Nếu hãng hạn chế app, cho phép chạy nền/auto-start theo cấu hình của hãng; tên tùy model.

`POST_NOTIFICATIONS` không phải quyền nhận FCM data và không phải điều kiện hệ thống bắt buộc để khởi động foreground service. Tuy nhiên cấp quyền giúp hiển thị thông báo điều khiển/fallback. Xem [quyền thông báo Android](https://developer.android.com/develop/ui/views/notifications/notification-permission).

Manifest đã khai báo Internet, foreground service, media playback, chỉnh âm lượng, DND access và wake lock; người dùng không cần tự thêm code quyền. Quyền DND vẫn phải cấp riêng trong Settings.

## 6. Cấu hình âm thanh và Do Not Disturb — thực hiện tại PDA

Ở Home mở **Finder sound settings**:

1. Khi test lần đầu, tắt DND để kiểm tra loa cơ bản.
2. Bấm **Test this PDA speaker (5 seconds)**. Test dùng âm lượng báo thức tối đa, nên chuẩn bị trước khi bấm.
3. Nếu cần dùng khi bật DND: bấm **Grant Do Not Disturb access**, cho phép **Store PDA**.
4. Quay lại app, bấm **Configure Do Not Disturb / allow alarms**. Trong chế độ đang dùng, bật **Alarms/Báo thức**. Kiểm tra cả các chế độ/lịch tự bật DND đang hoạt động.
5. Bật lại DND và chạy test loa 5 giây lần nữa. Kiểm tra thêm silent/vibrate nếu đó là cách sử dụng thực tế.

| Trạng thái thiết bị | Hành vi code hiện tại |
| --- | --- |
| Bình thường, silent hoặc vibrate | Dùng luồng âm thanh báo thức; vẫn cần kiểm tra loa và audio focus |
| DND cho phép alarms | Có thể phát; chế độ priority cần DND access để code đọc chính sách |
| DND chặn alarms/total silence | Trả lỗi, không ép phát âm thanh |
| Có DND access nhưng alarms vẫn bị chặn | Chưa đủ; cần cho phép alarms hoặc tắt DND |

App không tự tắt DND. FCM priority HIGH không phải quyền vượt DND. Âm thanh được phát bằng `PdaAlarmService` với `mediaPlayback`, không phải chỉ dựa vào âm báo của notification channel.

Khi lỗi, xem **Last finder audio failure** trong màn hình này. Kiểm tra loa, âm lượng hệ thống/máy tính nếu dùng emulator, thiết bị âm thanh kết nối, cuộc gọi/app khác giữ audio focus và giới hạn MDM. `RINGING` xác nhận kiểm tra phần mềm đã qua, không chứng minh người dùng thực sự nghe thấy.

Code liên quan: [FinderSoundActivity](../pda-android/app/src/main/java/com/company/pda/presentation/pdafinder/FinderSoundActivity.java), [AlarmAudioPolicy](../pda-android/device-android/src/main/java/com/company/device/android/AlarmAudioPolicy.java), [PdaAlarmService](../pda-android/app/src/main/java/com/company/pda/infrastructure/alarm/PdaAlarmService.java).

## 7. Chạy React và kiểm tra toàn bộ luồng

Từ thư mục `pda-web`:

```powershell
npm ci
npm run dev
```

Mở `http://localhost:5173`. Proxy mặc định trỏ tới `http://localhost:8080`; nếu backend ở nơi khác, cấu hình `BACKEND_URL` trong `pda-web/.env.local` rồi khởi động lại Vite. React không cần credential Firebase.

1. Giữ app Android đang mở trong lần thử đầu, hoàn tất đăng ký và test loa.
2. Trên React chọn đúng PDA → **Find**, chưa bấm Stop ngay.
3. Quan sát trạng thái `QUEUED → SENT → RINGING` và nghe âm thanh. Do giao diện refresh định kỳ, có thể không thấy đủ mọi trạng thái trung gian.
4. Bấm Stop trên web hoặc Stop alarm trên PDA; xác nhận chuông dừng.
5. Tạo Find mới để thử khi app ở nền/màn hình khóa, rồi thử DND đã cho phép alarms.

Thời hạn mặc định là 60 giây, có thể đổi bằng `FINDER_TIMEOUT_SECONDS` ở backend. Lệnh đã Stop/hết hạn không được dùng lại. Test notification từ Firebase Console không thay thế bước Find: app cần data message với `command`, `requestId`, `expiresAt` do backend tạo.

## 8. Xác định lỗi theo điểm dừng

| Hiện tượng | Cần kiểm tra |
| --- | --- |
| Không có thiết bị trên React | Backend/proxy, đăng nhập và đăng ký Android, cửa hàng |
| Không có push token | File Android đúng package/project, Google Play services, Internet, APK đã build lại |
| Hai cờ false | Environment variables của đúng tiến trình backend, restart |
| QUEUED mãi dù hai cờ true | Log scheduler/thread dump, transport Firebase, worker có bị kẹt hay không |
| RETRY | Credential/quyền Firebase, mạng backend, lỗi gửi push |
| INVALID_TOKEN / NO_TOKEN | Token cũ/thiếu; mở app và kiểm tra đồng bộ đăng ký/token |
| SENT nhưng không RINGING | Android dùng đúng bản FCM, không Force stop, kết nối Firebase, deadline, lỗi khởi động service; kiểm tra cả đường ACK Android → backend |
| FAILED | DND/allow alarms, audio focus và Last finder audio failure |
| RINGING nhưng không nghe | Test loa cục bộ, âm lượng/đầu ra audio của PDA hoặc máy host emulator |

Nếu mạng API Android bị lỗi, máy có thể đã phát chuông nhưng ACK chưa tới backend, nên không chỉ dựa vào trạng thái trên web. FCM dry-run thành công cũng chỉ xác nhận Firebase chấp nhận yêu cầu, không giao tới PDA.

Luồng code chi tiết: [FCM đến âm thanh PDA](20-fcm-to-pda-alarm-guide.md). Chẩn đoán thực tế và API xóa: [tài liệu 22](22-delete-device-and-finder-troubleshooting.md). Cấu hình transport: [FCM/polling](16-fcm-to-polling-fallback.md).
