# 22. Xóa thiết bị và xử lý tình trạng web tìm nhưng PDA không kêu

Thiết lập từ đầu: [Cấu hình Firebase, backend và Android để phát âm thanh](23-firebase-and-android-sound-setup.md).

## API xóa một thiết bị

```http
DELETE /web/finder/devices/123
```

Không cần body/JWT, cùng phạm vi công khai của website. API trả:

- `204`: đã xóa thiết bị, các request tìm, alert log và outbox FIND/STOP liên quan trong một transaction. Giữ audit chung và thêm `DEVICE_DELETE_WEB` với actor null, store/id thiết bị tương ứng.
- `404`: không còn thiết bị với ID đó.
- `409`: còn request QUEUED/SENT/RINGING chưa hết hạn; dừng hoặc chờ hết hạn rồi xóa.

Không có API xóa toàn bộ hàng loạt. Nút **Xóa** trên React yêu cầu xác nhận, khóa khi request đang active. Những thiết bị khác không bị ảnh hưởng. Xóa không bảo đảm dừng chuông tại PDA offline; dùng nút dừng tại PDA hoặc chờ timeout nếu cần. Outbox STOP chưa gửi của thiết bị bị xóa cũng bị dọn.

Code: [WebFinderController](../pda-management/src/main/java/com/company/pda/presentation/rest/pdafinder/WebFinderController.java) → [DeviceService.deleteFromWeb](../pda-management/src/main/java/com/company/pda/application/device/service/DeviceService.java) → [DeviceMapper.xml](../pda-management/src/main/resources/mapper/DeviceMapper.xml). Không cần migration mới cho chức năng xóa.

## Đăng ký lại Android sau khi xóa

Cài APK mới rồi mở Home. `HomeActivity.checkRegistration()` gọi `/pda/fcm-health` bằng credential thiết bị một lần khi Home resume để kiểm tra đăng ký:

- HTTP 401: xóa cặp deviceId/deviceSecret cũ nếu chúng vẫn khớp cặp đã kiểm tra, dừng service polling/chuông cũ, hiện form Register.
- Thành công: giữ đăng ký, kể cả response báo FCM_DISABLED; tình trạng FCM không quyết định thiết bị đã đăng ký hay chưa.
- Lỗi mạng/5xx/403: giữ credential, hiện lỗi để thử lại; không coi mất mạng là thiết bị đã bị xóa.

FCM token và mã tài sản gợi ý được giữ; sau đăng ký lại sẽ có deviceId/secret mới. Có thể dùng lại mã và token đã được giải phóng bởi API xóa. Lần kiểm tra Home này không phải polling lệnh hoặc cơ chế tự chuyển transport.

## Nguyên nhân đã quan sát trong database dev

Lượt kiểm tra chỉ đọc ngày 03/10/2026: request từ website lúc 18:04 có QUEUED/STOP_REQUESTED, không có PUSH_FIND; outbox tương ứng có `attempts=0`, `processed_at=null`. Cấu hình dev khi đó tắt scheduler, trong khi Android mặc định FCM. Như vậy lệnh chưa đi tới bước gửi FCM; token tồn tại trong database không đủ làm PDA kêu.

Đã đổi `application-dev.yml` thành `scheduler-enabled: ${APP_SCHEDULER_ENABLED:true}`. **Cần khởi động lại backend** để áp dụng. FCM vẫn cần bật rõ bằng `FCM_ENABLED=true` và credential Admin hợp lệ; không tự chuyển Android sang polling để che lỗi cấu hình.

## Chạy backend với FCM

`pda-android/app/google-services.json` là cấu hình Android, không phải credential Firebase Admin của server. Lấy service-account JSON từ Firebase project tương ứng theo [hướng dẫn Firebase Admin](https://firebase.google.com/docs/admin/setup): Project settings → Service accounts → Generate new private key. Giữ file ngoài source/APK.

Sau khi dừng backend cũ ở IDE, dùng Java 8 và chạy từ workspace:

```powershell
mvn -f pda-management/pom.xml package
powershell -NoProfile -ExecutionPolicy Bypass -File .\pda-management\run-fcm.ps1 -CredentialPath 'C:\secrets\service-account.json'
```

`ExecutionPolicy Bypass` chỉ áp dụng cho tiến trình PowerShell chạy script, không sửa policy toàn máy. Script kiểm tra loại file và project khớp `google-services.json` nếu có, rồi bật `FCM_ENABLED`, `APP_SCHEDULER_ENABLED` và `GOOGLE_APPLICATION_CREDENTIALS`. Không in private key. Script không kiểm chứng quyền Firebase trước khi chạy; lỗi quyền/mạng vẫn phải xử lý khi gửi.

Nếu chạy từ IDE, đặt environment variables cho chính Run Configuration backend:

```text
FCM_ENABLED=true
APP_SCHEDULER_ENABLED=true
GOOGLE_APPLICATION_CREDENTIALS=C:\secrets\service-account.json
```

Đường dẫn là ví dụ, thay bằng file thực. Biến môi trường ở terminal khác không tự truyền vào tiến trình IDE đang chạy. Không chạy hai backend cùng cổng 8080.

## Kiểm tra trên React

### Hai cờ true nhưng lệnh vẫn QUEUED: kiểm tra transport Firebase

Kiểm tra thực tế ngày 03/10/2026 sau khi bật FCM: thread `scheduling-1` bị chờ tại `BasicFuture.get()` → `ApacheHttp2Request.execute()` → `FirebaseMessaging.send()`. Hai lần lấy thread dump vẫn thấy cùng điểm chờ. Scheduler đã chạy nhưng bị giữ trong lần gửi trước, nên chưa xử lý các lệnh mới; cờ `schedulerEnabled=true` không phải kiểm tra sức khỏe worker.

`FirebaseConfig` đã chỉ định `NetHttpTransport` cho Firebase Admin thay cho transport Apache HTTP/2 mặc định, giữ timeout kết nối/đọc 5 giây và Java 8. Không thay đổi transport nhận lệnh của Android: vẫn FCM. Firebase hỗ trợ cấu hình transport qua [`FirebaseOptions.Builder.setHttpTransport`](https://github.com/firebase/firebase-admin-java/blob/main/src/main/java/com/google/firebase/FirebaseOptions.java).

Đã chạy unit test backend và kiểm tra FCM `send(message, true)` (dry-run) bằng credential hiện có và token của thiết bị ID 1: Firebase chấp nhận yêu cầu. Dry-run không giao message xuống Android, không xác nhận loa đã kêu. Không lưu key/token vào tài liệu.

Sau sửa code, dừng và chạy lại backend trong IntelliJ, chờ `Started PdaApplication`, rồi tạo Find mới. Request cũ đã Stop/hết hạn không dùng để kiểm tra lại. Kỳ vọng `PUSH_FIND`/`SENT`, sau đó Android ACK `RINGING`; nếu chỉ đến SENT thì tiếp tục kiểm tra Android.

`GET /web/finder/configuration` trả các cờ runtime, không trả credential:

```json
{"fcmEnabled":true,"schedulerEnabled":true}
```

Web hiện cảnh báo riêng nếu FCM hoặc scheduler tắt. Hai cờ true chỉ chứng minh cấu hình đang bật, chưa chứng minh push giao được. Danh sách có `lastEvent/lastEventMessage` của request gần nhất:

| Log/trạng thái | Kiểm tra tiếp |
| --- | --- |
| QUEUED mãi, scheduler false | Bật scheduler, restart backend |
| RETRY, FCM false | Bật FCM và cung cấp Firebase Admin credential |
| RETRY, hai cờ true | Quyền Firebase, kết nối mạng, project Admin/client, cấu hình SDK |
| NO_TOKEN / INVALID_TOKEN | Đăng ký/cập nhật token PDA; token cũ không còn dùng được |
| PUSH_FIND / SENT | Backend đã gửi; kiểm tra Android nhận message, foreground-service restrictions và deadline |
| FAILED | Kiểm tra Finder sound settings, DND, audio focus, volume/loa trên Android |
| RINGING nhưng không nghe | Trạng thái phần mềm không chứng minh nghe được; test loa thực tế |

Sau khởi động lại, tạo **yêu cầu Tìm mới**: lệnh cũ đã hết hạn sẽ không được phát muộn. Giữ app Android đã mở ít nhất một lần sau cài/force-stop và test loa tại Finder sound settings. [Luồng âm thanh đầy đủ](20-fcm-to-pda-alarm-guide.md).

Thay đổi đã có test database giả lập và Chrome với API giả. Chưa xác nhận giao FCM thật nếu chưa có credential Admin; không xóa thiết bị thật trong quá trình kiểm thử. [Kết quả kiểm tra](verification.md).
