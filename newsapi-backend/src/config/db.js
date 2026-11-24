const mongoose = require("mongoose");

let connectionPromise = null;
let listenersAttached = false;

/**
 * Kết nối MongoDB (Atlas hoặc Local) bằng Mongoose.
 * Reuse a warm connection and share one pending connection across concurrent requests.
 * A failed/disconnected connection can be retried by a later serverless invocation.
 */
async function connectDB() {
  if (mongoose.connection.readyState === 1) return mongoose.connection;
  if (connectionPromise) return connectionPromise;

  const uri = (process.env.MONGODB_URI || "").trim();
  if (!uri) {
    throw new Error("Thiếu biến môi trường MONGODB_URI. Hãy tạo file .env từ .env.example.");
  }

  mongoose.set("strictQuery", true);

  if (!listenersAttached) {
    mongoose.connection.on("disconnected", () => console.warn("[MongoDB] Mất kết nối."));
    mongoose.connection.on("reconnected", () => console.log("[MongoDB] Đã kết nối lại."));
    mongoose.connection.on("error", (err) => console.error("[MongoDB] Lỗi:", err.message));
    listenersAttached = true;
  }

  connectionPromise = Promise.resolve()
    .then(() => mongoose.connect(uri, { serverSelectionTimeoutMS: 15000 }))
    .then(() => {
      console.log("[MongoDB] Đã kết nối.");
      return mongoose.connection;
    })
    .finally(() => { connectionPromise = null; });
  return connectionPromise;
}

module.exports = connectDB;
