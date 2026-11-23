# Advanced Inventory — Sakura Edition (Minecraft 26.1.2 Fabric)

## Bản cập nhật 1.3.0 — Sakura UI Refresh

Bản 1.3.0 chỉ làm mới giao diện Sakura; toàn bộ cơ chế Singleplayer, chọn vật phẩm, enchantment, số lượng, lệnh và phím tắt từ 1.2.0 được giữ nguyên.

### Điểm mới

- **Giao diện Sakura được làm mới:** bảng nền plum-glass, viền vàng hồng, hoa anh đào ở góc và các điểm sáng trang trí.
- **Bố cục rõ ràng hơn:** tách vùng danh mục, tìm kiếm, lưới vật phẩm và bảng xem trước; màn hình enchantment có thẻ riêng cho từng dòng.
- **Chuyển động trang trí:** cánh hoa trôi và ánh sáng nhịp nhẹ; hiệu ứng nằm phía sau các nút và ô nhập.
- **Trạng thái trực quan:** ô vật phẩm đang chọn/đang rê chuột, thông tin số lượng và số enchantment được nhấn mạnh hơn.
- **Chỉ cập nhật UI:** không thay đổi cách hoạt động của `/inv`, phím tắt K, registry item/enchantment, giới hạn số lượng hoặc cơ chế thả vật phẩm.

### Chức năng được giữ nguyên từ 1.2.0

- **Registry enchantment động:** khi mở màn hình enchantment, mod đọc registry enchantment đã được đồng bộ vào thế giới hiện tại. Vì vậy danh sách không còn bị khóa ở 18 enchantment viết thủ công.
- **Enchantment từ mod:** các enchantment tương thích có ID trong registry được hiển thị cùng namespace của mod.
- **Tìm kiếm và phân trang:** tìm theo tên hiển thị hoặc ID dạng `modid:enchantment`; danh sách có phân trang để không tràn màn hình.
- **Cấp độ tùy chỉnh:** mỗi mục có ô nhập cấp độ 1–255; nhiều enchantment có thể được chọn cùng lúc.
- **Phím tắt:** mặc định là `K`. Có thể đổi trong **Options → Controls → Key Binds → Advanced Inventory → Open Advanced Inventory**. Nhấn phím khi đang ở trong thế giới Singleplayer để mở; nhấn lại khi GUI đang mở để đóng.
- **Giữ `/inv`:** lệnh vẫn hoạt động như trước.

### Phạm vi enchantment

Bộ chọn hiển thị các enchantment tiện ích, di chuyển, câu cá và phòng thủ được phát hiện trong registry, bao gồm những mục từ mod tương thích. Các enchantment chuyên tăng sát thương hoặc phục vụ vũ khí/chiến đấu bị lọc khỏi giao diện và bị từ chối ở bước tạo item. Bộ lọc dựa trên ID và các từ khóa đã biết; mod bên thứ ba có ID không mô tả rõ chức năng có thể cần được rà soát riêng.

### Các tính năng trước đó được giữ lại

- Registry item của Minecraft và các mod Fabric đã cài.
- Tab Tất cả / Vật phẩm mod / Minecraft và các danh mục khác.
- Sakura UI, cánh hoa chuyển động và màu sắc đồng bộ.
- Nhập số lượng, tự chia stack hợp lệ.
- Thả item thành Item Entity trong thế giới, không đưa thẳng vào inventory/hotbar.
- Xử lý trên Integrated Server; không yêu cầu cài mod lên Dedicated Server.

## Build trên GitHub Codespaces

Dự án dùng Java 25 và Gradle theo cấu hình trong `.devcontainer/devcontainer.json`.

1. Upload hoặc thay thế toàn bộ nội dung dự án trong Repository, giữ nguyên cấu trúc thư mục gốc.
2. Mở **Code → Codespaces** và tạo/mở Codespace.
3. Trong Terminal chạy:

```bash
gradle --no-daemon clean build
```

4. JAR được tạo trong `build/libs/`.
5. Hoặc mở tab **Actions**, chọn workflow **Build Fabric mod**, sau khi chạy thành công tải artifact `advanced-inventory-fabric-26.1.2`.

## Cài đặt và kiểm thử

- Minecraft Java 26.1.2, Fabric Loader phù hợp và Fabric API đúng phiên bản.
- Cài JAR vào thư mục `mods` của profile Singleplayer.
- Thử `/inv` và phím tắt mặc định `K`.
- Vào **Options → Controls → Key Binds** để đổi phím theo ý muốn.
- Sao lưu world trước khi thử mod mới.

## Trạng thái build

Giao diện và asset đã được cập nhật. Đã rà soát cấu trúc source, các file JSON và metadata. Môi trường tạo ZIP này không có Java 25/Gradle tương thích để biên dịch, vì vậy **chưa xác nhận build thành công và chưa tạo JAR đã kiểm thử**. Cần chạy Codespaces hoặc GitHub Actions để xác nhận compile thực tế.
