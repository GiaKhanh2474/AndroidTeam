# AndroidTeamNews — Backend & AI Pipeline

Package Android: `com.vlu.newsapi` · Node.js (>= 20.18.1) + Express + MongoDB (Mongoose) + Gemini 3.8 Flash + RSS crawler

## 1. Chạy nhanh

```bash
npm install
cp .env.example .env      # Windows: copy .env.example .env  → rồi điền MONGODB_URI, GEMINI_API_KEY
npm run seed              # xóa articles + market_charts cũ, nạp 19 bài + 5 biểu đồ mẫu
npm start                 # http://localhost:5000/api/v1   (npm run dev = tự restart khi sửa code)
```

Thử nhanh: `http://localhost:5000/api/v1/news/main-grids` và `http://localhost:5000/health`.

### Kết nối MongoDB Atlas
1. Atlas → **Database & Network Access**: tạo Database User, thêm IP (`0.0.0.0/0` khi phát triển).
2. Cluster → **Connect → Drivers** → copy connection string, dán vào `MONGODB_URI` (đổi `<password>`, URL-encode nếu có ký tự đặc biệt).
3. Chạy `npm run seed` rồi mở **Data Explorer** để thấy database `newsapi_db`.

### Gemini API key
Lấy tại https://aistudio.google.com/apikey. **Không có key vẫn chạy được**: service tự chuyển sang chế độ fallback
(cắt 18 từ đầu làm `main_grid_summary`, 45 từ đầu làm `card_summary`, suy luận `category` theo URL RSS).

## 2. Cấu trúc

```
newsapi-backend/
├── server.js                       # Express, CORS, morgan, /health, error handler, khởi động cron
├── seed/seedData.js                # 19 bài tiếng Việt + 5 biểu đồ × 15 điểm; `npm run seed`
├── src/
│   ├── config/        db.js, gemini.js
│   ├── models/        Article, MarketChart, User, CrawlLog (.model.js)
│   ├── services/      geminiAi, rssCrawler, marketCrawler (.service.js)
│   ├── controllers/   news, market, user, admin (.controller.js)
│   ├── routes/        api.routes.js
│   ├── jobs/          cronJob.js   # mỗi 30 phút; `npm run crawl` để chạy 1 lần từ dòng lệnh
│   └── utils/         vnDate.js    # nhãn ngày theo giờ Việt Nam (UTC+7)
├── .env.example · package.json
```

## 3. API (`/api/v1`)

| # | Method | Endpoint | Trả về |
|---|---|---|---|
| 1 | GET | `/news/main-grids` | `{ kinhTe[3], chinhTri[3], congNghe[3] }` — mỗi phần tử là `ArticleDto` |
| 2 | GET | `/news/by-date?category=KINH_TE&page=1&limit=20` | `[ { date_group, articles[] } ]`, `full_content = null` |
| 3 | GET | `/news/detail/:id` | 1 `ArticleDto` đầy đủ (404 nếu không có) |
| 4 | GET | `/news/search?q=...` | `ArticleDto[]` (tối đa 20) |
| 5 | GET | `/market/charts` | `MarketAssetChartDto[]` kèm `history_points` |
| 6 | GET | `/news/sideline` | `ArticleDto[]` BEN_LE (tối đa 15) |
| 7a | POST | `/users/sync` | Firebase Bearer token + body `{ email, display_name, avatar_url }` → thông tin user |
| 7b | POST | `/admin/trigger-crawl[?wait=true]` | 202 chạy nền; `wait=true` chờ và trả kết quả |

`ArticleDto` (13 trường, giữ nguyên snake_case): `article_id, title, main_grid_summary, card_summary, full_content, image_url,
original_url, source_name, category, published_at, date_label, ai_score, is_main_featured`.

Lỗi 4xx/5xx trả `{ "status": "error", "message": "..." }`; các API thành công trả JSON thẳng, không bọc thêm lớp.

```bash
curl http://localhost:5000/api/v1/news/by-date?category=KINH_TE
curl "http://localhost:5000/api/v1/news/search?q=lãi%20suất"
curl -X POST http://localhost:5000/api/v1/users/sync -H "Content-Type: application/json" \
     -H "Authorization: Bearer <FIREBASE_ID_TOKEN>" \
     -d '{"email":"a@b.com","display_name":"Khanh"}'
curl -X POST "http://localhost:5000/api/v1/admin/trigger-crawl?wait=true"      # bắt buộc thêm -H "x-admin-key: <ADMIN_KEY>"
```

## 4. Nối với app Android
- Emulator gọi máy tính qua `http://10.0.2.2:5000/api/v1/` (base URL của Retrofit, có dấu `/` cuối).
- Điện thoại thật cùng Wi‑Fi: dùng IP LAN của máy (server đã lắng nghe `0.0.0.0`).
- Dùng `http` (không phải `https`) thì Android 9+ cần `android:usesCleartextTraffic="true"` trong `AndroidManifest.xml`
  (hoặc network security config) khi test; khi deploy lên server có HTTPS thì bỏ đi.

## 5. Lưu ý quan trọng
- **Dữ liệu seed là dữ liệu mẫu**: nội dung bài báo là giả lập, `original_url` chỉ là link minh họa, ảnh dùng `picsum.photos`
  (trừ bài đầu), giá trong `market_charts` không phải giá thị trường thật. Bài do crawler lấy mới là bài thật từ RSS.
- **Biểu đồ thị trường**: `USD_VND` (open.er-api.com) và `OIL_BRENT` (Yahoo Finance) lấy giá thật mỗi 30 phút;
  `RON95`, `E5_RON92`, `GOLD_SJC` không có API công khai ổn định nên được **mô phỏng** dao động nhẹ mỗi ngày
  (chỉ bật demo bằng `MARKET_SIMULATE_FALLBACK=true`; mặc định tắt).
- **Gemini free tier có giới hạn request/phút**: crawler nghỉ `GEMINI_DELAY_MS` (mặc định 6,5 giây) giữa 2 bài và chỉ lấy
  tối đa `MAX_ITEMS_PER_FEED` (mặc định 3) bài mới mỗi nguồn mỗi lượt. Dính lỗi 429 thì dùng fallback 10 phút rồi thử lại.
- `POST /users/sync` yêu cầu `Authorization: Bearer <FIREBASE_ID_TOKEN>`. UID được lấy từ token đã xác minh; UID khác trong body bị từ chối (403). Cấu hình Firebase Admin bằng Application Default Credentials: đặt `GOOGLE_APPLICATION_CREDENTIALS` tới tệp service account nằm ngoài repository và `GOOGLE_CLOUD_PROJECT` theo dự án Firebase. Token không hợp lệ/hết hạn/bị thu hồi trả 401. Xem https://firebase.google.com/docs/auth/admin/verify-id-tokens.
- `trigger-crawl` yêu cầu `ADMIN_KEY`; không đặt key thì từ chối. Chỉ demo trên máy: đặt cả `NODE_ENV=development` và `ADMIN_ALLOW_UNAUTHENTICATED=true` để bỏ qua key. Không dùng chế độ này khi triển khai công khai.
- Phân trang API 2 tính theo **số bài** (`limit`), nên một mốc ngày có thể tách sang trang kế — app nên gộp các nhóm cùng `date_group`.

## 6. Kiểm thử

Chạy `npm ci` rồi `npm test`. Các regression test kiểm tra quyền truy cập mà không cần MongoDB hoặc Firebase credentials thật. App Android vẫn dùng mock auth; khi nối API thật phải gửi Firebase ID token.


## Core update (2026-11-10)

## AI recovery and cycle locking

- New fallback articles have `ai_status=PENDING`, `ai_attempts=0` and a retry time. Each full cycle retries at most five due articles before scanning RSS. Successful AI results update the original document and mark it COMPLETED; no duplicate article is inserted.
- Failed retries keep existing summaries/content and back off from 10 minutes up to 24 hours. After five retry attempts the article becomes FAILED for operator review. After resolving the cause, reset that article to PENDING, ai_attempts=0 and next_retry_at=null to retry it.
- Old records without ai_status are re-evaluated once in bounded batches because their AI provenance was not recorded. A score of 50 is not used to identify failures. This can incur Gemini requests for previously successful legacy records too. Missing credentials and quota cooldown do not consume retry attempts.
- The full market → AI retry → RSS cycle has one synchronous in-process lock, shared by cron, startup and the admin endpoint. A concurrent admin request returns 409. The lock is always released after completion/errors.
- Run one backend process/instance with this scheduler. This lock is NOT distributed: do not run a separate `npm run crawl` process alongside the server. Multi-instance deployment requires a MongoDB lease or an external singleton scheduler.

### Android HTTPS backend URL

Copy the deployed service's public HTTPS URL from Render, then check `https://YOUR-SERVICE.onrender.com/health`. Build Android from the repository root with:

```powershell
.\gradlew.bat :app:assembleDebug -PnewsApiBaseUrl=https://YOUR-SERVICE.onrender.com/api/v1/
.\gradlew.bat :app:assembleRelease -PnewsApiBaseUrl=https://YOUR-SERVICE.onrender.com/api/v1/
```

Replace YOUR-SERVICE with the actual service name; do not use the Render dashboard URL. The trailing `/api/v1/` is the API base path. Release signing still requires the documented keystore environment variables. Changing Render environment variables alone does not change an APK's compiled base URL: rebuild and reinstall the app.

- Default model: `gemini-3.8-flash`, verified against https://aistudio.google.com/docs/models/gemini-3.8-flash. Override with `GEMINI_MODEL` in the deployment environment. Restart the service after changing it. No API key is committed.
- Main grids now return up to three featured articles per category, ordered by AI score then publication time. If fewer are eligible, the API returns fewer; it does not label fallback/unfeatured content as featured.
- RSS includes VnExpress, Tuoi Tre, CafeF and Thanh Nien. Individual feed/network failures remain isolated by the crawler.
- Market simulation is OFF by default. Only `MARKET_SIMULATE_FALLBACK=true` enables demo values; real RON95/E5/SJC providers remain an integration prerequisite. Existing seeded demo prices are not converted into live prices by this switch.
- Render blueprint: repository-root `render.yaml`, rootDir `newsapi-backend`. Supply MongoDB/Gemini/admin credentials in Render and Firebase Admin credentials through a Render secret file. This commit does not deploy the service.
