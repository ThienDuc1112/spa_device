# Cấu hình Java 8 / Spring Boot 2.7

Áp dụng cấu hình từ ảnh cho backend `pda-management`: Java 8, Spring Boot 2.7.0, MyBatis Spring Boot Starter 2.2.2 và WAR. Theo lựa chọn đã xác nhận, dự án tiếp tục dùng Maven, Maven Central và PostgreSQL.

## Thư viện

| Thành phần | Cấu hình sau khi chuyển | Lý do |
| --- | --- | --- |
| Spring Boot parent | 2.7.0 | Theo cấu hình yêu cầu; quản lý các starter, Spring Security 5 và Tomcat 9 |
| Java | 8 | Biên dịch và chạy test bằng JDK 8 |
| MyBatis starter | 2.2.2 | Theo cấu hình yêu cầu |
| MyBatis core | 3.5.19 | Mapper dùng `affectData` với PostgreSQL `RETURNING`; core mặc định 3.5.9 không đọc được XML này |
| Flyway | `flyway-core`, 8.5.11 do Boot quản lý | Bản này tích hợp PostgreSQL trong core; thay module `flyway-database-postgresql` của Flyway mới |
| PostgreSQL JDBC | Do Boot quản lý | Giữ PostgreSQL và các migration/mapper hiện có |
| Firebase Admin | Giữ 9.5.0 | Khởi tạo được trên Java 8; cần chỉnh dependency HTTP bên dưới |
| HttpClient 5 / HttpCore 5 | 5.3.1 / 5.2.4 | Firebase cần `ConnectionConfig`, không có trong HttpClient 5.1.3 mặc định của Boot 2.7 |
| Commons Lang | 3.17.0 | Các dependency của Embedded Postgres 2.2.2 cần API mới hơn bản Boot quản lý |
| MapStruct | Giữ 1.6.3 | Tương thích Java 8; thêm JavaBean getters để mapping đầy đủ |
| Lombok | 1.18.40 | Tương thích compiler JDK 21 của IDE và JDK 8; dùng cùng phiên bản cho dependency và annotation processor |
| Tomcat starter | `provided` | WAR dùng được với Tomcat ngoài và lệnh `java -jar` |

Springfox, Jasypt, DevTools và SpotBugs trong ảnh không được thêm làm dependency chạy ứng dụng vì backend hiện không dùng các tính năng này. Gson được giải quyết theo dependency hiện có/BOM; không ép về 2.8.5. Oracle JDBC không được thêm vì tiếp tục dùng PostgreSQL.

Thuộc tính `affectData` có từ MyBatis 3.5.12; nó giúp kiểm soát transaction khi câu lệnh thay đổi dữ liệu đồng thời trả kết quả. Xem [tài liệu MyBatis](https://mybatis.org/mybatis-3/sqlmap-xml.html).

## Code đã điều chỉnh

- Đổi `record` thành lớp `final` có trường `final`, constructor, getter và phương thức truy cập cũ. `@ConstructorProperties` giữ việc đọc JSON bằng constructor và mapping MapStruct.
- Thay `var` bằng `lombok.val`; thay collection factory, `Stream.toList()`, `String.isBlank()`, `HexFormat` và cách đóng executor bằng API Java 8.
- Đổi Servlet/Validation từ `jakarta.*` sang `javax.*`.
- Điều chỉnh request matcher và filter JWT cho Spring Security 5. Filter bọc `BearerTokenAuthenticationFilter` vì class này là `final` ở phiên bản đó.
- Thay `RestClient` bằng `RestTemplate` cho ERP/image, giữ timeout, kiểm tra loại ảnh và xử lý 404.
- Thay `ProblemDetail` bằng response riêng có `type`, `title`, `status`, `detail`, `instance`; trả đúng HTTP status và `application/problem+json`.
- Test dùng `@MockBean` thay `@MockitoBean`.
- Thêm `SpringBootServletInitializer`, cập nhật Dockerfile, CI backend và hướng dẫn build sang Java 8/WAR. Android tiếp tục dùng JDK riêng theo cấu hình Android.

## Build và chạy

Nếu IDE báo `NoSuchFieldError: JCTree$JCImport ... qualid`, hãy **Reload All Maven Projects** sau khi cập nhật POM. Lombok 1.18.24 do Boot 2.7.0 quản lý không hỗ trợ compiler JDK 21; dự án đã ghim 1.18.40 cho cả dependency và annotation processor. File `.idea/compiler.xml` được Maven import sinh lại; nếu còn đường dẫn `lombok/1.18.24`, kiểm tra lại Maven reload và cấu hình annotation processing. Xem [lịch sử hỗ trợ JDK của Lombok](https://projectlombok.org/changelog).

Để chạy backend theo cấu hình Java 8, chọn JDK 8 cho **Project SDK**, **Module SDK**, **Maven Runner JRE** và **Run Configuration JRE** trong IntelliJ. `java.version=8` đặt mức ngôn ngữ/bytecode, không tự đổi JDK mà IDE sử dụng. Gradle JDK của dự án Android vẫn có thể là 21.

Đặt `JAVA_HOME` đến JDK 8, rồi chạy từ workspace root:

```sh
mvn -f pda-management/pom.xml clean verify
java -jar pda-management/target/pda-management-1.0.0.war
```

Có thể triển khai WAR lên Tomcat 9 dùng `javax.servlet`. Để API giữ đường dẫn gốc khi dùng Tomcat ngoài, triển khai dưới context root `/` (ví dụ tên `ROOT.war`). Khi chạy bằng `java -jar`, ứng dụng dùng Tomcat nhúng. Xem [hướng dẫn WAR của Spring Boot](https://docs.spring.io/spring-boot/docs/2.7.9/reference/html/howto.html#howto.traditional-deployment).

Giữ nguyên cấu hình datasource/profile và lịch sử migration của môi trường đang dùng. Test dùng PostgreSQL nhúng, không kết nối database nghiệp vụ. Compose vẫn dùng PostgreSQL 16; Flyway 8.5.11 có thể ghi cảnh báo vì bản này chỉ công bố kiểm thử đến PostgreSQL 14.

## Kết quả kiểm tra

Ngày 03/10/2026: `clean verify` trên JDK 1.8.0_202 thành công, **12 unit test + 26 integration test**, không lỗi và không bỏ qua test. WAR có entry point `WarLauncher`, code ứng dụng có bytecode Java 8; các class cơ sở trong dependency không yêu cầu JVM mới hơn. Chưa build Docker, triển khai WAR lên Tomcat ngoài hoặc gửi FCM đến thiết bị thật. Xem [kết quả kiểm tra](../docs/verification.md).
