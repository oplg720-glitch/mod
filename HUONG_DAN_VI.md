# Advanced Inventory — Hướng dẫn tiếng Việt

## Bản hiện tại

Đây là dự án mã nguồn Fabric cho Minecraft Java **26.1.2**, thiết kế **chỉ chạy ở Singleplayer**. Mod dùng lệnh client `/inv`; khi tạo đồ, mã chuyển công việc sang Integrated Server của thế giới đơn để vật phẩm được thả thành Item Entity thật trong thế giới. Không cần cài mod lên Dedicated Server.

Bản khởi đầu có:

- GUI duyệt vật phẩm từ registry, gồm tìm kiếm theo tên/ID và các nhóm All, Blocks, Food, Tools, Redstone, Miscellaneous.
- Ô nhập số lượng, nút tăng/giảm và nút MAX.
- Tự chia thành các stack theo giới hạn stack của vật phẩm.
- Nút thả vật phẩm xuống đất; không đưa vào inventory hoặc hotbar.
- Màn hình enchantment tiện ích/phòng thủ, nhập level từ 1 đến 255.
- Giới hạn entity để tránh tạo quá nhiều vật phẩm cùng lúc.

Các vật phẩm hướng chiến đấu và enchantment thiên về tấn công không nằm trong danh sách của bản này.

## Build trực tuyến bằng GitHub Codespaces

1. Tạo một Repository GitHub trống.
2. Giải nén ZIP dự án trên máy tính.
3. Mở Repository → **Add file → Upload files**.
4. Kéo toàn bộ nội dung bên trong thư mục dự án đã giải nén vào trang Upload files. Phải upload các tệp `build.gradle`, `settings.gradle`, `gradle.properties`, `src`, `.devcontainer` và `.github` ở thư mục gốc Repository.
5. Nhấn **Commit changes**.
6. Chọn **Code → Codespaces → Create codespace on main**.
7. Đợi Codespaces đọc `.devcontainer/devcontainer.json` và cài Java 25 cùng Gradle 9.4.1.
8. Mở Terminal và chạy:

```bash
gradle --no-daemon clean build
```

Nếu build thành công, JAR sẽ nằm trong `build/libs/`.

Có thể thay bước Terminal bằng cách mở tab **Actions** của Repository. Workflow **Build Fabric mod** sẽ tự chạy sau khi push/commit. Khi trạng thái xanh, mở lần chạy đó và tải artifact `advanced-inventory-fabric-26.1.2`.

## Cài vào Minecraft

1. Dùng profile Minecraft 26.1.2 có Fabric Loader.
2. Cài Fabric API đúng phiên bản tương thích 26.1.2.
3. Chép JAR mod vào thư mục `mods` của profile.
4. Vào thế giới Singleplayer và gõ `/inv` trong chat.

## Thông số giới hạn

- Giới hạn tổng: tối đa 4.096 vật phẩm mỗi yêu cầu.
- Giới hạn số entity: tối đa 128 stack/entity mỗi yêu cầu.
- Vì vậy, giới hạn thực tế phụ thuộc vào max stack size của vật phẩm. Ví dụ vật phẩm không stack được có giới hạn 128 mỗi lần; nút MAX sẽ tự tính con số tương ứng.
- Enchantment được ghi vào data component `ENCHANTMENTS`; tuy nhiên bản mã nguồn này chưa được xác nhận qua build và kiểm thử trong game.

## Lưu ý về trạng thái build

Môi trường tạo dự án hiện tại chỉ có Java 21, trong khi Minecraft 26.1.2 cần Java 25. Vì vậy chưa thể build JAR tại đây và không được xem ZIP này là JAR đã biên dịch. Hãy chạy build trong Codespaces hoặc GitHub Actions; nếu xuất hiện lỗi, gửi nội dung tab **Actions → Build Fabric mod → Build mod** để tiếp tục sửa.
