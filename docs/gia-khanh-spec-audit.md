# Đối chiếu phần Phạm Gia Khánh — 07/11/2026

Phạm vi: mục 2.1 và các yêu cầu liên quan của đặc tả người dùng. Kiểm tra mã nguồn Gia-Khanh tại 0e6505f và bổ sung đọc bài đã lưu offline. Không đánh giá hoàn thành thay cho Nam Khánh/Tường Long.

| Yêu cầu | Kết quả đối chiếu |
|---|---|
| Kotlin, minSdk 24, Java 11, ViewBinding | Có cấu hình trong app/build.gradle.kts. |
| AndroidTeamNews, com.vlu.newsapi, SDK 34 | Chưa khớp: project News, namespace/applicationId com.example.news, compile/target 37. Chưa đổi vì ảnh hưởng toàn bộ module. |
| Base UI colors/dimens/theme chuẩn | Có tài nguyên dùng chung nhưng tên/giá trị chưa đúng bộ brand_primary, space_xs, Theme.AndroidTeamNews… |
| DrawerLayout + NavHost, không Bottom Navigation | Có NavHost, không Bottom Navigation; activity_main hiện là FrameLayout, thiếu DrawerLayout và các ID chuẩn. |
| Navigation IDs/arguments chuẩn | Có điều hướng; màn detail hiện là Activity, các action/argument chưa trùng đặc tả. Cần phối hợp trước khi đổi contract. |
| Singleton AppDatabase | Có NewsDatabase qua AppContainer lazy; chưa đúng tên AppDatabase và chưa singleton ở factory create(). |
| SavedArticleEntity / lưu-xóa bài | Có lưu đủ nội dung, Room DAO, Flow; toggle có transaction. Tên DB news.db, bảng saved_article và tên cột khác đặc tả news_app_local.db/saved_articles. |
| Đọc bài đã lưu offline | Đã bổ sung OfflineNewsRepository: tìm bản Room trước, nguồn tin chỉ được gọi khi chưa lưu. Áp dụng qua AppContainer, không sửa UI/ViewModel của thành viên khác. |
| TrackedChartEntity / NewsLocalDao | Chưa đúng schema: chỉ lưu ID, thứ tự, thời điểm đánh dấu; chưa lưu giá, loại tài sản, trạng thái và lịch sử biểu đồ. Promote hiện có Room transaction. |
| SeedDatabaseCallback | Chưa có; dữ liệu mẫu hiện nằm trong MockNews/MockMarket, không phải seed SQLite. |
| RetrofitClient / NewsApiService / DTO | Chưa có; tin tức và thị trường vẫn dùng mock repository. |
| Đồng bộ Cloud–Room / cache API | Chưa có. Bổ sung lần này chỉ hoàn thiện đọc bài đã lưu; không đồng nghĩa cache trang chủ, biểu đồ hay ảnh offline. |
| GestureHelper hai hàm chuẩn | Chưa có API chung đúng tên; hiện có DoubleTapLayout, SwipeBackLayout; logic mũi tên nằm trong DetailActivity, chưa đúng điều kiện scrollY > 900 và dy < -10. |
| Node/Express/MongoDB, models/indexes, seed | Có code tại newsapi-backend, khác đường dẫn backend trong đặc tả. Chưa kiểm chứng Atlas bằng credentials thật. |
| 6 GET API và 2 POST API | Có route/controller. users/sync hiện yêu cầu Firebase ID token; cần client gửi Authorization header. Main grids ưu tiên cửa sổ 48 giờ, có thể trả cả bài không featured nên khác lọc featured=true nghiêm ngặt. |
| RSS / cron | Có VnExpress, Tuổi Trẻ và cron; thiếu CafeF, Thanh Niên. |
| Gemini 2.5 Flash / tóm tắt | Có pipeline, fallback 18/45 từ và xử lý quota; default model đang gemini-3.8-flash, khác đặc tả. Validator chấp nhận tóm tắt >=5/10 từ nên chưa đảm bảo tối thiểu 15/35 từ. |
| EncryptedSharedPreferences | Chưa có; PreferencesSessionRepository lưu cờ demo bằng SharedPreferences thường. Chưa lưu token thật. |
| Ẩn secrets | Backend đọc biến môi trường; .env/node_modules được ignore. Không xác minh secrets của môi trường triển khai. |
| Render triển khai | Chưa có bằng chứng URL deployment hoạt động trong phạm vi kiểm tra. Root Directory hiện phải tương ứng newsapi-backend nếu giữ cấu trúc repo. |
| LeakCanary / Profiler | Chưa có bằng chứng kiểm tra memory leak. |
| Keystore / APK-AAB release | Chưa có bằng chứng signing configuration hoặc bản phát hành đã ký. |

## Kiểm thử bổ sung

OfflineNewsRepositoryTest gồm: mở bản đã lưu khi nguồn tin không truy cập được; bài chưa lưu đọc từ nguồn; sau xóa không dùng bản cache trong RAM. Cần chạy testDebugUnitTest và kiểm tra chế độ máy bay trên thiết bị. Không khẳng định các test Android đã pass khi Gradle chưa build thành công.

## Thứ tự phần việc tiếp theo của Gia Khánh

1. Room schema/migration tương thích dữ liệu hiện có, chart cache và seed.
2. Retrofit/DTO/repository kết nối URL backend thật và đồng bộ cache.
3. Bổ sung GestureHelper/base UI theo contract thống nhất với hai thành viên.
4. Đối chiếu model Gemini, nguồn RSS và validation theo đặc tả.
5. Cấu hình triển khai, storage bảo mật, kiểm tra leak và ký release với thông tin của chủ dự án.
