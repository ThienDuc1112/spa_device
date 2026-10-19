# 24. Android gọi service trực tiếp từ Activity/Fragment

Android hiện không dùng ViewModel, UseCase hoặc Repository để chuyển tiếp nghiệp vụ. Mỗi màn hình gọi HTTP API hoặc service Android trực tiếp. Backend giữ nguyên kiến trúc và API.

| Thao tác | Nơi đọc/sửa code |
| --- | --- |
| Đăng nhập, lưu token, mở Home | [LoginActivity.login()](../pda-android/app/src/main/java/com/company/pda/presentation/auth/LoginActivity.java) → `authApi.login()` |
| Đăng ký PDA, lưu deviceId/secret, kiểm tra đăng ký đã bị xóa | [HomeActivity](../pda-android/app/src/main/java/com/company/pda/presentation/home/HomeActivity.java) → `finderApi.register()` / `fcmHealth()` |
| Quét barcode, lấy sản phẩm và cache offline | [ProductFragment](../pda-android/app/src/main/java/com/company/pda/presentation/product/ProductFragment.java) → `ScannerManager`, `productsApi.product()`, Room |
| Đọc/điều chỉnh tồn kho | [InventoryFragment](../pda-android/app/src/main/java/com/company/pda/presentation/inventory/InventoryFragment.java) → `inventoryApi` |
| Danh sách/chi tiết/tạo/xác nhận/hủy disposal | [DisposalFragment](../pda-android/app/src/main/java/com/company/pda/presentation/disposal/DisposalFragment.java) → `disposalsApi` |
| Nhận FCM hoặc polling, bật/dừng chuông | [FinderCommandHandler](../pda-android/app/src/main/java/com/company/pda/infrastructure/firebase/FinderCommandHandler.java) → `AlarmController` → `PdaAlarmService` |

## Ví dụ đăng ký thiết bị

Trong `HomeActivity.register(String code, String name)`, sau kiểm tra dữ liệu:

```java
HttpReceiver<PdaFinderDto.Registration> httpReceiver = registration -> {
  tasks.clearRetry();
  modules.fcm.initialize();
  FinderPollingService.start(getApplication());
  tasks.status.setValue("Device registered");
};
tasks.runAsync("Registering device?", () ->
    modules.finderApi.register(new PdaFinderDto.Register(
        code.trim(), name.trim(), modules.tokens.get("fcmToken")))
        .thenApplyAsync(registration -> {
          modules.device.registered(registration.deviceId, registration.deviceSecret);
          return registration;
        }, modules.io),
    httpReceiver);
```

`FinderPollingService.start()` chỉ khởi động nếu APK cấu hình polling; FCM vẫn là mặc định. `callApi(method, path, body, type, headers, executor)` gửi HTTP trên executor; kết quả đi qua `ScreenTasks` tới `BaseActivity.httpReceiver()` trên UI thread rồi mới gọi callback đã đăng ký. `LoginActivity` và `HomeActivity` kế thừa `BaseActivity` qua `AlarmAwareActivity`; Fragment dùng Activity chủ và vòng đời của view.

## Các tiện ích còn giữ

- `AppModule`: khởi tạo và giữ HTTP API client, database, preferences, `DeviceManager`, `AlarmController`, `FcmTokenManager`. Không chứa workflow màn hình.
- `HttpReceiver<T>`: callback được khai báo tại Activity/Fragment. `onSuccess(T)` nhận dữ liệu trên UI thread; ghi đè `onError(Exception)` và trả `true` nếu đã xử lý lỗi, hoặc trả `false` để dùng thông báo lỗi chung.
- `ScreenTasks`: chạy công việc nền, báo busy/error/retry và bỏ callback UI khi Activity hoặc view của Fragment đã bị hủy. Không chứa logic đăng ký, tồn kho hay disposal.
- `ApiCalls`: kiểm tra HTTP response và chuyển lỗi API thành thông báo chung.
- `TokenStorage`, Room, `ApiCalls`: tiếp tục lưu token/credential, refresh JWT, cache và đồng bộ ACK.
- Module scanner/audio theo hãng: giữ để làm việc với thiết bị thật; màn hình gọi manager trực tiếp.

API đồng bộ `ApiCalls.execute(...)` và `Request.execute()` đã được bỏ; dùng `ApiCalls.callApi(method, path, body, type, headers, executor)` trả về `CompletableFuture`. Refresh token và retry một lần khi gặp 401 vẫn chạy trong tác vụ nền. Không tự phát lại thao tác ghi khi xoay màn hình; nút Retry trong cùng màn hình giữ request ID đã tạo cho inventory/disposal. Sau khi rời màn hình trong lúc gửi, tải lại dữ liệu trước khi thao tác tiếp vì server có thể đã xử lý dù callback UI cũ bị bỏ.

`HomeActivity` lưu danh sách barcode, sản phẩm đang chọn và màn hình hiện tại bằng `onSaveInstanceState`. `DisposalFragment` lưu trang/chi tiết rồi tải lại khi tạo view. Scanner bắt đầu ở `onResume`, dừng ở `onPause`; callback cũ không được dùng để tra sản phẩm sau khi màn hình ngừng hoạt động.

Luồng chuông vẫn kiểm tra request ID, deadline, dấu đã xử lý, DND và audio focus. Việc bỏ các lớp trung gian không cấp thêm quyền vượt DND và không tự chuyển FCM sang polling.

Thiết lập Firebase/âm thanh: [tài liệu 23](23-firebase-and-android-sound-setup.md). Luồng nhận lệnh: [tài liệu 20](20-fcm-to-pda-alarm-guide.md).

Với một request đơn, dùng `tasks.request("Loading inventory...", modules.inventoryApi.inventory(code), httpReceiver)`. Với chuỗi API, dùng `tasks.runAsync(label, () -> future, httpReceiver)`, nối request bằng `thenCompose` và chạy Room/preferences bằng `thenApplyAsync` hoặc `handleAsync` với `modules.io`. `tasks.run` dành cho công việc nền cục bộ. Callback chỉ đến khi màn hình STARTED và bị bỏ khi Activity/view của Fragment đã hủy. Nút Retry giữ nguyên request và callback. Worker/polling đã chạy trên luồng nền nên có thể chờ future tuần tự; màn hình không chờ future trên UI thread.
