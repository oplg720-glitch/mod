# HƯỚNG DẪN — ADVANCED INVENTORY SAKURA 1.3.0

## 1. Cập nhật giao diện 1.3.0

- Bảng nền Sakura plum-glass với viền vàng hồng và hoa anh đào ở góc.
- Bố cục tách riêng danh mục, tìm kiếm, lưới vật phẩm và bảng xem trước.
- Các ô vật phẩm có trạng thái rê chuột và chọn rõ ràng hơn.
- Cánh hoa chuyển động nhẹ và các điểm sáng trang trí phía sau điều khiển.
- Màn hình enchantment có thẻ riêng cho từng dòng và khu vực thống kê rõ ràng.
- Đây chỉ là cập nhật giao diện; chức năng và cách xử lý vật phẩm được giữ nguyên.

## 2. Tính năng được giữ nguyên từ 1.2.0

- Danh sách enchantment đọc từ registry của thế giới Singleplayer đang mở, không còn chỉ có 18 mục cố định.
- Tự phát hiện enchantment tương thích từ các mod đã cài và dữ liệu registry được đồng bộ.
- Có ô tìm kiếm theo tên hiển thị hoặc ID đầy đủ, ví dụ `minecraft:unbreaking` hay `examplemod:swift_mining`.
- Có phân trang để duyệt danh sách dài.
- Mỗi enchantment có nút bật/tắt và ô nhập cấp độ từ 1 đến 255.
- Có thể chọn nhiều enchantment tiện ích/phòng thủ cùng lúc.
- Thêm phím tắt mở GUI, mặc định là `K`.

## 3. Đổi phím tắt

1. Mở Minecraft.
2. Chọn **Options (Tùy chọn)**.
3. Chọn **Controls (Điều khiển)** → **Key Binds (Tổ hợp phím)**.
4. Tìm nhóm **Advanced Inventory**.
5. Chọn **Open Advanced Inventory**.
6. Nhấn phím mới muốn gán. Nếu phím đang được mod khác sử dụng, chọn một phím khác để tránh xung đột.

Phím mặc định: `K`. Khi đang ở Singleplayer, nhấn `K` để mở; nhấn lại khi GUI Advanced Inventory đang mở để đóng. Lệnh `/inv` vẫn hoạt động.

## 4. Phạm vi enchantment

Mod đọc dữ liệu từ registry để hiển thị enchantment tiện ích, di chuyển, câu cá và phòng thủ, bao gồm các mục từ mod tương thích. Những enchantment tập trung vào tăng sát thương hoặc sử dụng vũ khí/chiến đấu vẫn bị loại khỏi danh sách và bị chặn ở bước tạo vật phẩm.

## 5. Cập nhật source trên GitHub

1. Tải ZIP source 1.3.0 và giải nén.
2. Mở Repository.
3. Chọn **Add file → Upload files**.
4. Upload toàn bộ nội dung bên trong thư mục dự án, giữ nguyên cấp thư mục.
5. Thay thế file cũ khi GitHub báo trùng.
6. Nhấn **Commit changes**.
7. Mở **Actions** để theo dõi workflow build.

## 6. Build trong Codespaces

Mở Codespace, chọn Terminal và chạy:

```bash
gradle --no-daemon clean build
```

Nếu thành công, file JAR nằm trong `build/libs/`. Có thể tải artifact từ GitHub Actions sau khi workflow hoàn tất thành công.

## 7. Trạng thái kiểm thử

Đã thực hiện kiểm tra tĩnh source/resource và JSON. Chưa chạy Gradle build hoặc thử trong client Minecraft tại môi trường đóng gói này; cần xác nhận qua Codespaces/GitHub Actions trước khi cài JAR vào world chính.
