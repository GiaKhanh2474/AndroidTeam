/**
 * seed/seedData.js — nạp dữ liệu mẫu tiếng Việt cho AndroidTeamNews.
 *
 *   npm run seed
 *
 * Script XÓA dữ liệu cũ của collection `articles` và `market_charts` rồi nạp mới
 * (không đụng tới `users` và `crawl_logs`).
 *
 * LƯU Ý: đây là dữ liệu mẫu để test giao diện — nội dung bài báo là giả lập, original_url chỉ là link minh họa,
 * giá trong market_charts không phải giá thị trường thật.
 */
require("dotenv").config();
const mongoose = require("mongoose");

const Article = require("../src/models/Article.model");
const MarketChart = require("../src/models/MarketChart.model");
const { buildDateLabel } = require("../src/utils/vnDate");

// ---------------------------------------------------------------------------
// BÀI BÁO: 5 Kinh tế + 5 Chính trị + 5 Công nghệ + 4 Bên lề = 19 bài
//   - Mỗi chuyên mục chính: 3 bài ngày 26/10/2026 (is_main_featured: true) + 2 bài ngày 25/10/2026
//   - main_grid_summary: 15–20 từ · card_summary: 35–50 từ · date_label tính theo giờ Việt Nam
// ---------------------------------------------------------------------------
const ARTICLE_SEEDS = [
  {
    article_id: "art_kt_01",
    title: "Ngân hàng Nhà nước giữ nguyên lãi suất điều hành quý 4/2026",
    main_grid_summary: "Lãi suất điều hành tiếp tục ổn định nhằm hỗ trợ doanh nghiệp phục hồi sản xuất cuối năm.",
    card_summary: "Ngân hàng Nhà nước quyết định duy trì mức lãi suất điều hành hiện tại nhằm tạo điều kiện cho các ngân hàng thương mại giảm lãi suất cho vay, thúc đẩy tăng trưởng tín dụng.",
    full_content: "Ngân hàng Nhà nước quyết định duy trì mức lãi suất điều hành hiện tại nhằm tạo điều kiện cho các ngân hàng thương mại giảm lãi suất cho vay, thúc đẩy tăng trưởng tín dụng.\n\nTheo các chuyên gia, việc giữ nguyên lãi suất điều hành giúp ổn định mặt bằng lãi suất và tỷ giá trong bối cảnh nhu cầu vốn của doanh nghiệp tăng vào cuối năm.\n\nGiới phân tích cho rằng điều kiện then chốt để lãi suất cho vay giảm là các ngân hàng kiểm soát tốt chi phí vốn và nợ xấu.",
    image_url: "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3",
    original_url: "https://vnexpress.net/kinh-doanh-bai-1.html",
    source_name: "VnExpress",
    category: "KINH_TE",
    published_at: "2026-10-26T07:00:00Z",
    ai_score: 96,
    is_main_featured: true,
  },
  {
    article_id: "art_kt_02",
    title: "Giá vàng miếng SJC đi ngang sau tuần biến động mạnh",
    main_grid_summary: "Giá vàng miếng SJC đi ngang sau tuần dao động mạnh, nhà đầu tư chuyển sang thận trọng.",
    card_summary: "Giá vàng miếng SJC kết thúc tuần trong biên độ hẹp sau nhiều phiên tăng giảm đan xen. Giới phân tích cho rằng tâm lý chờ đợi thông tin từ thị trường vàng thế giới và chính sách tiền tệ khiến nhà đầu tư hạn chế giải ngân mới.",
    full_content: "Giá vàng miếng SJC kết thúc tuần trong biên độ hẹp sau nhiều phiên tăng giảm đan xen. Chênh lệch giữa giá mua vào và bán ra tại các hệ thống kinh doanh lớn vẫn duy trì ở mức tương đối ổn định.\n\nGiới phân tích cho rằng thị trường đang chờ tín hiệu mới từ giá vàng thế giới và các quyết định chính sách tiền tệ, nên nhà đầu tư cá nhân hạn chế giải ngân mạnh.\n\nCác chuyên gia khuyến nghị người dân cân nhắc kỹ mục tiêu đầu tư, tránh vay mượn để mua vàng và theo dõi nguồn thông tin chính thức trước khi ra quyết định.",
    image_url: "https://picsum.photos/seed/art_kt_02/1200/675",
    original_url: "https://cafef.vn/gia-vang-mieng-sjc-di-ngang-sau-tuan-bien-artkt02.html",
    source_name: "CafeF",
    category: "KINH_TE",
    published_at: "2026-10-26T05:30:00Z",
    ai_score: 92,
    is_main_featured: true,
  },
  {
    article_id: "art_kt_03",
    title: "Xuất khẩu nông sản tăng trưởng nhờ nhu cầu từ các thị trường châu Á",
    main_grid_summary: "Xuất khẩu nông sản quý 4 khởi sắc nhờ đơn hàng gia tăng từ các thị trường châu Á.",
    card_summary: "Kim ngạch xuất khẩu nhiều mặt hàng nông sản chủ lực tăng tích cực trong tháng 10 nhờ nhu cầu nhập khẩu cao từ các đối tác châu Á. Doanh nghiệp đang đẩy mạnh chế biến sâu để nâng giá trị và hạn chế rủi ro biến động giá.",
    full_content: "Kim ngạch xuất khẩu nhiều mặt hàng nông sản chủ lực ghi nhận mức tăng tích cực trong tháng 10, nhờ nhu cầu nhập khẩu cao từ các đối tác châu Á. Gạo, cà phê, hạt điều và rau quả là những nhóm hàng có đơn hàng tăng rõ rệt.\n\nDoanh nghiệp đang đẩy mạnh chế biến sâu, nâng cao chất lượng và truy xuất nguồn gốc để đáp ứng tiêu chuẩn khắt khe hơn của thị trường nhập khẩu.\n\nTheo các hiệp hội ngành hàng, việc đa dạng hóa thị trường và ký kết hợp đồng dài hạn sẽ giúp hạn chế rủi ro biến động giá trong thời gian tới.",
    image_url: "https://picsum.photos/seed/art_kt_03/1200/675",
    original_url: "https://tuoitre.vn/xuat-khau-nong-san-tang-truong-nho-nhu-cau-artkt03.html",
    source_name: "Tuổi Trẻ",
    category: "KINH_TE",
    published_at: "2026-10-26T03:15:00Z",
    ai_score: 88,
    is_main_featured: true,
  },
  {
    article_id: "art_kt_04",
    title: "Giá xăng RON 95-III giảm nhẹ trong kỳ điều hành mới",
    main_grid_summary: "Giá xăng RON 95-III giảm nhẹ trong kỳ điều hành mới, giảm bớt áp lực chi phí vận chuyển.",
    card_summary: "Giá bán lẻ xăng dầu vừa được điều chỉnh, trong đó xăng RON 95-III giảm nhẹ so với kỳ trước. Mức giảm được kỳ vọng sẽ hỗ trợ doanh nghiệp vận tải và giúp kiềm chế đà tăng chi phí logistics trong giai đoạn cao điểm cuối năm.",
    full_content: "Giá bán lẻ xăng dầu được điều chỉnh trong kỳ điều hành mới, trong đó xăng RON 95-III giảm nhẹ so với kỳ trước, còn các mặt hàng dầu có mức tăng giảm không đồng đều.\n\nMức giảm được kỳ vọng sẽ hỗ trợ doanh nghiệp vận tải và giúp kiềm chế đà tăng chi phí logistics trong giai đoạn cao điểm cuối năm.\n\nCơ quan quản lý cho biết sẽ tiếp tục theo dõi sát diễn biến giá dầu thế giới để điều hành linh hoạt, bảo đảm ổn định thị trường trong nước.",
    image_url: "https://picsum.photos/seed/art_kt_04/1200/675",
    original_url: "https://vnexpress.net/gia-xang-ron-95-iii-giam-nhe-trong-ky-artkt04.html",
    source_name: "VnExpress",
    category: "KINH_TE",
    published_at: "2026-10-25T08:00:00Z",
    ai_score: 78,
    is_main_featured: false,
  },
  {
    article_id: "art_kt_05",
    title: "Chứng khoán giữ thanh khoản ổn định, nhóm ngân hàng dẫn dắt thị trường",
    main_grid_summary: "Chứng khoán giữ thanh khoản ổn định, nhóm cổ phiếu ngân hàng dẫn dắt đà tăng nhẹ của thị trường.",
    card_summary: "Các chỉ số chính đóng cửa trong sắc xanh nhẹ với thanh khoản duy trì ở mức ổn định so với các phiên trước. Cổ phiếu ngân hàng và bất động sản thu hút dòng tiền, trong khi nhà đầu tư nước ngoài tiếp tục giao dịch thận trọng.",
    full_content: "Các chỉ số chính đóng cửa trong sắc xanh nhẹ, thanh khoản duy trì ở mức ổn định so với các phiên trước. Nhóm cổ phiếu ngân hàng và bất động sản thu hút dòng tiền, tạo lực đỡ cho thị trường chung.\n\nKhối ngoại tiếp tục giao dịch thận trọng với giá trị mua và bán gần như cân bằng trong phiên.\n\nGiới phân tích nhận định thị trường nhiều khả năng dao động trong biên độ hẹp khi nhà đầu tư chờ thêm thông tin về kết quả kinh doanh quý 3.",
    image_url: "https://picsum.photos/seed/art_kt_05/1200/675",
    original_url: "https://cafef.vn/chung-khoan-giu-thanh-khoan-on-dinh-nhom-ngan-artkt05.html",
    source_name: "CafeF",
    category: "KINH_TE",
    published_at: "2026-10-25T04:40:00Z",
    ai_score: 76,
    is_main_featured: false,
  },
  {
    article_id: "art_ct_01",
    title: "Quốc hội thảo luận dự thảo luật về chuyển đổi số trong cơ quan nhà nước",
    main_grid_summary: "Dự thảo luật chuyển đổi số: trọng tâm thảo luận là dữ liệu dùng chung và an toàn thông tin.",
    card_summary: "Các đại biểu cho ý kiến về dự thảo luật quy định cơ sở dữ liệu dùng chung, chuẩn hóa dịch vụ công trực tuyến và bảo vệ dữ liệu cá nhân. Nhiều ý kiến đề nghị bổ sung lộ trình triển khai cụ thể.",
    full_content: "Trong phiên thảo luận, các đại biểu cho ý kiến về dự thảo luật quy định việc xây dựng cơ sở dữ liệu dùng chung, chuẩn hóa dịch vụ công trực tuyến và bảo vệ dữ liệu cá nhân trong hoạt động của cơ quan nhà nước.\n\nNhiều ý kiến đề nghị bổ sung lộ trình triển khai cụ thể, bảo đảm nguồn lực và đào tạo đội ngũ cán bộ để chuyển đổi số không chỉ dừng ở mức hình thức.\n\nCơ quan soạn thảo sẽ tiếp thu, chỉnh lý dự thảo trước khi trình thông qua ở các kỳ họp tiếp theo.",
    image_url: "https://picsum.photos/seed/art_ct_01/1200/675",
    original_url: "https://tuoitre.vn/quoc-hoi-thao-luan-du-thao-luat-ve-chuyen-artct01.html",
    source_name: "Tuổi Trẻ",
    category: "CHINH_TRI",
    published_at: "2026-10-26T06:45:00Z",
    ai_score: 94,
    is_main_featured: true,
  },
  {
    article_id: "art_ct_02",
    title: "Chính phủ đẩy mạnh cải cách thủ tục hành chính theo hướng trực tuyến toàn trình",
    main_grid_summary: "Nhiều thủ tục hành chính chuyển sang trực tuyến toàn trình, giúp giảm thời gian và chi phí.",
    card_summary: "Các bộ, ngành rà soát và cắt giảm thành phần hồ sơ, đồng thời mở rộng dịch vụ công trực tuyến toàn trình trên cổng quốc gia. Mục tiêu là rút ngắn thời gian giải quyết và hạn chế việc người dân phải đi lại nhiều lần.",
    full_content: "Các bộ, ngành đang rà soát và cắt giảm thành phần hồ sơ, đồng thời mở rộng dịch vụ công trực tuyến toàn trình trên cổng dịch vụ công quốc gia.\n\nMục tiêu là rút ngắn thời gian giải quyết và hạn chế tình trạng người dân, doanh nghiệp phải đi lại nhiều lần giữa các cơ quan.\n\nBên cạnh đó, các địa phương được yêu cầu tăng cường hướng dẫn người dân sử dụng định danh điện tử và thanh toán trực tuyến khi thực hiện thủ tục.",
    image_url: "https://picsum.photos/seed/art_ct_02/1200/675",
    original_url: "https://thanhnien.vn/chinh-phu-day-manh-cai-cach-thu-tuc-hanh-artct02.html",
    source_name: "Thanh Niên",
    category: "CHINH_TRI",
    published_at: "2026-10-26T04:30:00Z",
    ai_score: 90,
    is_main_featured: true,
  },
  {
    article_id: "art_ct_03",
    title: "Việt Nam thúc đẩy hợp tác kinh tế số với các đối tác trong khu vực ASEAN",
    main_grid_summary: "Việt Nam và ASEAN thúc đẩy hợp tác kinh tế số, thương mại điện tử, thanh toán xuyên biên giới.",
    card_summary: "Tại cuộc họp cấp bộ trưởng, các nước thành viên thảo luận về kết nối hệ thống thanh toán, thống nhất tiêu chuẩn dữ liệu và thuận lợi hóa thương mại điện tử. Việt Nam đề xuất hỗ trợ doanh nghiệp nhỏ và vừa tham gia chuỗi giá trị số.",
    full_content: "Tại cuộc họp cấp bộ trưởng khu vực, các nước thành viên thảo luận về việc kết nối hệ thống thanh toán, thống nhất tiêu chuẩn dữ liệu và thuận lợi hóa thương mại điện tử.\n\nViệt Nam đề xuất tăng cường hỗ trợ doanh nghiệp nhỏ và vừa tham gia chuỗi giá trị số, đồng thời chia sẻ kinh nghiệm về bảo vệ người tiêu dùng trên môi trường trực tuyến.\n\nCác bên thống nhất xây dựng kế hoạch hành động chung và sẽ đánh giá tiến độ tại hội nghị tiếp theo.",
    image_url: "https://picsum.photos/seed/art_ct_03/1200/675",
    original_url: "https://dantri.com.vn/viet-nam-thuc-day-hop-tac-kinh-te-so-artct03.html",
    source_name: "Dân Trí",
    category: "CHINH_TRI",
    published_at: "2026-10-26T02:10:00Z",
    ai_score: 88,
    is_main_featured: true,
  },
  {
    article_id: "art_ct_04",
    title: "Địa phương thí điểm mô hình chính quyền số ở cấp xã",
    main_grid_summary: "Địa phương thí điểm chính quyền số cấp xã, đưa hồ sơ và báo cáo lên nền tảng chung.",
    card_summary: "Mô hình chính quyền số cấp xã được thử nghiệm tại một số địa phương, giúp cán bộ xử lý hồ sơ, theo dõi báo cáo và tiếp nhận phản ánh trên cùng một nền tảng. Thời gian giải quyết công việc bước đầu được rút ngắn.",
    full_content: "Mô hình chính quyền số cấp xã được thử nghiệm tại một số địa phương, cho phép cán bộ xử lý hồ sơ, theo dõi báo cáo và tiếp nhận phản ánh của người dân trên cùng một nền tảng.\n\nKết quả bước đầu cho thấy thời gian giải quyết công việc được rút ngắn và việc theo dõi tiến độ trở nên minh bạch hơn.\n\nĐịa phương cho biết thách thức lớn nhất là hạ tầng mạng ở vùng sâu và kỹ năng số của một bộ phận cán bộ, người dân.",
    image_url: "https://picsum.photos/seed/art_ct_04/1200/675",
    original_url: "https://vietnamnet.vn/dia-phuong-thi-diem-mo-hinh-chinh-quyen-so-artct04.html",
    source_name: "VietnamNet",
    category: "CHINH_TRI",
    published_at: "2026-10-25T07:00:00Z",
    ai_score: 74,
    is_main_featured: false,
  },
  {
    article_id: "art_ct_05",
    title: "Công bố lộ trình đổi mới kỳ thi tuyển sinh theo hướng đánh giá năng lực",
    main_grid_summary: "Đổi mới thi tuyển sinh hướng đến giảm áp lực và đánh giá năng lực học sinh toàn diện.",
    card_summary: "Lộ trình đổi mới được công bố theo từng giai đoạn, ưu tiên đánh giá năng lực thay cho ghi nhớ kiến thức và tăng cường tư vấn hướng nghiệp từ sớm. Các trường được khuyến khích chuẩn bị điều kiện tổ chức thi và xét tuyển phù hợp.",
    full_content: "Lộ trình đổi mới được công bố theo từng giai đoạn, ưu tiên đánh giá năng lực thay cho ghi nhớ kiến thức, đồng thời tăng cường tư vấn hướng nghiệp từ sớm.\n\nCác trường được khuyến khích chuẩn bị điều kiện tổ chức thi và xét tuyển phù hợp với từng địa phương, bảo đảm công bằng giữa các nhóm thí sinh.\n\nCơ quan quản lý sẽ lấy ý kiến học sinh, phụ huynh và giáo viên trước khi áp dụng chính thức.",
    image_url: "https://picsum.photos/seed/art_ct_05/1200/675",
    original_url: "https://tuoitre.vn/cong-bo-lo-trinh-doi-moi-ky-thi-tuyen-artct05.html",
    source_name: "Tuổi Trẻ",
    category: "CHINH_TRI",
    published_at: "2026-10-25T03:00:00Z",
    ai_score: 70,
    is_main_featured: false,
  },
  {
    article_id: "art_cn_01",
    title: "Nhiều trường đại học đưa trí tuệ nhân tạo vào chương trình đào tạo công nghệ thông tin",
    main_grid_summary: "Học phần trí tuệ nhân tạo và học máy trở thành nội dung bắt buộc tại nhiều trường đại học.",
    card_summary: "Nhiều trường đại học bổ sung các học phần về trí tuệ nhân tạo, học máy và dữ liệu lớn vào chương trình công nghệ thông tin. Sinh viên được thực hành trên dự án thực tế để rút ngắn khoảng cách với yêu cầu của doanh nghiệp.",
    full_content: "Nhiều trường đại học cập nhật chương trình công nghệ thông tin theo hướng bổ sung các học phần về trí tuệ nhân tạo, học máy và dữ liệu lớn.\n\nSinh viên được thực hành trên các dự án thực tế, qua đó rút ngắn khoảng cách giữa kiến thức trong lớp và yêu cầu của doanh nghiệp.\n\nĐại diện các trường cho biết đội ngũ giảng viên cũng được đào tạo bổ sung và hợp tác với doanh nghiệp để cập nhật công nghệ mới.",
    image_url: "https://picsum.photos/seed/art_cn_01/1200/675",
    original_url: "https://vnexpress.net/nhieu-truong-dai-hoc-dua-tri-tue-nhan-tao-artcn01.html",
    source_name: "VnExpress",
    category: "CONG_NGHE",
    published_at: "2026-10-26T06:00:00Z",
    ai_score: 95,
    is_main_featured: true,
  },
  {
    article_id: "art_cn_02",
    title: "Thế hệ chip di động mới tập trung tối ưu xử lý trí tuệ nhân tạo ngay trên thiết bị",
    main_grid_summary: "Chip di động mới xử lý AI trên thiết bị, giúp tăng tốc độ và bảo vệ quyền riêng tư.",
    card_summary: "Các nhà sản xuất giới thiệu vi xử lý di động mới với bộ tăng tốc chuyên dụng cho trí tuệ nhân tạo. Chạy mô hình trực tiếp trên điện thoại giúp giảm độ trễ, tiết kiệm pin và hạn chế gửi dữ liệu cá nhân lên đám mây.",
    full_content: "Các nhà sản xuất chip di động giới thiệu thế hệ vi xử lý mới với bộ tăng tốc chuyên dụng cho trí tuệ nhân tạo.\n\nViệc chạy mô hình trực tiếp trên điện thoại giúp giảm độ trễ, tiết kiệm pin và hạn chế phải gửi dữ liệu cá nhân lên máy chủ đám mây.\n\nTheo giới phân tích, xu hướng này sẽ thúc đẩy nhiều ứng dụng như dịch thuật, nhận diện giọng nói và chỉnh sửa ảnh hoạt động tốt hơn ngay cả khi không có kết nối mạng.",
    image_url: "https://picsum.photos/seed/art_cn_02/1200/675",
    original_url: "https://thanhnien.vn/the-he-chip-di-dong-moi-tap-trung-toi-artcn02.html",
    source_name: "Thanh Niên",
    category: "CONG_NGHE",
    published_at: "2026-10-26T03:50:00Z",
    ai_score: 91,
    is_main_featured: true,
  },
  {
    article_id: "art_cn_03",
    title: "Doanh nghiệp Việt tăng đầu tư an ninh mạng trước làn sóng tấn công mới",
    main_grid_summary: "Doanh nghiệp tăng ngân sách an ninh mạng khi các vụ tấn công giả mạo và mã độc gia tăng.",
    card_summary: "Số vụ tấn công mạng nhắm vào doanh nghiệp tăng trong quý này, chủ yếu qua email giả mạo và mã độc tống tiền. Nhiều đơn vị bắt đầu đầu tư giám sát an ninh, đào tạo nhân viên và xây dựng quy trình ứng phó sự cố.",
    full_content: "Báo cáo mới cho thấy số vụ tấn công mạng nhắm vào doanh nghiệp tăng trong quý này, chủ yếu qua email giả mạo và mã độc tống tiền.\n\nNhiều đơn vị bắt đầu đầu tư giám sát an ninh, đào tạo nhân viên và xây dựng quy trình ứng phó sự cố bài bản hơn.\n\nChuyên gia khuyến nghị doanh nghiệp bật xác thực hai lớp, sao lưu dữ liệu định kỳ và cập nhật phần mềm thường xuyên để giảm thiểu rủi ro.",
    image_url: "https://picsum.photos/seed/art_cn_03/1200/675",
    original_url: "https://dantri.com.vn/doanh-nghiep-viet-tang-dau-tu-an-ninh-mang-artcn03.html",
    source_name: "Dân Trí",
    category: "CONG_NGHE",
    published_at: "2026-10-26T01:40:00Z",
    ai_score: 89,
    is_main_featured: true,
  },
  {
    article_id: "art_cn_04",
    title: "Lập trình viên Android chuyển dần sang Jetpack Compose cho giao diện mới",
    main_grid_summary: "Jetpack Compose ngày càng phổ biến trong phát triển giao diện Android nhờ mã nguồn gọn và dễ bảo trì.",
    card_summary: "Nhiều nhóm phát triển Android bắt đầu xây dựng giao diện mới bằng Jetpack Compose thay vì layout XML truyền thống. Cách tiếp cận khai báo giúp giảm mã lặp và dễ kiểm thử, nhưng đội ngũ vẫn cần thời gian làm quen và chuyển dần màn hình cũ.",
    full_content: "Nhiều nhóm phát triển ứng dụng Android bắt đầu xây dựng giao diện mới bằng Jetpack Compose thay vì layout XML truyền thống.\n\nCách tiếp cận khai báo giúp giảm lượng mã lặp và dễ kiểm thử hơn, nhưng đội ngũ vẫn cần thời gian làm quen và di chuyển dần các màn hình cũ.\n\nNhiều dự án chọn giải pháp song song, dùng Compose cho tính năng mới và giữ nguyên phần cũ cho đến khi có điều kiện nâng cấp.",
    image_url: "https://picsum.photos/seed/art_cn_04/1200/675",
    original_url: "https://vnexpress.net/lap-trinh-vien-android-chuyen-dan-sang-jetpack-compose-artcn04.html",
    source_name: "VnExpress",
    category: "CONG_NGHE",
    published_at: "2026-10-25T08:30:00Z",
    ai_score: 78,
    is_main_featured: false,
  },
  {
    article_id: "art_cn_05",
    title: "Điện toán đám mây giúp startup tiết kiệm chi phí hạ tầng ở giai đoạn đầu",
    main_grid_summary: "Startup dùng điện toán đám mây để trả phí theo mức sử dụng và mở rộng hệ thống linh hoạt.",
    card_summary: "Thay vì đầu tư máy chủ riêng, nhiều startup thuê hạ tầng đám mây để trả phí theo mức sử dụng và dễ mở rộng khi người dùng tăng. Tuy nhiên, chuyên gia khuyến nghị theo dõi chi phí thường xuyên để tránh phát sinh ngoài dự kiến.",
    full_content: "Thay vì đầu tư máy chủ riêng, nhiều startup chọn thuê hạ tầng đám mây để trả phí theo mức sử dụng và dễ dàng mở rộng khi lượng người dùng tăng.\n\nCách làm này giúp đội ngũ nhỏ tập trung vào sản phẩm thay vì vận hành hạ tầng.\n\nTuy nhiên, các chuyên gia khuyến nghị cần theo dõi chi phí thường xuyên để tránh phát sinh ngoài dự kiến.",
    image_url: "https://picsum.photos/seed/art_cn_05/1200/675",
    original_url: "https://thanhnien.vn/dien-toan-dam-may-giup-startup-tiet-kiem-chi-artcn05.html",
    source_name: "Thanh Niên",
    category: "CONG_NGHE",
    published_at: "2026-10-25T05:15:00Z",
    ai_score: 72,
    is_main_featured: false,
  },
  {
    article_id: "art_bl_01",
    title: "Quán cà phê nhỏ giữ chân giới trẻ Sài Gòn bằng không gian đọc sách yên tĩnh",
    main_grid_summary: "Quán cà phê nhỏ có góc đọc sách yên tĩnh thành điểm hẹn cuối tuần của giới trẻ Sài Gòn.",
    card_summary: "Giữa nhịp sống nhanh, nhiều quán cà phê nhỏ tại Sài Gòn chọn thiết kế không gian yên tĩnh với kệ sách và bàn học riêng. Khách chủ yếu là sinh viên và nhân viên văn phòng trẻ, tìm nơi để đọc, làm việc và nghỉ ngơi.",
    full_content: "Giữa nhịp sống nhanh, nhiều quán cà phê quy mô nhỏ tại Sài Gòn chọn thiết kế không gian yên tĩnh với kệ sách và bàn học riêng.\n\nKhách hàng chủ yếu là sinh viên và nhân viên văn phòng trẻ, tìm một nơi để đọc, làm việc và nghỉ ngơi sau tuần dài.\n\nChủ quán cho biết điều giữ chân khách không phải đồ uống đắt đỏ mà là cảm giác thoải mái và riêng tư.",
    image_url: "https://picsum.photos/seed/art_bl_01/1200/675",
    original_url: "https://tuoitre.vn/quan-ca-phe-nho-giu-chan-gioi-tre-sai-artbl01.html",
    source_name: "Tuổi Trẻ",
    category: "BEN_LE",
    published_at: "2026-10-26T05:00:00Z",
    ai_score: 66,
    is_main_featured: false,
  },
  {
    article_id: "art_bl_02",
    title: "Sinh viên rủ nhau chạy bộ buổi sáng để giảm căng thẳng mùa thi",
    main_grid_summary: "Nhóm sinh viên duy trì thói quen chạy bộ buổi sáng để giữ tinh thần thoải mái trong mùa thi.",
    card_summary: "Nhiều câu lạc bộ sinh viên tổ chức các buổi chạy bộ nhẹ vào sáng sớm quanh khuôn viên trường và công viên gần đó. Hoạt động này giúp các bạn cân bằng giữa học tập và sức khỏe, đồng thời kết nối thêm bạn bè mới.",
    full_content: "Nhiều câu lạc bộ sinh viên tổ chức các buổi chạy bộ nhẹ nhàng vào sáng sớm quanh khuôn viên trường và công viên gần đó.\n\nHoạt động này giúp các bạn trẻ cân bằng giữa học tập và sức khỏe, đồng thời kết nối thêm bạn bè mới trong mùa thi căng thẳng.\n\nCác bạn chia sẻ chỉ cần 30 phút vận động mỗi sáng cũng đủ giúp đầu óc tỉnh táo và ngủ ngon hơn.",
    image_url: "https://picsum.photos/seed/art_bl_02/1200/675",
    original_url: "https://thanhnien.vn/sinh-vien-ru-nhau-chay-bo-buoi-sang-de-artbl02.html",
    source_name: "Thanh Niên",
    category: "BEN_LE",
    published_at: "2026-10-26T01:30:00Z",
    ai_score: 62,
    is_main_featured: false,
  },
  {
    article_id: "art_bl_03",
    title: "Chợ sách cũ cuối tuần hút người trẻ tìm lại thói quen đọc sách giấy",
    main_grid_summary: "Chợ sách cũ cuối tuần thu hút người trẻ muốn tìm lại thói quen đọc sách giấy.",
    card_summary: "Các gian hàng sách cũ mở vào cuối tuần thu hút đông đảo bạn trẻ đến tìm những đầu sách quý, giáo trình giá mềm và truyện cũ. Nhiều người cho biết đọc sách giấy giúp họ tập trung hơn và giảm thời gian dùng điện thoại mỗi ngày.",
    full_content: "Các gian hàng sách cũ mở vào cuối tuần thu hút đông đảo bạn trẻ đến tìm những đầu sách quý, giáo trình giá mềm và truyện cũ.\n\nNhiều người cho biết đọc sách giấy giúp họ tập trung hơn và giảm thời gian dùng điện thoại mỗi ngày.\n\nBan tổ chức dự kiến mở rộng thêm khu trao đổi sách để người đọc có thể đổi sách cho nhau.",
    image_url: "https://picsum.photos/seed/art_bl_03/1200/675",
    original_url: "https://vietnamnet.vn/cho-sach-cu-cuoi-tuan-hut-nguoi-tre-tim-artbl03.html",
    source_name: "VietnamNet",
    category: "BEN_LE",
    published_at: "2026-10-25T10:00:00Z",
    ai_score: 60,
    is_main_featured: false,
  },
  {
    article_id: "art_bl_04",
    title: "Giải bóng đá sinh viên khai mạc với sự tham gia của hàng chục đội bóng",
    main_grid_summary: "Giải bóng đá sinh viên khai mạc với hàng chục đội đến từ nhiều trường đại học khác nhau.",
    card_summary: "Lễ khai mạc diễn ra sôi nổi với màn diễu hành của các đội bóng và trận mở màn hấp dẫn giữa hai trường chủ nhà. Ban tổ chức kỳ vọng giải đấu tạo sân chơi lành mạnh, khuyến khích sinh viên rèn luyện thể thao.",
    full_content: "Lễ khai mạc diễn ra sôi nổi với màn diễu hành của các đội bóng và trận mở màn hấp dẫn giữa hai trường chủ nhà.\n\nTheo lịch thi đấu, các đội sẽ chia bảng và đá vòng tròn trong hai tuần trước khi bước vào vòng loại trực tiếp.\n\nNhiều trường cho biết sẽ tổ chức cổ vũ tập thể và hỗ trợ cầu thủ cân bằng giữa luyện tập và học tập.",
    image_url: "https://picsum.photos/seed/art_bl_04/1200/675",
    original_url: "https://thanhnien.vn/giai-bong-da-sinh-vien-khai-mac-voi-su-artbl04.html",
    source_name: "Thanh Niên",
    category: "BEN_LE",
    published_at: "2026-10-25T06:20:00Z",
    ai_score: 58,
    is_main_featured: false,
  },
];

const ARTICLES = ARTICLE_SEEDS.map((a) => {
  const publishedAt = new Date(a.published_at);
  return { ...a, published_at: publishedAt, date_label: buildDateLabel(publishedAt) };
});

// ---------------------------------------------------------------------------
// BIỂU ĐỒ THỊ TRƯỜNG: 5 mã × 15 điểm lịch sử (12/10 → 26/10)
// current_value = điểm cuối; change_percent = % thay đổi so với điểm liền trước
// ---------------------------------------------------------------------------
const HISTORY_START_DAY = 12; // 12/10 → 26/10 = 15 ngày liên tiếp
const HISTORY_MONTH = "10";

function buildChart({ symbol, asset_name, asset_type, unit, display_order, is_default_top, series, decimals }) {
  const history_points = series.map((price_value, i) => ({
    recorded_at: `${String(HISTORY_START_DAY + i).padStart(2, "0")}/${HISTORY_MONTH}`,
    price_value,
  }));
  const last = series[series.length - 1];
  const prev = series[series.length - 2];
  const factor = 10 ** (decimals ?? 2);
  return {
    symbol,
    asset_name,
    asset_type,
    unit,
    current_value: last,
    change_percent: Math.round(((last - prev) / prev) * 100 * factor) / factor,
    is_default_top,
    display_order,
    history_points,
  };
}

const MARKET_CHARTS = [
  buildChart({
    symbol: "USD_VND", asset_name: "Tỷ giá USD/VND", asset_type: "CURRENCY", unit: "VNĐ",
    display_order: 1, is_default_top: true,
    series: [25210, 25235, 25220, 25260, 25285, 25270, 25310, 25340, 25325, 25380, 25410, 25395, 25420, 25425, 25450],
  }),
  buildChart({
    symbol: "RON95", asset_name: "Xăng RON 95-III", asset_type: "FUEL", unit: "VNĐ/lít",
    display_order: 2, is_default_top: false,
    series: [21480, 21480, 21480, 21480, 21480, 21480, 21480, 21650, 21650, 21650, 21650, 21650, 21650, 21650, 21520],
  }),
  buildChart({
    symbol: "E5_RON92", asset_name: "Xăng E5 RON 92", asset_type: "FUEL", unit: "VNĐ/lít",
    display_order: 3, is_default_top: false,
    series: [20260, 20260, 20260, 20260, 20260, 20260, 20260, 20420, 20420, 20420, 20420, 20420, 20420, 20420, 20290],
  }),
  buildChart({
    symbol: "GOLD_SJC", asset_name: "Vàng SJC 9999", asset_type: "COMMODITY", unit: "Triệu VNĐ/lượng",
    display_order: 4, is_default_top: false,
    series: [131.8, 132.4, 132.1, 133.0, 133.6, 133.2, 134.1, 134.8, 134.5, 135.2, 134.9, 135.6, 135.1, 135.4, 136.0],
  }),
  buildChart({
    symbol: "OIL_BRENT", asset_name: "Dầu thô Brent", asset_type: "FUEL", unit: "USD/thùng",
    display_order: 5, is_default_top: false,
    series: [67.2, 66.8, 67.5, 68.1, 67.7, 66.9, 67.3, 68.0, 68.6, 68.2, 67.8, 68.5, 69.0, 68.6, 69.3],
  }),
];

// ---------------------------------------------------------------------------
// Nạp dữ liệu
// ---------------------------------------------------------------------------
async function seed() {
  const uri = (process.env.MONGODB_URI || "").trim();
  if (!uri) throw new Error("Thiếu MONGODB_URI. Hãy tạo file .env từ .env.example.");

  await mongoose.connect(uri, { serverSelectionTimeoutMS: 15000 });
  console.log(`Đã kết nối database "${mongoose.connection.name}"`);

  const [delArticles, delCharts] = await Promise.all([Article.deleteMany({}), MarketChart.deleteMany({})]);
  console.log(`Đã xóa dữ liệu cũ: ${delArticles.deletedCount} bài báo, ${delCharts.deletedCount} biểu đồ`);

  // Đồng bộ index theo schema (unique, compound, text...) trước khi nạp
  await Promise.all([Article.syncIndexes(), MarketChart.syncIndexes()]);

  await Article.insertMany(ARTICLES, { ordered: true });
  await MarketChart.insertMany(MARKET_CHARTS, { ordered: true });

  console.log(`Đã nạp ${ARTICLES.length} bài báo và ${MARKET_CHARTS.length} biểu đồ:`);
  for (const category of ["KINH_TE", "CHINH_TRI", "CONG_NGHE", "BEN_LE"]) {
    const total = ARTICLES.filter((a) => a.category === category).length;
    const featured = ARTICLES.filter((a) => a.category === category && a.is_main_featured).length;
    console.log(`  ${category.padEnd(10)} ${total} bài (is_main_featured: ${featured})`);
  }
  console.log("Hoàn tất. Chạy `npm start` rồi thử: http://localhost:" + (process.env.PORT || 5000) + "/api/v1/news/main-grids");
}

module.exports = { ARTICLES, MARKET_CHARTS, seed };

if (require.main === module) {
  seed()
    .catch((err) => {
      console.error("Seed thất bại:", err.message);
      process.exitCode = 1;
    })
    .finally(() => mongoose.disconnect());
}
