const mongoose = require("mongoose");

/**
 * Kết nối MongoDB (Atlas hoặc Local) bằng Mongoose.
 * Ném lỗi nếu thiếu MONGODB_URI hoặc không kết nối được — server.js sẽ in lỗi rồi thoát.
 */
async function connectDB() {
  const uri = (process.env.MONGODB_URI || "").trim();
  if (!uri) {
    throw new Error("Thiếu biến môi trường MONGODB_URI. Hãy tạo file .env từ .env.example.");
  }

  mongoose.set("strictQuery", true);

  mongoose.connection.on("disconnected", () => console.warn("[MongoDB] Mất kết nối."));
  mongoose.connection.on("reconnected", () => console.log("[MongoDB] Đã kết nối lại."));
  mongoose.connection.on("error", (err) => console.error("[MongoDB] Lỗi:", err.message));

  await mongoose.connect(uri, { serverSelectionTimeoutMS: 15000 });
  console.log(`[MongoDB] Đã kết nối database "${mongoose.connection.name}" (${mongoose.connection.host})`);
  return mongoose.connection;
}

module.exports = connectDB;
