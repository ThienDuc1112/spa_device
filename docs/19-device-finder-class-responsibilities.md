# Vì sao luồng tìm thiết bị đi qua nhiều class?

Tài liệu giải thích vai trò các class trong chức năng tìm PDA, cập nhật theo luồng web React ngày 03/10/2026. Các đề xuất đơn giản hóa ở cuối là đánh giá thiết kế, chưa phải thay đổi đã triển khai.

## 1. Tổng quan

Số lượng class đến từ hai lý do: chức năng đi qua nhiều hệ thống, và dự án chủ động chia nhỏ trách nhiệm theo kiến trúc. Tuy nhiên, không phải lớp nào cũng bắt buộc phải tách riêng ở quy mô hiện tại.

“Tìm thiết bị” ở đây là gửi lệnh để PDA đích phát chuông. Quá trình gồm:

```text
Máy người tìm              Backend                     PDA đích
Nhấn Find ──────────────► Tạo yêu cầu
                         Lưu lệnh chờ gửi
                         Gửi FCM ────────────────────► Nhận lệnh
                                                      Phát chuông
                         Nhận trạng thái ◄──────────── Báo kết quả
Xem trạng thái ◄───────── Trả kết quả
```

PDA đích mặc định nhận lệnh qua FCM. Chỉ khi build/cài APK với `finderTransport=polling` mới nhận qua HTTP; không tự chuyển theo lỗi FCM. Vì vậy, đây không phải một chuỗi gọi hàm chạy liên tục trong cùng một ứng dụng.

## 2. Website gửi lệnh và Android đăng ký

| Thành phần | Trách nhiệm |
| --- | --- |
| React `App` trong [main.jsx](../pda-web/src/main.jsx) | Nhóm thiết bị theo cửa hàng, lọc, gửi Tìm/Dừng, refresh trạng thái, hiện lỗi. |
| `api()` trong React | Gọi nhóm API `/web/finder` bằng fetch; không có token hoặc màn hình login. |
| `vite.config.js` | Proxy dev/preview tới Spring Boot; bản deploy tĩnh dùng reverse proxy. |
| Android `HomeActivity` | Sau login mở form đăng ký nếu chưa có credential; giữ nút đăng ký lại khi bỏ qua. |
| `HomeActivity.register()` | Gọi trực tiếp `PdaFinderApi.register()`, lưu deviceId/secret rồi đồng bộ FCM token. |
| `PdaFinderApi` | API đăng ký, đồng bộ token, nhận lệnh và ACK của PDA. |

Website gửi yêu cầu; Android nhận lệnh qua `FinderCommandHandler` và gọi trực tiếp `AlarmController`. Đã bỏ các lớp UseCase/Repository/ViewModel trung gian của Android. Xem [hướng dẫn React](21-react-device-finder.md).

## 3. Backend tiếp nhận và lưu yêu cầu

| Class/interface | Làm gì? | Vì sao tách riêng? |
|---|---|---|
| `WebFinderController` | API công khai xem toàn hệ thống và tìm/dừng từ React. | Tách endpoint web khỏi các API credential/JWT hiện có. |
| `WebFinderQueries` / `WebFinderMapper` | Đọc cửa hàng, thiết bị và request gần nhất, không trả secret/token. | Projection riêng cho màn hình web. |
| `PdaFinderController` | Nhận HTTP, kiểm tra dữ liệu đầu vào, yêu cầu quyền manager ở các endpoint quản lý. | Tách giao thức HTTP khỏi xử lý nghiệp vụ. |
| `FinderUseCase` | Hợp đồng các chức năng finder mà controller sử dụng. | Controller không phụ thuộc trực tiếp class service cụ thể. Đây là interface, không phải thêm một đối tượng trung gian thực thi. |
| `PdaFinderService` | Kiểm tra thiết bị thuộc cửa hàng, tạo request/deadline, ghi log, đưa lệnh vào outbox, xử lý stop và trạng thái. | Là nơi điều phối nghiệp vụ và transaction. |
| `CurrentActor` | Cung cấp user/store cho đăng ký và API manager cũ; web finder không có actor. | Không giả mạo tài khoản cho yêu cầu công khai. |
| `DeviceRepository` | Tra cứu thiết bị, token và cập nhật thông tin liên quan. | Tách truy cập dữ liệu thiết bị khỏi nghiệp vụ finder. |
| `DeviceUseCase` / `DeviceService` | Xác thực thiết bị bằng ID và secret khi thiết bị lấy lệnh hoặc gửi kết quả. | Dùng chung quy tắc nhận diện thiết bị. |
| `PdaFindRepository` | Hợp đồng lưu và đọc yêu cầu tìm, trạng thái, log. | Nghiệp vụ không gắn trực tiếp với MyBatis. |
| `MyBatisPdaFindRepository` | Triển khai hợp đồng repository bằng MyBatis. | Nối mô hình nghiệp vụ với cách lưu dữ liệu cụ thể. |
| `PdaFindMapper` | Thực thi các thao tác SQL được cấu hình cho MyBatis. | Tập trung chi tiết truy vấn database. |
| `PdaFindRequestEntityMapper` | Chuyển entity lưu trữ thành model nghiệp vụ. | Tránh để cấu trúc lưu trữ lan sang tầng nghiệp vụ. |
| `OperationsRepository` | Ghi outbox và audit, quản lý các lệnh chờ gửi. | Dùng chung cơ chế hàng chờ bền vững và nhật ký thao tác. |

Điểm đáng giữ trong [PdaFinderService.java](../pda-management/src/main/java/com/company/pda/application/pdafinder/service/PdaFinderService.java) là tạo request và ghi lệnh chờ gửi trong cùng transaction. Nếu gửi mạng ngay giữa thao tác này, sẽ khó xử lý trường hợp database lưu thành công nhưng gửi lệnh thất bại.

Các class như `FindPdaRequest`, `FindPdaCommand`, `FindPdaResult`, `FindPdaResponse`, `PdaFindRequestEntity` là các dạng dữ liệu ở từng ranh giới: HTTP, ứng dụng và lưu trữ. Việc tách giúp thay đổi từng phần độc lập, nhưng cũng làm tăng code chuyển đổi khi các cấu trúc gần như giống nhau.

## 4. Backend gửi lệnh tới PDA

| Class/interface | Làm gì? | Vì sao tách riêng? |
|---|---|---|
| `OutboxScheduler` | Định kỳ kích hoạt xử lý lệnh và hết hạn. | Tách lịch chạy khỏi nội dung xử lý. |
| `OutboxProcessor` | Đọc lệnh, kiểm tra hạn/trạng thái/token, gửi push, xử lý retry và hết hạn. | Việc gửi có thể thất bại và cần chạy tiếp sau khi HTTP ban đầu đã kết thúc. |
| `NotificationPort` | Hợp đồng gửi thông báo. | Processor không phải biết Firebase SDK. |
| `FirebaseNotificationAdapter` | Triển khai gửi FCM. | Cô lập payload và xử lý đặc thù của Firebase. |

Đây là phần có lý do thực tế rõ ràng: người dùng tạo yêu cầu thành công không đồng nghĩa PDA đã nhận được lệnh. Outbox giúp lưu công việc cần làm và thử lại.

## 5. PDA đích nhận lệnh và bật chuông

| Class/interface | Làm gì? | Vì sao tách riêng? |
|---|---|---|
| `PdaFirebaseMessagingService` | Nhận message FCM. | Là điểm tiếp nhận của Firebase trên Android. |
| `FinderPollingService` | Chỉ chạy khi cấu hình `polling`; nhận lệnh HTTP, backoff khi lỗi, không kiểm tra FCM health. | Có vòng đời và lịch chạy nền riêng, độc lập với một phiên chuông. |
| `FinderPollingCycle` | Chỉ gọi commands khi được bật bằng cấu hình; nhịp thành công 5 giây. | Tách việc lấy lệnh khỏi Android Service để kiểm thử không có HTTP khi tắt polling. |
| `FinderCommandHandler` | Xử lý FIND/STOP; kiểm tra UUID, deadline, lệnh đã xử lý và phiên đang chạy. | FCM và polling dùng cùng quy tắc, tránh xử lý khác nhau hoặc bật chuông trùng. |
| `AlarmController` | Tạo Intent khởi động service; đánh dấu request đã dừng và dừng đúng phiên. | Gom chi tiết điều khiển Android Service vào một chỗ. |
| `PdaAlarmService` | Quản lý phiên chuông, foreground notification, adapter, kiểm tra âm thanh và báo kết quả. | Chuông cần hoạt động độc lập với màn hình và được giải phóng tài nguyên theo vòng đời service. |
| `AlarmTimeoutManager` | Đặt/hủy lịch dừng theo deadline. | Gom trách nhiệm quản lý timer. |

Ví dụ, [AlarmController.java](../pda-android/app/src/main/java/com/company/pda/infrastructure/alarm/AlarmController.java) lưu dấu `handled:<requestId>` khi nhận STOP. Nếu STOP tới trước FIND, FIND đến muộn sẽ không bật chuông lại.

Việc kiểm tra deadline và dấu đã xử lý xuất hiện ở cả handler, controller và service cũng có mục đích: từ lúc nhận message tới lúc service thực sự chạy, trạng thái có thể đã thay đổi.

## 6. Phần thực sự phát âm thanh

| Class/interface | Làm gì? | Vì sao tách riêng? |
|---|---|---|
| `DeviceModule` | Cung cấp adapter cho ứng dụng. | Gom nơi khởi tạo dependency; hiện là lớp rất mỏng. |
| `DeviceAlarmFactory` | Chọn adapter Zebra, Urovo hoặc Android chung. | Service không cần chứa điều kiện theo hãng thiết bị. |
| `DeviceAlarmProvider` | Khai báo thiết bị được hỗ trợ và cách tạo adapter. | Cho phép mở rộng danh sách adapter. |
| `DeviceAlarmAdapter` | Hợp đồng `start`, `stop`, `check`. | Service chỉ cần biết chức năng chuông, không cần biết cách phát cụ thể. |
| `ZebraAlarmAdapter` | Adapter cho Zebra. | Chừa điểm mở rộng theo hãng; hiện dùng lại triển khai Android chung. |
| `UrovoAlarmAdapter` | Adapter cho Urovo. | Tương tự Zebra, hiện chưa có xử lý âm thanh đặc thù riêng. |
| `AndroidAlarmAdapter` | Phối hợp player, âm lượng và audio focus; kiểm tra lỗi, thu hồi tài nguyên. | Đóng gói một phiên phát âm thanh hoàn chỉnh. |
| `AndroidAlarmPlayer` | Phát/dừng âm thanh và kiểm tra player/đường ra âm thanh. | Tách việc phát media khỏi quản lý âm lượng và chính sách âm thanh. |
| `AndroidVolumeController` | Tăng âm lượng, lưu và khôi phục trạng thái cũ. | Quản lý âm lượng có vòng đời và cơ chế khôi phục riêng. |
| `AndroidAudioModeController` | Quản lý audio focus và kiểm tra điều kiện âm thanh. | Cô lập tương tác với chính sách âm thanh hệ thống. |
| `AlarmAudioPolicy` | Chứa logic đánh giá chính sách âm thanh. | Giúp kiểm thử quy tắc riêng khỏi các API Android. |

Các interface `AlarmPlayer`, `VolumeController`, `AudioModeController` còn cho phép thay bằng bản giả để test [AndroidAlarmAdapter](../pda-android/device-android/src/main/java/com/company/device/android/AndroidAlarmAdapter.java) mà không cần phát loa thật.

`AlarmPolicy`, `AlarmResult`, `AlarmStatus` lần lượt biểu diễn cấu hình, kết quả và trạng thái; chúng không phải những tầng xử lý bổ sung.

## 7. Báo kết quả về máy người tìm

| Class | Làm gì? | Vì sao tách riêng? |
|---|---|---|
| `PendingEvent` cùng lớp truy cập Room | Lưu sự kiện chờ gửi như `RINGING`, `STOPPED`, `FAILED`. | Giữ sự kiện đã lưu khi mạng tạm thời không dùng được. |
| `SyncWorker` | Gửi sự kiện và cập nhật token; thử lại khi lỗi có thể phục hồi. | Việc đồng bộ không phụ thuộc phiên chuông còn chạy hay không. |
| React `App` | Đọc danh sách và request gần nhất mỗi khoảng 5 giây hoặc khi bấm Làm mới. | Website hiển thị ACK Android đã gửi lên server. |

## 8. Đánh giá mức độ cần thiết

Những ranh giới nên giữ:

- UI và gọi API.
- HTTP và nghiệp vụ.
- Nghiệp vụ và database.
- Hàng chờ và gửi FCM.
- Nhận lệnh và quản lý phiên chuông.
- Phiên chuông và âm thanh.
- Sự kiện và đồng bộ mạng.

Mỗi phần có lý do thay đổi và kiểu lỗi khác nhau.

Những phần hiện có thể cân nhắc đơn giản hóa:

- `DeviceModule.alarm()`: chủ yếu bọc lời gọi factory.
- `ZebraAlarmAdapter` và `UrovoAlarmAdapter`: chưa có hành vi khác triển khai Android chung.
- Các bộ DTO/model/entity giống nhau: lợi ích tách lớp cần cân đối với lượng code mapping.

Android đã đơn giản hóa: `HomeActivity` gọi Retrofit để đăng ký; `FinderCommandHandler` gọi trực tiếp `AlarmController.start/stop`. Backend giữ kiến trúc hiện tại. Các Activity/Fragment khác cũng gọi API trực tiếp; `ScreenTasks` chỉ chạy công việc ngoài UI thread và bỏ callback khi màn hình bị hủy.

## 9. Tài liệu liên quan

- [Luồng hoạt động tìm thiết bị PDA](14-device-finder-flow.md).
- [Polling theo cấu hình cho PDA finder](15-finder-polling.md).
- [Finder audio and silent mode](12-finder-audio.md).
