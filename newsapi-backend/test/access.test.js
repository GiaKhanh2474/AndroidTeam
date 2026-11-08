const { test, afterEach } = require("node:test");
const assert = require("node:assert/strict");
// Avoid project discovery through machine metadata in the malformed-token test.
process.env.GOOGLE_CLOUD_PROJECT ||= "demo-androidteam-tests";
const express = require("express");
const { createRequireFirebaseAuth } = require("../src/middleware/firebaseAuth");
const User = require("../src/models/User.model");
const { syncUser, checkUser } = require("../src/controllers/user.controller");
const app = require("../server");

async function withServer(app, fn) {
  const server = app.listen(0, "127.0.0.1");
  await new Promise((resolve) => server.once("listening", resolve));
  try {
    await fn(`http://127.0.0.1:${server.address().port}`);
  } finally {
    await new Promise((resolve, reject) => server.close(err => err ? reject(err) : resolve()));
  }
}

const originalEnv = { ...process.env };
afterEach(() => {
  for (const key of ["ADMIN_KEY", "NODE_ENV", "ADMIN_ALLOW_UNAUTHENTICATED"]) {
    if (originalEnv[key] === undefined) delete process.env[key];
    else process.env[key] = originalEnv[key];
  }
});

test("real sync route rejects missing and invalid tokens before database access", async () => {
  await withServer(app, async base => {
    for (const authorization of [undefined, "Bearer invalid-token"]) {
      const response = await fetch(`${base}/api/v1/users/sync`, {
        method: "POST", headers: { "content-type": "application/json", ...(authorization && { authorization }) },
        body: JSON.stringify({ firebase_uid: "victim" }),
      });
      assert.equal(response.status, 401);
    }
  });
});

test("verified user cannot sync another UID; omitted UID uses the verified identity", async t => {
  const app = express();
  app.use(express.json());
  app.post("/sync", createRequireFirebaseAuth(async token => {
    assert.equal(token, "valid-token");
    return { uid: "owner" };
  }), syncUser);
  t.mock.method(User, "exists", async filter => {
    assert.deepEqual(filter, { firebase_uid: "owner" });
    return true;
  });
  t.mock.method(User, "findOneAndUpdate", (filter, update) => {
    assert.deepEqual(filter, { firebase_uid: "owner" });
    assert.deepEqual(update, { $set: { display_name: "Updated" } });
    return { lean: async () => ({ firebase_uid: "owner", display_name: "Updated" }) };
  });
  await withServer(app, async base => {
    const send = body => fetch(`${base}/sync`, {
      method: "POST", headers: { "content-type": "application/json", authorization: "Bearer valid-token" },
      body: JSON.stringify(body),
    });
    assert.equal((await send({ firebase_uid: "victim" })).status, 403);
    const response = await send({ display_name: "Updated" });
    assert.equal(response.status, 200);
    assert.equal((await response.json()).firebase_uid, "owner");
  });
});

test("expired or revoked tokens are denied and never reach the controller", async () => {
  const app = express();
  app.post("/sync", createRequireFirebaseAuth(async () => {
    throw Object.assign(new Error("revoked"), { code: "auth/id-token-revoked" });
  }), () => assert.fail("unauthorized request reached controller"));
  await withServer(app, async base => {
    const response = await fetch(`${base}/sync`, {
      method: "POST", headers: { authorization: "Bearer revoked-token" },
    });
    assert.equal(response.status, 401);
  });
});

test("public availability check cannot hide a conflict using a supplied UID", async t => {
  t.mock.method(User, "find", filter => {
    assert.equal(filter.firebase_uid, undefined);
    return { lean: async () => [{ email: "used@example.com" }] };
  });
  let result;
  await checkUser({ body: { email: "used@example.com", firebase_uid: "victim" } }, {
    json: value => { result = value; },
  });
  assert.equal(result.available, false);
});

test("admin denies missing configuration even with a production demo bypass", async () => {
  delete process.env.ADMIN_KEY;
  process.env.NODE_ENV = "production";
  process.env.ADMIN_ALLOW_UNAUTHENTICATED = "true";
  await withServer(app, async base => {
    const response = await fetch(`${base}/api/v1/admin/trigger-crawl`, { method: "POST" });
    assert.equal(response.status, 401);
  });
});

test("admin denies an incorrect configured key", async () => {
  process.env.ADMIN_KEY = "test-admin-key";
  await withServer(app, async base => {
    const response = await fetch(`${base}/api/v1/admin/trigger-crawl`, {
      method: "POST", headers: { "x-admin-key": "wrong" },
    });
    assert.equal(response.status, 401);
  });
});
