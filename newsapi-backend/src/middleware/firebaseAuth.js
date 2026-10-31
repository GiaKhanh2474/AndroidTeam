const { getApps, initializeApp, applicationDefault } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");

async function verifyIdToken(token) {
  const app = getApps()[0] || initializeApp({ credential: applicationDefault() });
  return getAuth(app).verifyIdToken(token, true);
}

// Injectable verifier keeps authorization tests independent of Firebase credentials.
function createRequireFirebaseAuth(verify = verifyIdToken) {
  return async (req, res, next) => {
    const match = /^Bearer\s+(\S+)$/i.exec(req.get("authorization") || "");
    if (!match) {
      return res.status(401).json({ status: "error", message: "Cần Firebase ID token" });
    }
    try {
      const claims = await verify(match[1]);
      if (!claims || typeof claims.uid !== "string" || !claims.uid) throw new Error("Missing UID");
      req.auth = claims;
    } catch (_) {
      return res.status(401).json({ status: "error", message: "Firebase ID token không hợp lệ hoặc đã hết hạn" });
    }
    return next();
  };
}

module.exports = { requireFirebaseAuth: createRequireFirebaseAuth(), createRequireFirebaseAuth };
