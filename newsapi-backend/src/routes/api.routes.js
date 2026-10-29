const express = require("express");

const news = require("../controllers/news.controller");
const market = require("../controllers/market.controller");
const user = require("../controllers/user.controller");
const admin = require("../controllers/admin.controller");

const router = express.Router();

// Bắt lỗi async để chuyển cho error handler trong server.js
const wrap = (fn) => (req, res, next) => Promise.resolve(fn(req, res, next)).catch(next);

// ----- Tin tức -----
router.get("/news/main-grids", wrap(news.getMainGrids)); // API 1
router.get("/news/by-date", wrap(news.getArticlesByDate)); // API 2
router.get("/news/detail/:id", wrap(news.getArticleDetail)); // API 3
router.get("/news/search", wrap(news.searchArticles)); // API 4
router.get("/news/sideline", wrap(news.getSidelineArticles)); // API 6

// ----- Thị trường -----
router.get("/market/charts", wrap(market.getMarketCharts)); // API 5

// ----- Người dùng & quản trị -----
router.post("/users/check", wrap(user.checkUser)); // kiểm tra email / số điện thoại đã dùng chưa
router.post("/users/sync", wrap(user.syncUser)); // API 7a
router.post("/admin/trigger-crawl", wrap(admin.triggerCrawl)); // API 7b

module.exports = router;
