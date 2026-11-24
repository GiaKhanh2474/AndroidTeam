const { test } = require('node:test');
const assert = require('node:assert/strict');
const mongoose = require('mongoose');
const dotenv = require('dotenv');

const dbPath = require.resolve('../src/config/db');
const serverPath = require.resolve('../server');

function isolate(t, { vercel = true } = {}) {
  const env = { MONGODB_URI: process.env.MONGODB_URI, VERCEL: process.env.VERCEL };
  process.env.MONGODB_URI = 'mongodb://unused.invalid/unit-test';
  if (vercel) process.env.VERCEL = '1';
  else delete process.env.VERCEL;
  // Never read developer credentials or connect to a real database in these tests.
  t.mock.method(dotenv, 'config', () => ({}));
  t.mock.method(console, 'log', () => {});
  const originalListeners = new Map(['error', 'disconnected', 'reconnected']
    .map(name => [name, new Set(mongoose.connection.listeners(name))]));
  const state = { readyState: 0 };
  const originalReadyState = Object.getOwnPropertyDescriptor(mongoose.connection, 'readyState');
  Object.defineProperty(mongoose.connection, 'readyState', {
    configurable: true, get: () => state.readyState,
  });
  delete require.cache[dbPath];
  delete require.cache[serverPath];
  t.after(() => {
    for (const [name, value] of Object.entries(env)) {
      if (value === undefined) delete process.env[name];
      else process.env[name] = value;
    }
    for (const [name, original] of originalListeners) {
      for (const listener of mongoose.connection.listeners(name)) {
        if (!original.has(listener)) mongoose.connection.removeListener(name, listener);
      }
    }
    if (originalReadyState) Object.defineProperty(mongoose.connection, 'readyState', originalReadyState);
    else delete mongoose.connection.readyState;
    delete require.cache[dbPath];
    delete require.cache[serverPath];
  });
  return { state, connectDB: require(dbPath) };
}

async function withServer(app, action) {
  const server = app.listen(0, '127.0.0.1');
  await new Promise(resolve => server.once('listening', resolve));
  try {
    await action(`http://127.0.0.1:${server.address().port}`);
  } finally {
    await new Promise((resolve, reject) => server.close(error => error ? reject(error) : resolve()));
  }
}

test('concurrent cold requests share one connection; warm requests reuse it', async t => {
  const { state, connectDB } = isolate(t);
  let finish;
  let calls = 0;
  t.mock.method(mongoose, 'connect', async (uri, options) => {
    calls++;
    assert.equal(uri, 'mongodb://unused.invalid/unit-test');
    assert.equal(options.serverSelectionTimeoutMS, 15000);
    await new Promise(resolve => { finish = resolve; });
    state.readyState = 1;
  });
  const pending = [connectDB(), connectDB(), connectDB()];
  await Promise.resolve();
  assert.equal(calls, 1);
  finish();
  assert.deepEqual(await Promise.all(pending), Array(3).fill(mongoose.connection));
  await connectDB();
  assert.equal(calls, 1);
});

test('failed connection can retry and disconnect can reconnect without duplicate listeners', async t => {
  const { state, connectDB } = isolate(t);
  const counts = ['error', 'disconnected', 'reconnected'].map(name => mongoose.connection.listenerCount(name));
  let calls = 0;
  t.mock.method(mongoose, 'connect', async () => {
    if (++calls === 1) throw new Error('connection refused');
    state.readyState = 1;
  });
  const failures = await Promise.allSettled([connectDB(), connectDB()]);
  assert.deepEqual(failures.map(result => result.status), ['rejected', 'rejected']);
  assert.equal(calls, 1);
  await connectDB();
  state.readyState = 0;
  await connectDB();
  assert.equal(calls, 3);
  assert.deepEqual(['error', 'disconnected', 'reconnected']
    .map(name => mongoose.connection.listenerCount(name)), counts.map(count => count + 1));
});

test('missing URI fails before connecting and can recover when configured', async t => {
  const { state, connectDB } = isolate(t);
  const connect = t.mock.method(mongoose, 'connect', async () => { state.readyState = 1; });
  delete process.env.MONGODB_URI;
  await assert.rejects(connectDB(), /MONGODB_URI/);
  assert.equal(connect.mock.callCount(), 0);
  process.env.MONGODB_URI = 'mongodb://unused.invalid/unit-test';
  await connectDB();
  assert.equal(connect.mock.callCount(), 1);
});

test('Vercel root stays available; health and API initialize MongoDB without starting cron', async t => {
  const { state } = isolate(t);
  const connect = t.mock.method(mongoose, 'connect', async () => { state.readyState = 1; });
  const cron = require('../src/jobs/cronJob');
  const startCron = t.mock.method(cron, 'startCronJobs', () => assert.fail('serverless cron started'));
  const app = require(serverPath);
  await withServer(app, async base => {
    assert.equal((await fetch(base + '/')).status, 200);
    assert.equal(connect.mock.callCount(), 0);
    assert.equal((await fetch(base + '/health')).status, 200);
    assert.equal(connect.mock.callCount(), 1);
    const api = await fetch(base + '/api/v1/news/search?q=');
    assert.equal(api.status, 200);
    assert.deepEqual(await api.json(), []);
    assert.equal(connect.mock.callCount(), 1);
  });
  assert.equal(startCron.mock.callCount(), 0);
});

test('Vercel API connects on its first request and failed initialization returns generic 503', async t => {
  const { state } = isolate(t);
  let calls = 0;
  t.mock.method(mongoose, 'connect', async () => {
    if (++calls <= 2) throw new Error('mongodb://private-user:secret@private-host/db');
    state.readyState = 1;
  });
  const app = require(serverPath);
  await withServer(app, async base => {
    for (const path of ['/api/v1/news/search?q=', '/health']) {
      const response = await fetch(base + path);
      assert.equal(response.status, 503);
      const body = await response.json();
      assert.equal(body.status, 'error');
      assert.doesNotMatch(JSON.stringify(body), /private|secret|mongodb/);
    }
    const retry = await fetch(base + '/api/v1/news/search?q=');
    assert.equal(retry.status, 200);
    assert.equal(calls, 3);
  });
});

test('imported local app retains existing behavior without automatic database initialization', async t => {
  isolate(t, { vercel: false });
  t.mock.method(mongoose, 'connect', () => assert.fail('unexpected local DB connection'));
  const app = require(serverPath);
  await withServer(app, async base => {
    assert.equal((await fetch(base + '/health')).status, 503);
    assert.equal((await fetch(base + '/api/v1/news/search?q=')).status, 200);
  });
});
