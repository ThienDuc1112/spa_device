# Hướng dẫn cài đặt Android Studio để làm việc với dự án PDA

Hướng dẫn dành cho Windows, đối chiếu cấu hình repository ngày 2026-09-29. Mục tiêu là mở dự án, đồng bộ thư viện, build APK và chạy trên emulator hoặc PDA thật. Tên một số menu có thể khác nhẹ giữa các phiên bản Android Studio.

## 1. Cần cài những gì?

| Thành phần | Có cần không? | Dùng để làm gì? |
| --- | --- | --- |
| Android Studio | Bắt buộc nếu làm việc bằng IDE này | Viết code, thiết kế giao diện, build và debug |
| JDK 21 | Cấu hình đề xuất cho repository này | Chạy Gradle; backend dùng JDK 8 riêng |
| Android SDK Platform 35 | Bắt buộc theo cấu hình hiện tại | Biên dịch app với `compileSdk 35` |
| Android SDK Build-Tools 35.0.0 | Cần cho bộ công cụ AGP hiện tại | Đóng gói và xử lý APK |
| Android SDK Platform-Tools | Cần để kết nối thiết bị | Cung cấp `adb` |
| Android Emulator và một system image | Chỉ cần nếu dùng máy ảo | Chạy app khi không có PDA thật |
| Driver USB phù hợp | Có thể cần trên Windows khi dùng thiết bị thật | Cho ADB nhận điện thoại/PDA |
| Git | Nếu lấy code và quản lý phiên bản bằng Git | Clone, pull, commit |
| Maven và PostgreSQL | Chỉ cần nếu tự chạy backend theo cách native | Chạy Spring Boot và database |
| Docker Desktop | Tùy chọn nếu dùng Docker Compose cho backend | Chạy backend/database trong container |
| Cấu hình Firebase | Chỉ cần khi thử FCM thật | Nhận/gửi push notification |

Không cần cài Gradle toàn máy: repository có `gradlew.bat` để dùng đúng phiên bản. Các thư viện như Retrofit, Room, LiveData, Firebase và Compose được Gradle tải theo file build; không cài từng thư viện bằng bộ cài riêng.

## 2. Cài Android Studio

1. Tải bộ cài Windows từ [trang Android Studio chính thức](https://developer.android.com/studio).
2. Chạy bộ cài, sau đó mở Android Studio và hoàn tất Setup Wizard.
3. Để wizard cài Android SDK; bổ sung các phiên bản cần cho dự án ở bước 4 bên dưới.

Ưu tiên bản Stable có hỗ trợ AGP của dự án. Kiểm tra yêu cầu hệ điều hành, RAM và dung lượng trên [hướng dẫn cài đặt chính thức](https://developer.android.com/studio/install). Máy chạy thêm emulator cần tài nguyên nhiều hơn máy chỉ chạy IDE; nếu máy hạn chế tài nguyên, có thể dùng PDA thật thay emulator.

## 3. Cấu hình JDK cho Gradle

Repository khuyến nghị JDK 21 trong [README Android](../pda-android/README.md). Backend yêu cầu Java 8; chọn JDK riêng khi chạy Maven. Mức ngôn ngữ Java của source Android vẫn là 17; đây là hai cấu hình khác nhau.

Mở:

```text
File → Settings → Build, Execution, Deployment → Build Tools → Gradle
```

Ở **Gradle JDK**, chọn JDK 21 đã có, hoặc dùng **Download JDK** để tải JDK 21. Nếu IDE dùng cấu hình **Gradle Daemon JVM criteria**, chọn JVM 21 ở cấu hình tương ứng. Không cần đổi runtime chạy chính Android Studio. Xem [cách Android Studio chọn JDK](https://developer.android.com/build/jdks).

Đối với PowerShell, đặt `JAVA_HOME` trỏ tới thư mục JDK thực tế. Ví dụ dưới đây chỉ áp dụng cho cửa sổ terminal đang mở; thay đường dẫn cho đúng máy:

```powershell
$env:JAVA_HOME = 'C:\Java\jdk-21'
& "$env:JAVA_HOME\bin\java.exe" -version
```

Không trỏ `JAVA_HOME` tới thư mục `bin`. Khi dùng giao diện Run/Build, IDE chọn Gradle JDK của nó; khi dùng terminal thông thường, Gradle Wrapper dùng `JAVA_HOME` hoặc Java trên PATH. Nên cấu hình hai cách chạy cùng JDK để tránh kết quả khác nhau.

AGP 8.9 yêu cầu tối thiểu JDK 17 và Gradle 8.11.1. Dùng JDK 21 ở đây là lựa chọn theo repository, không có nghĩa mọi dự án Android đều bắt buộc Java 21. [Bảng tương thích AGP 8.9](https://developer.android.com/build/releases/agp-8-9-0-release-notes).

## 4. Cài Android SDK bằng SDK Manager

Mở **Tools → SDK Manager**; ở màn hình chào có thể mở qua **More Actions → SDK Manager**.

Trong **SDK Platforms**, chọn **Android 15 / API Level 35**.

Trong **SDK Tools**, chọn các mục cần dùng:

```text
Android SDK Build-Tools → 35.0.0
Android SDK Platform-Tools
Android SDK Command-line Tools (latest)  [tiện khi dùng sdkmanager]
Android Emulator                       [nếu chạy máy ảo]
```

Bật **Show Package Details** nếu cần chọn đúng phiên bản Build-Tools. Nhấn Apply và đọc/chấp nhận license để cài. NDK/CMake không nằm trong yêu cầu build Java/XML thông thường của dự án này. [Hướng dẫn SDK Manager](https://developer.android.com/studio/intro/update#sdk-manager).

Ghi lại **Android SDK Location**. Đường dẫn Windows thường có dạng:

```text
C:\Users\<ten-user>\AppData\Local\Android\Sdk
```

Trong `pda-android/local.properties`, có thể cấu hình bằng dấu `/` để dễ đọc:

```properties
sdk.dir=C:/Users/<ten-user>/AppData/Local/Android/Sdk
```

Thay `<ten-user>` bằng tên user thật. Android Studio thường tạo file này khi mở dự án. Không sao chép đường dẫn của máy khác và không commit `local.properties` vào Git.

Nếu dùng nhiều công cụ dòng lệnh, có thể đặt biến môi trường `ANDROID_HOME` bằng SDK Location và thêm `%ANDROID_HOME%\platform-tools` vào PATH. Chỉ khai báo một đường dẫn SDK nhất quán với `local.properties`; không cần tạo các biến `ANDROID_USER_HOME` hoặc `ANDROID_SDK_HOME` cho cách cài đặt thông thường.

## 5. Mở đúng thư mục dự án

Tại Android Studio, chọn **Open**, rồi mở:

```text
D:\studying\JAVA\design pattern\psa device\pda-android
```

Đây là thư mục chứa:

```text
settings.gradle
build.gradle
gradlew.bat
app/
scanner-api/
device-api/
...
```

`pda-management` là dự án backend Maven, không phải thư mục Android cần mở. Nếu Android Studio hỏi tin cậy dự án, xác nhận khi bạn tin cậy nguồn code đang mở.

Chờ **Gradle Sync** hoàn tất. Lần đầu cần Internet để tải Gradle Wrapper, plugin và dependency. Không bật chế độ offline trước khi đã có đủ dependency trong cache. Nếu cần chạy lại, chọn **File → Sync Project with Gradle Files**.

Cấu hình đang khai báo trong repository:

| Mục | Giá trị |
| --- | --- |
| Android Gradle Plugin | 8.9.1 |
| Gradle Wrapper | 8.11.1 |
| `compileSdk` / `targetSdk` | 35 / 35 |
| `minSdk` | 26, tương ứng Android 8.0 |
| Java source/target compatibility | 17 |
| JDK dùng để build theo README | 21 |
| Application ID | `com.company.pda` |
| AndroidX Activity | 1.10.1 cho cả `activity`, `activity-ktx`, `activity-compose` |
| AndroidX Lifecycle | 2.8.7 |
| Compose BOM | 2025.03.00 cho app và instrumentation test |

Nguồn: [build.gradle cấp project](../pda-android/build.gradle), [build.gradle module app](../pda-android/app/build.gradle), [Gradle Wrapper](../pda-android/gradle/wrapper/gradle-wrapper.properties).

Các phiên bản AndroidX/Compose trên được chọn để dùng với SDK 35/AGP 8.9.1. Bộ Activity 1.13.0, Lifecycle 2.11.0 và Compose BOM 2026.02.01 trước đó kéo dependency yêu cầu SDK 36–37/AGP 9.1. Sau khi nhận thay đổi, chạy **Sync Project with Gradle Files**, rồi build lại. Khi thêm hoặc nâng thư viện, kiểm tra `:app:checkDebugAarMetadata`; cài lại Android Studio không tự sửa được sự không tương thích giữa dependency và SDK/AGP.

## 6. Chọn chạy bằng emulator hoặc PDA thật

### Cách A: Android Emulator

Trên Windows, bật ảo hóa CPU trong BIOS/UEFI (Intel VT-x hoặc AMD SVM/AMD-V). Sau đó mở **Turn Windows features on or off**, bật **Windows Hypervisor Platform**, rồi khởi động lại nếu được yêu cầu. Google hướng dẫn dùng WHPX cho emulator trên Windows. [Thiết lập tăng tốc emulator](https://developer.android.com/studio/run/emulator-acceleration).

Trong Android Studio:

1. Mở **Tools → Device Manager** hoặc **View → Tool Windows → Device Manager**.
2. Chọn **Create Virtual Device** và một mẫu điện thoại.
3. Chọn system image API 35 để thử cùng mức target của app; tải image phù hợp kiến trúc máy.
4. Chọn image có **Google Play** nếu cần thử FCM/Google Play services.
5. Hoàn tất tạo AVD rồi nhấn nút Play để khởi động.

SDK Platform dùng để biên dịch và system image dùng để chạy emulator là hai gói khác nhau. [Hướng dẫn tạo AVD](https://developer.android.com/studio/run/managing-avds).

### Cách B: PDA hoặc điện thoại thật

Thiết bị cần đáp ứng `minSdk 26` của app. Trên thiết bị, bật Developer options và **USB debugging**, kết nối bằng cáp có truyền dữ liệu, rồi chấp nhận hộp thoại cho phép máy tính debug. Trên Windows, cài driver ADB/OEM nếu thiết bị chưa được nhận. Khi thiết bị xuất hiện trong danh sách chạy, chọn nó và nhấn Run. [Hướng dẫn thiết bị thật](https://developer.android.com/studio/run/device).

Kiểm tra kết nối trong terminal nếu đã thêm Platform-Tools vào PATH:

```powershell
adb devices
```

Hoặc gọi trực tiếp bằng SDK Location:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

Nếu thấy `unauthorized`, mở khóa thiết bị và chấp nhận quyền debug. Nếu không thấy thiết bị, kiểm tra cáp, USB debugging và driver. PDA doanh nghiệp có thể cần quản trị viên EMM cho phép debug.

## 7. Build và chạy app lần đầu

Trong terminal tại thư mục `pda-android`, chạy:

```powershell
.\gradlew.bat --version
.\gradlew.bat :app:assembleDebug
```

Lệnh đầu giúp kiểm tra Gradle/JVM thực sự được dùng. Lệnh sau tạo APK debug tại:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Trong Android Studio, chọn run configuration **app**, chọn thiết bị rồi nhấn **Run**. Nếu đã có thiết bị kết nối và muốn cài bằng terminal:

```powershell
.\gradlew.bat :app:installDebug
```

App hiện khai báo `LoginActivity` là launcher. Vì vậy mở màn hình Login là hành vi dự kiến, không phải lỗi cài đặt Android Studio. Có backend hoạt động và tài khoản phù hợp mới đăng nhập và thử được các chức năng có gọi API.

Kiểm tra unit test/lint khi cần:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug
```

## 8. Kết nối app với backend

Build APK không yêu cầu backend đang chạy. Đăng nhập, danh sách PDA, tồn kho và các chức năng dùng API thì cần backend/database hoạt động.

Nếu tự chạy backend bằng Java, cài JDK 8, Maven và PostgreSQL rồi làm theo [README backend](../pda-management/README.md) và [hướng dẫn triển khai](11-deployment.md). Nếu dùng backend đã triển khai sẵn, không cần cài PostgreSQL/Maven trên máy Android Studio. Nếu dùng Docker Compose, làm theo cấu hình `.env` và Docker trong tài liệu triển khai.

### Emulator kết nối tới backend trên máy tính

App debug mặc định dùng:

```text
http://10.0.2.2:8080/
```

`10.0.2.2` là địa chỉ đặc biệt của Android Emulator để truy cập loopback của máy host. `localhost` trong emulator là chính emulator, không phải máy tính chạy Spring Boot. [Tài liệu mạng emulator](https://developer.android.com/studio/run/emulator-networking).

### PDA thật kết nối qua LAN

Ví dụ máy tính có IP LAN `192.168.1.100`:

```powershell
.\gradlew.bat :app:installDebug '-PapiUrl=http://192.168.1.100:8080/'
```

PDA cần truy cập được mạng đó; backend cần lắng nghe trên địa chỉ truy cập được từ LAN và firewall cần cho phép cổng tương ứng. File Compose hiện publish `127.0.0.1:8080:8080`, nên cách này chưa cho PDA từ LAN vào trực tiếp; cần cấu hình địa chỉ publish phù hợp trong môi trường thử nghiệm hoặc dùng USB reverse bên dưới.

### PDA thật kết nối qua USB

Khi ADB đã nhận thiết bị, có thể dùng:

```powershell
adb reverse tcp:8080 tcp:8080
.\gradlew.bat :app:installDebug '-PapiUrl=http://127.0.0.1:8080/'
```

Cách này chuyển cổng trên thiết bị về máy phát triển qua ADB. Nếu có nhiều thiết bị, chọn serial bằng `adb -s <serial> reverse tcp:8080 tcp:8080` và chọn đúng thiết bị cài app. Tham khảo [hướng dẫn ADB reverse của Android Developers](https://developer.android.com/develop/ui/views/layout/webapps/access-local-server?hl=vi).

Giá trị `apiUrl` cần dấu `/` cuối URL. Đây là giá trị build-time, nên đổi URL phải build/cài lại APK. Tham số `-PapiUrl` ở terminal chỉ áp dụng cho lần build đó, không tự đổi cấu hình Run của Android Studio. Muốn dùng cùng URL khi build từ IDE, có thể đặt `apiUrl=...` trong file Gradle properties cá nhân của user, rồi Sync và build lại; tránh commit IP riêng của máy vào cấu hình chung.

Bản debug có manifest cho phép HTTP để thử nghiệm. Khi build release, truyền URL HTTPS của backend triển khai và cấu hình chữ ký phát hành theo [README Android](../pda-android/README.md).

## 9. Firebase có bắt buộc không?

Không cần cấu hình Firebase chỉ để cài Android Studio, mở dự án hoặc build app. Module app chỉ áp dụng Google Services plugin khi có file cấu hình client.

Để thử push FCM thật, cần:

- Firebase project đăng ký package `com.company.pda`.
- File client `google-services.json` đặt tại `pda-android/app/`.
- Backend bật FCM và có credential Firebase Admin phù hợp.
- Thiết bị/system image hỗ trợ Google Play services cho luồng FCM hiện tại.

Chi tiết cấu hình nằm trong [hướng dẫn triển khai](11-deployment.md). Cơ chế polling có thể hoạt động khi FCM không được cấu hình; xem [giải thích fallback FCM → polling](16-fcm-to-polling-fallback.md). Trong cấu hình dev hiện tại, scheduler backend bị tắt; cần bật theo hướng dẫn triển khai khi kiểm thử đầy đủ gửi push và xử lý hết hạn.

## 10. Tạo màn hình Java sau khi cài xong

Để tiếp tục theo các màn hình Java/XML của app, chọn package trong module `app`, dùng **New → Activity → Empty Views Activity**, rồi chọn **Source Language / Language = Java** nếu wizard cung cấp lựa chọn này.

Mẫu Compose như **Empty Activity** tạo code Kotlin. Việc cài JDK 21 không đổi ngôn ngữ của template sang Java. Compose dùng Kotlin cho phần giao diện. [Thiết lập Compose và lựa chọn ngôn ngữ](https://developer.android.com/develop/ui/compose/setup).

Sau khi tạo, vẫn cần nối Activity với ViewModel, Data Binding và repository theo cấu trúc của app; wizard không tự hoàn thành luồng nghiệp vụ MVVM.

## 11. Lỗi thường gặp

| Hiện tượng | Kiểm tra đầu tiên |
| --- | --- |
| `SDK location not found` | SDK Location và `pda-android/local.properties` |
| Thiếu `android-35` hoặc Build-Tools | SDK Manager, chọn đúng package/version |
| `JAVA_HOME is not set` hoặc JDK quá cũ | Gradle JDK trong IDE và `JAVA_HOME` trong terminal |
| `Unsupported class file major version` | JDK thực tế trong `gradlew.bat --version` và tương thích Gradle/plugin |
| Không resolve được plugin/dependency | Internet, proxy nếu có, chế độ offline và phiên bản dependency được khai báo |
| `checkDebugAarMetadata` yêu cầu SDK/AGP cao hơn | Tương thích phiên bản thư viện; không chỉ cài thêm SDK rồi giữ nguyên mọi cấu hình |
| Emulator không khởi động hoặc rất chậm | Ảo hóa CPU, WHPX và tài nguyên máy; thử PDA thật |
| `adb` không được nhận là lệnh | Platform-Tools chưa ở PATH; gọi bằng đường dẫn đầy đủ |
| Thiết bị `unauthorized` | Mở khóa thiết bị, chấp nhận quyền USB debugging |
| App mở Login nhưng đăng nhập lỗi mạng | Backend, API URL, firewall và kết nối thiết bị |
| FCM không hoạt động | Firebase client/backend và Google Play services; không phải chỉ việc cài IDE |
| Activity mới sinh `.kt` | Kiểm tra template Compose/Views và mục Language |

Khi báo lỗi, lấy thông báo lỗi đầu tiên trong Build Output hoặc Logcat. Phân biệt lỗi môi trường cài đặt, lỗi dependency/code và lỗi kết nối backend để xử lý đúng bước.

## 12. Danh sách tự kiểm tra

- [ ] Android Studio đã cài và mở được thư mục `pda-android`.
- [ ] Gradle dùng JDK 21 theo cấu hình dự án.
- [ ] SDK Platform 35, Build-Tools 35.0.0 và Platform-Tools đã cài.
- [ ] Gradle Sync hoàn tất hoặc đã xác định rõ lỗi tương thích dependency cần xử lý.
- [ ] `:app:assembleDebug` tạo được APK.
- [ ] Emulator hoặc PDA thật đã được nhận và cài/chạy được app.
- [ ] Nếu thử chức năng có API: backend hoạt động, URL đúng và có tài khoản đăng nhập.
- [ ] Nếu thử FCM: client/backend đã cấu hình Firebase theo tài liệu triển khai.

Tài liệu này ghi hướng dẫn cài đặt và thông số đang khai báo; không thực hiện cài phần mềm hay thay đổi cấu hình máy tự động.
