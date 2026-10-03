# PDA Finder Web

React + Vite, JavaScript và CSS thuần. Không login, router, Redux hoặc thư viện UI.

## Chạy

Cần Node.js 20+ và backend đang chạy:

```powershell
cd pda-web
npm ci
npm run dev
```

Mở http://localhost:5173. Vite proxy `/web/finder` tới `http://localhost:8080`, nên không cần bật CORS toàn backend. Muốn đổi backend, tạo `.env.local`:

```properties
BACKEND_URL=http://localhost:8080
```

Khởi động lại Vite sau khi đổi. Biến này dành cho proxy Node, không chứa credential.

## Sử dụng

1. Trên PDA, đăng nhập nhân viên/quản lý, hoàn thành form đăng ký xuất hiện tại Home.
2. Trên web, xem tất cả cửa hàng hoặc lọc một cửa hàng. Cửa hàng chưa có thiết bị cũng hiển thị.
3. Bấm **Tìm**. Web cập nhật danh sách/trạng thái khoảng 5 giây một lần; có nút **Làm mới**.
4. Bấm **Dừng** để gửi STOP. Yêu cầu còn hoạt động sẽ khóa nút Tìm để tránh gửi trùng.
5. Nút **Xóa** xóa thiết bị và lịch sử finder sau xác nhận; dừng yêu cầu active hoặc chờ hết hạn trước khi xóa. Mở Home Android để kiểm tra và đăng ký lại.
6. Lỗi mạng/409 được hiển thị trên trang. Thiếu token vẫn cho phép tìm vì PDA có thể đã chọn polling.

Web hiện cờ FCM/scheduler runtime và log gửi gần nhất để chẩn đoán tình trạng không reo; xem [hướng dẫn khắc phục](../docs/22-delete-device-and-finder-troubleshooting.md).

Web gọi API công khai theo yêu cầu; bất kỳ ai truy cập được API đều xem và tìm/dừng thiết bị của mọi cửa hàng. Triển khai trong mạng nội bộ phù hợp. Web không nhận FCM token, device secret hay credential hash.

`hasPushToken` chỉ báo token có trong database, không phải trạng thái online. RINGING là xác nhận phần mềm Android đang phát, không bảo đảm loa đã được nghe thấy. STOPPED sau nút Dừng là backend đã nhận lệnh dừng; PDA offline có thể chưa nhận STOP.

## Build

```powershell
npm run build
npm run preview
```

`dist/` chứa website tĩnh. Preview ở http://localhost:4173 có cùng proxy để thử bản build. Khi deploy thực tế, web server phục vụ `dist/` và reverse proxy `/web/finder/` tới Spring Boot (giữ nguyên đường dẫn). Vite proxy không nằm trong các file tĩnh.

## File chính

- `src/main.jsx`: trang duy nhất, fetch API, nhóm theo cửa hàng, tìm/dừng/xóa, cảnh báo cấu hình và refresh.
- `src/style.css`: giao diện cơ bản và bảng cuộn trên màn hình nhỏ.
- `vite.config.js`: proxy dev/preview.
- `package-lock.json`: khóa phiên bản để `npm ci` dựng lại.

[Luồng và API đầy đủ](../docs/21-react-device-finder.md). [React](https://react.dev/learn), [Vite 6](https://v6.vite.dev/guide/).
