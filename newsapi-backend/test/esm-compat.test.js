const { test } = require("node:test");
const assert = require("node:assert/strict");
const { spawnSync } = require("node:child_process");
const fs = require("node:fs");
const os = require("node:os");
const path = require("node:path");
const { patchJwksRsa } = require("../scripts/patch-jwks-rsa.cjs");

test("Firebase, JWKS signatures and Passport work with synchronous ESM loading disabled", () => {
  const result = spawnSync(process.execPath, ["--no-experimental-require-module", "-e", `
    const assert = require('node:assert/strict');
    const { generateKeyPairSync } = require('node:crypto');
    const jwt = require('jsonwebtoken');
    const jwks = require('jwks-rsa');
    assert.equal(typeof require('firebase-admin/auth').getAuth, 'function');
    assert.equal(typeof require('./server'), 'function');

    (async () => {
      const { publicKey, privateKey } = generateKeyPairSync('rsa', { modulusLength: 2048 });
      const jwk = { ...publicKey.export({ format: 'jwk' }), kid: 'test-key', alg: 'RS256', use: 'sig' };
      const options = { jwksUri: 'https://unused.invalid/jwks', cache: false,
        fetcher: async () => ({ keys: [jwk] }) };
      const keys = await jwks(options).getSigningKeys();
      assert.equal(keys.length, 1);
      assert.equal(keys[0].kid, jwk.kid);
      const token = jwt.sign({ sub: 'test-user' }, privateKey, { algorithm: 'RS256', keyid: jwk.kid });
      assert.equal(jwt.verify(token, keys[0].getPublicKey(), { algorithms: ['RS256'] }).sub, 'test-user');
      const other = generateKeyPairSync('rsa', { modulusLength: 2048 });
      const forged = jwt.sign({ sub: 'test-user' }, other.privateKey, { algorithm: 'RS256', keyid: jwk.kid });
      assert.throws(() => jwt.verify(forged, keys[0].getPublicKey(), { algorithms: ['RS256'] }), /invalid signature/);

      const resolve = (provider, value) => new Promise((yes, no) => {
        provider({}, value, (error, key) => error ? no(error) : yes(key));
      });
      const provider = jwks.passportJwtSecret(options);
      const pem = await resolve(provider, token);
      assert.equal(jwt.verify(token, pem, { algorithms: ['RS256'] }).sub, 'test-user');
      assert.equal(await resolve(provider, 'malformed'), null);
      assert.equal(await resolve(provider, jwt.sign({}, 'test-secret', { algorithm: 'HS256' })), null);
      const unknown = jwt.sign({}, privateKey, { algorithm: 'RS256', keyid: 'unknown-key' });
      assert.equal(await resolve(provider, unknown), null);
      await assert.rejects(resolve(jwks.passportJwtSecret({ ...options,
        fetcher: async () => { throw new Error('JWKS unavailable'); }
      }), token), /JWKS unavailable/);
    })().catch(error => { console.error(error); process.exitCode = 1; });
  `], {
    cwd: path.resolve(__dirname, ".."), encoding: "utf8", timeout: 20000,
    env: { ...process.env, VERCEL: "1", CRON_ENABLED: "false" },
  });
  assert.equal(result.error, undefined);
  assert.equal(result.status, 0, result.stderr || result.stdout);
});

test("compatibility patch is repeatable and rejects unexpected dependency content/version", () => {
  const directory = fs.mkdtempSync(path.join(os.tmpdir(), "androidteam-jwks-test-"));
  try {
    const installed = path.dirname(require.resolve("jwks-rsa/package.json"));
    for (const file of ["package.json", "src/utils.js", "src/integrations/passport.js"]) {
      const destination = path.join(directory, file);
      fs.mkdirSync(path.dirname(destination), { recursive: true });
      fs.copyFileSync(path.join(installed, file), destination);
    }
    assert.equal(patchJwksRsa(directory), 0);
    fs.appendFileSync(path.join(directory, "src/utils.js"), "\n// unexpected change\n");
    assert.throws(() => patchJwksRsa(directory), /Unexpected jwks-rsa source/);
    fs.writeFileSync(path.join(directory, "package.json"), '{"version":"4.2.0"}');
    assert.throws(() => patchJwksRsa(directory), /Review the jose workaround/);
  } finally {
    // directory is a unique test-only child of the OS temporary directory.
    fs.rmSync(directory, { recursive: true, force: true });
  }
});
