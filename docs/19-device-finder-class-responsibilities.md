# Vì sao luồng tìm thiết bị đi qua nhiều class?

Tài liệu giải thích vai trò các class trong chức năng tìm PDA, dựa trên mã nguồn được xem ngày 30/09/2026. Các đề xuất đơn giản hóa ở cuối là đánh giá thiết kế, chưa phải thay đổi đã triển khai.

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

Ngoài FCM, PDA đích còn có đường nhận lệnh qua HTTP polling khi ghi nhận lỗi FCM. Vì vậy, đây không phải một chuỗi gọi hàm chạy liên tục trong cùng một ứng dụng.

## 2. Phía máy người dùng nhấn Find

| Class/interface | Làm gì? | Vì sao tách riêng? |
|---|---|---|
| `PdaFinderActivity` | Hiển thị danh sách, nhận thao tác Find/Stop/Refresh. | Giữ phần giao diện và vòng đời màn hình ở một chỗ. |
| `PdaFinderViewModel` | Xử lý thao tác, gọi repository và cập nhật trạng thái màn hình. | Activity không phải tự quản lý việc tải dữ liệu và kết quả. |
| `AsyncViewModel` | Cung cấp cơ chế chạy công việc nền rồi cập nhật UI. | Dùng chung cách xử lý tác vụ bất đồng bộ cho nhiều màn hình. |
| `PdaFinderUiState` | Chứa danh sách thiết bị và yêu cầu tìm hiện tại. | Gom dữ liệu hiển thị thành một trạng thái rõ ràng. Đây là dữ liệu, không phải một bước xử lý. |
| `PdaFinderRepository` | Khai báo các thao tác tìm, dừng, đăng ký và điều khiển chuông cục bộ. | Bên gọi phụ thuộc hợp đồng, có thể thay implementation hoặc dùng bản giả khi test. |
| `PdaFinderRepositoryImpl` | Gọi API, chuyển DTO thành model, chuyển lệnh chuông cục bộ tới `AlarmController`. | Che giấu chi tiết lấy dữ liệu và gọi hạ tầng khỏi ViewModel. |
| `PdaFinderApi` | Khai báo endpoint HTTP bằng Retrofit. | Tập trung đường dẫn, body và header của API. |

Trong [PdaFinderViewModel.java](../pda-android/app/src/main/java/com/company/pda/presentation/pdafinder/PdaFinderViewModel.java), `find()` gọi trực tiếp repository. Nó không đi qua `StartPdaAlarmUseCase`; use case đó thuộc luồng bật chuông trên PDA đích.

## 3. Backend tiếp nhận và lưu yêu cầu

| Class/interface | Làm gì? | Vì sao tách riêng? |
|---|---|---|
| `PdaFinderController` | Nhận HTTP, kiểm tra dữ liệu đầu vào, yêu cầu quyền manager ở các endpoint quản lý. | Tách giao thức HTTP khỏi xử lý nghiệp vụ. |
| `FinderUseCase` | Hợp đồng các chức năng finder mà controller sử dụng. | Controller không phụ thuộc trực tiếp class service cụ thể. Đây là interface, không phải thêm một đối tượng trung gian thực thi. |
| `PdaFinderService` | Kiểm tra thiết bị thuộc cửa hàng, tạo request/deadline, ghi log, đưa lệnh vào outbox, xử lý stop và trạng thái. | Là nơi điều phối nghiệp vụ và transaction. |
| `CurrentActor` | Cung cấp người dùng và cửa hàng hiện tại. | Service không phải đọc trực tiếp chi tiết Spring Security. |
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
| `FinderPollingService` | Duy trì đường kiểm tra và lấy lệnh qua HTTP. | Có vòng đời và lịch chạy nền riêng, độc lập với một phiên chuông. |
| `FinderPollingCycle` | Quyết định lấy lệnh hay chỉ kiểm tra health, điều chỉnh khoảng chờ. | Tách quyết định polling khỏi Android Service để dễ kiểm thử. |
| `FinderCommandHandler` | Xử lý FIND/STOP; kiểm tra UUID, deadline, lệnh đã xử lý và phiên đang chạy. | FCM và polling dùng cùng quy tắc, tránh xử lý khác nhau hoặc bật chuông trùng. |
| `StartPdaAlarmUseCase` | Chuyển yêu cầu bật chuông tới repository. | Thể hiện thao tác ở tầng domain; hiện chưa có logic riêng đáng kể. |
| `StopPdaAlarmUseCase` | Chuyển yêu cầu dừng chuông tới repository. | Tương tự use case bật chuông. |
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
| `PdaFinderViewModel` | Khi người dùng Refresh, lấy trạng thái từ backend để hiển thị. | Trạng thái trên máy người tìm được cập nhật qua server. |

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

- `StartPdaAlarmUseCase` và `StopPdaAlarmUseCase`: hiện chỉ chuyển tiếp một lời gọi.
- `DeviceModule.alarm()`: chủ yếu bọc lời gọi factory.
- `ZebraAlarmAdapter` và `UrovoAlarmAdapter`: chưa có hành vi khác triển khai Android chung.
- Các bộ DTO/model/entity giống nhau: lợi ích tách lớp cần cân đối với lượng code mapping.

Riêng `PdaFinderRepositoryImpl` đang gánh cả gọi API từ xa và điều khiển chuông cục bộ. Vì thế đoạn `Handler → UseCase → Repository → AlarmController` có phần vòng vèo. Nếu refactor, nên ưu tiên làm rõ hai trách nhiệm này, đồng thời giảm các lớp chỉ chuyển tiếp khi chúng chưa mang lại giá trị cụ thể.

## 9. Tài liệu liên quan

- [Luồng hoạt động tìm thiết bị PDA](14-device-finder-flow.md).
- [Polling dự phòng cho PDA finder](15-finder-polling.md).
- [Finder audio and silent mode](12-finder-audio.md).
