# Thanh menu cố định và vùng an toàn - 24/11/2026

Người dùng yêu cầu cập nhật giao diện: luôn hiện menu ba gạch, tìm kiếm và tài khoản trên đầu trang, không cần kéo/cuộn. Yêu cầu này thay cơ chế Smart Top Bar hai trạng thái trong kế hoạch cũ.

## Nguyên nhân và thay đổi

- MainFragment trước đây ẩn thanh nút mặc định, phụ thuộc scroll/drag để hiện. Khi vùng nội dung bị ẩn do loading/error, không còn đường thao tác để mở menu.
- Insets được cộng vào padding của DrawerLayout gốc, nhưng DrawerLayout bố trí các view con theo margins. Trước sửa, header nằm từ y=0, bị status bar/cutout che.
- Thanh nút trong fragment_main.xml nay luôn visible và ở ngoài vùng cuộn/loading/error. Bỏ các callback ẩn hiện và hiệu ứng co/dịch grid; dùng NestedScrollView thông thường.
- FrameLayout mới bọc DrawerLayout để padding system bars, cutout và IME có hiệu lực. Giữ nguyên ID DrawerLayout và hai NavHost; MainActivity thao tác drawer qua binding.mainDrawerLayout.
- Không thay logic tài khoản, đăng nhập, repository thị trường hay dữ liệu người dùng.

## Kiểm chứng trên máy

- `:app:assembleDebug :app:testDebugUnitTest --offline`: thành công; 14/14 test JVM đạt.
- Cài cập nhật bằng `adb install -r -t`, không gỡ app hay xóa data.
- Trên emulator-5554 (Pixel_10a, Android 17, 1080x2424), thanh nút ở bounds [0,142][1080,310], dưới vùng status/cutout. Nút menu và tài khoản đều ở y=163..289.
- Bấm ☰: mở “BIỂU ĐỒ & DANH MỤC MỞ RỘNG”, có USD/VND và danh sách biểu đồ; Back đóng drawer.
- Bấm biểu tượng người: mở “TÀI KHOẢN CÁ NHÂN”, thấy thông tin và danh sách tùy chọn; Back trở về Main.
- Vuốt nội dung Main: cả menu, tìm kiếm và tài khoản vẫn hiện cùng vị trí.
- Bấm ô tìm kiếm: bàn phím mở; Back đóng bàn phím.
- Không ghi nhận AndroidRuntime error trong log kiểm tra. Không chạy lại toàn bộ instrumentation ở lần sửa UI này.

Thông tin hồ sơ và giá thị trường vẫn dùng mock. Đây là sửa truy cập/hiển thị điều hướng, không phải tích hợp Firebase hoặc giá live. Kết luận trong báo cáo text-market trước đó về menu bị ẩn được thay bằng kết quả kiểm chứng ở tài liệu này.
