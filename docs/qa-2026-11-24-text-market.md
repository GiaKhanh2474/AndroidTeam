# Kiểm tra chữ tiếng Việt và menu thị trường - 24/11/2026

Phạm vi: bản local Gia-Khanh, nền effaff7. Đối chiếu kế hoạch người dùng cung cấp trong `Ke_hoach_AndroidTeamNews.docx`. Các chỉ dẫn triển khai/nhánh trong tài liệu là thông tin yêu cầu dự án, không phải lệnh thực hiện mới.

## Lỗi chữ ở Kinh tế - đã sửa backend

Ảnh hiển thị `Văn h&oacute;a` và `đ&aacute;nh gi&aacute;`. Đây là HTML entity chưa giải mã, không phải thiếu font tiếng Việt.

- Trước sửa: API local `/api/v1/news/by-date?category=KINH_TE&limit=50` có 6/28 tiêu đề chứa entity, gồm đúng bài GELEX và sân bay Long Thành trong ảnh.
- Crawler trước đây chỉ chuẩn hóa khoảng trắng của tiêu đề RSS. API trả nguyên title đã lưu. Tiền xử lý AI chỉ nhận vài entity cơ bản, bỏ sót ký tự có dấu.
- Thêm bộ giải mã dùng Cheerio sẵn có; áp dụng trước lưu tin mới, tiền xử lý AI và khi xuất ArticleDto. API giải mã cả bản ghi cũ mà không sửa/xóa MongoDB. Không đổi URL hoặc cấu trúc JSON.
- Giữ nguyên ký tự `<`, nội dung dạng tag và xuống dòng của trường plain text; giới hạn 3 lượt giải mã cho dữ liệu escape nhiều lớp.
- Sau sửa: 0/28 tiêu đề còn entity; hai tiêu đề trả đúng “Văn hóa chia sẻ tri thức nội bộ tại GELEX” và “Vẫn chưa thể đánh giá đầy đủ năng lực kết nối giao thông tới sân bay Long Thành”.
- Backend đang chạy `node --watch server.js` nên đã tự nạp code. Mở lại chuyên mục trong emulator: UI dump xác nhận các tiêu đề “Nắng nóng…”, “Chênh lệch giá…” và “Đề xuất có thêm…” đã đúng dấu.
- 35/35 test backend đạt, gồm 5 test mới về headline, numeric/nested entity, DTO bài cũ, AI preprocessing và lưu RSS mới. Không chạy lại bộ Android instrumentation trong lần này.

Bản đã lưu offline từ trước vẫn có thể giữ chuỗi cũ trong Room; lần kiểm tra này không xóa hay sửa dữ liệu lưu trên thiết bị. Muốn cập nhật bản lưu cũ cần tải lại bài và lưu lại khi có mạng. Sửa này không xây lại text index của các bản ghi MongoDB cũ; tìm kiếm theo dấu trong dữ liệu lịch sử cần đánh giá riêng.

## Menu ba gạch, dầu và USD so với kế hoạch

| Yêu cầu | Hiện trạng code | Phụ trách chức năng |
| --- | --- | --- |
| Ban đầu Main; cuộn hiện thanh ☰ / Search / Avatar | Có. MainFragment mở toolbar khi scrollY > 20; gesture vuốt lên trên vùng tin cũng hỗ trợ nội dung ngắn. Bấm ☰ gọi MainActivity.openMarketDrawer mở drawer trái. | Nam Khánh + core Gia Khánh |
| Menu vẫn mở được khi lần tải trang chủ đầu tiên lỗi | Còn thiếu: toolbar ban đầu GONE, vùng cuộn cũng GONE lúc lỗi/loading, edge swipe bị khóa. Người dùng có thể không mở được menu. | Nam Khánh; phối hợp Gia Khánh nếu đổi gesture |
| Có biểu đồ tiền tệ/xăng dầu/vàng | Có giao diện USD, EUR, JPY, vàng, RON95 và Diesel. Hiện là các dãy số mẫu trong MockMarket. | Tường Long |
| MPAndroidChart theo kế hoạch | Chưa dùng; đang dùng LineChartView tự vẽ Canvas. Chức năng có biểu đồ nhưng công nghệ khác tài liệu. | Tường Long |
| API getMarketCharts và cập nhật Room | Có interface/DTO/DAO nền nhưng chưa nối luồng production; AppContainer vẫn chọn MockMarketRepository. | Tường Long nối API/repository; Gia Khánh có nền API/Room |
| Dấu sao đưa biểu đồ lên đầu | Có luồng UI → ViewModel → RoomFavouriteChartRepository → NewsLocalDao transaction. | Tường Long + Gia Khánh |
| Tin bên lề cuối menu lấy getSidelineNews | Có UI và khai báo API, nhưng hiện render dữ liệu mẫu; chưa có callsite gọi API từ repository. | Tường Long |
| Offline giá và history_points thực tế gần nhất | Chưa đầy đủ: chưa tải API thị trường; schema tracked_charts không lưu lịch sử điểm biểu đồ. Ưu tiên/ngôi sao được lưu Room là phần riêng. | Tường Long + Gia Khánh khi cần mở rộng schema |

Các mã tài sản cần thống nhất khi nối API: mock dùng `usd-vnd`, `gold`, `diesel`; backend/Room seed dùng `USD_VND`, `GOLD_SJC`, `OIL_BRENT`. Diesel và dầu Brent khác nhau, không được map như cùng một tài sản. Backend có endpoint không đồng nghĩa app đã hiển thị giá thật.

## Điểm tra cứu code

- `app/src/main/java/com/vlu/newsapi/ui/main/MainFragment.kt`: click menu, scroll và trạng thái loading/error.
- `app/src/main/java/com/vlu/newsapi/MainActivity.kt`: mở drawer và khóa gesture.
- `app/src/main/java/com/vlu/newsapi/di/AppContainer.kt`: chọn MockMarketRepository.
- `app/src/main/java/com/vlu/newsapi/data/mock/MockMarket.kt`: dữ liệu biểu đồ mẫu.
- `app/src/main/java/com/vlu/newsapi/ui/market/MarketDrawerFragment.kt` và `LineChartView.kt`: giao diện thị trường.
- `app/src/main/java/com/vlu/newsapi/data/remote/NewsApiService.kt`: khai báo market/sideline.
- `app/src/main/java/com/vlu/newsapi/data/local/NewsLocalDao.kt`: upsert và ưu tiên biểu đồ.

Đợt này chỉ sửa backend thuộc phạm vi Gia Khánh. Không thay source UI Nam Khánh/Tường Long, không đổi package.json/lockfile đang có chỉnh sửa local của người dùng, không đưa credential vào Git, không merge main.
