const { test } = require('node:test');
const assert = require('node:assert/strict');
const Article = require('../src/models/Article.model');
const MarketChart = require('../src/models/MarketChart.model');
const app = require('../server');

// Exercise real routes/serialization/error middleware; database access is isolated.
async function request(path, options) {
  const server = app.listen(0, '127.0.0.1');
  await new Promise(resolve => server.once('listening', resolve));
  try {
    const response = await fetch(`http://127.0.0.1:${server.address().port}${path}`, options);
    return { status: response.status, body: await response.json() };
  } finally {
    await new Promise((resolve, reject) => server.close(error => error ? reject(error) : resolve()));
  }
}
function query(rows, inspect = {}) {
  return {
    sort(value) { inspect.sort?.(value); return this; },
    skip(value) { inspect.skip?.(value); return this; },
    limit(value) { inspect.limit?.(value); return this; },
    async lean() { return rows; },
  };
}
const article = {
  article_id: 'sample', title: 'Tin tức', main_grid_summary: 'Short', card_summary: 'Long',
  full_content: 'Offline content', original_url: 'https://example.com/news',
  source_name: 'Source', category: 'KINH_TE', published_at: 1000,
  date_label: 'NGÀY 14/11/2026', ai_score: 90, is_main_featured: true,
};

test('invalid category is rejected before querying MongoDB', async t => {
  t.mock.method(Article, 'find', () => assert.fail('unexpected DB access'));
  for (const suffix of ['', '?category=INVALID']) {
    assert.equal((await request('/api/v1/news/by-date' + suffix)).status, 400);
  }
});

test('category pagination groups dates and omits full content', async t => {
  t.mock.method(Article, 'find', (filter, projection) => {
    assert.deepEqual(filter, { category: 'KINH_TE' });
    assert.equal(projection.full_content, undefined);
    return query([article, { ...article, article_id: 'second' },
      { ...article, article_id: 'older', date_label: 'NGÀY 13/11/2026' }], {
      sort: order => assert.deepEqual(order, { published_at: -1 }),
      skip: n => assert.equal(n, 3), limit: n => assert.equal(n, 3),
    });
  });
  const { status, body } = await request('/api/v1/news/by-date?category=kinh_te&page=2&limit=3');
  assert.equal(status, 200);
  assert.deepEqual(body.map(group => group.articles.length), [2, 1]);
  assert.equal(body[0].articles[0].full_content, null);
});

test('pagination bounds prevent negative offsets and unbounded page sizes', async t => {
  const offsets = [], limits = [];
  t.mock.method(Article, 'find', () => query([], {
    skip: n => offsets.push(n), limit: n => limits.push(n),
  }));
  await request('/api/v1/news/by-date?category=KINH_TE&page=-1&limit=0');
  await request('/api/v1/news/by-date?category=KINH_TE&page=999999&limit=999999');
  assert.deepEqual(offsets, [0, 9999900]);
  assert.deepEqual(limits, [20, 100]);
});

test('detail preserves offline content and returns 404 when absent', async t => {
  t.mock.method(Article, 'findOne', filter => {
    assert.deepEqual(filter, { article_id: 'sample' });
    return query(article);
  });
  const result = await request('/api/v1/news/detail/sample');
  assert.equal(result.status, 200);
  assert.equal(result.body.full_content, 'Offline content');
  assert.equal(result.body.image_url, null);
  t.mock.method(Article, 'findOne', () => query(null));
  assert.equal((await request('/api/v1/news/detail/missing')).status, 404);
});

test('Mongo ObjectId detail supports both legacy and public IDs', async t => {
  const id = '0123456789abcdef01234567';
  t.mock.method(Article, 'findOne', filter => {
    assert.deepEqual(filter, { $or: [{ article_id: id }, { _id: id }] });
    return query(article);
  });
  assert.equal((await request('/api/v1/news/detail/' + id)).status, 200);
});

test('empty search returns no rows without database access', async t => {
  t.mock.method(Article, 'find', () => assert.fail('unexpected DB access'));
  assert.deepEqual(await request('/api/v1/news/search?q=%20'), { status: 200, body: [] });
});

test('search escapes regex metacharacters and limits fallback results', async t => {
  let calls = 0;
  t.mock.method(Article, 'find', filter => {
    calls++;
    if (calls === 1) {
      assert.deepEqual(filter, { $text: { $search: 'a.*[b]' } });
      return query([]);
    }
    const regex = filter.$or[0].title;
    assert.equal(regex.test('a.*[b]'), true);
    assert.equal(regex.test('axxxb'), false);
    return query([article], { limit: n => assert.equal(n, 20) });
  });
  const result = await request('/api/v1/news/search?q=' + encodeURIComponent('a.*[b]'));
  assert.equal(result.status, 200);
  assert.equal(result.body[0].article_id, 'sample');
  assert.equal(calls, 2);
});

test('text search truncates excessive input and skips fallback on a match', async t => {
  let calls = 0;
  t.mock.method(Article, 'find', filter => {
    calls++;
    assert.equal(filter.$text.$search.length, 100);
    return query([article], { limit: n => assert.equal(n, 20) });
  });
  assert.equal((await request('/api/v1/news/search?q=' + 'x'.repeat(150))).status, 200);
  assert.equal(calls, 1);
});

test('sideline only queries BEN_LE and limits to fifteen articles', async t => {
  t.mock.method(Article, 'find', filter => {
    assert.deepEqual(filter, { category: 'BEN_LE' });
    return query([article], { limit: n => assert.equal(n, 15) });
  });
  assert.equal((await request('/api/v1/news/sideline')).body.length, 1);
});

test('market serializes missing history as empty and hides internal fields', async t => {
  t.mock.method(MarketChart, 'find', () => query([
    { symbol: 'USD_VND', history_points: null, display_order: 1 },
    { symbol: 'OIL_BRENT', history_points: [{ recorded_at: 100, price_value: 70, _id: 'private' }] },
  ], { sort: order => assert.deepEqual(order, { is_default_top: -1, display_order: 1 }) }));
  const { body } = await request('/api/v1/market/charts');
  assert.deepEqual(body[0].history_points, []);
  assert.equal(body[0].display_order, undefined);
  assert.deepEqual(body[1].history_points, [{ recorded_at: 100, price_value: 70 }]);
});

test('API returns JSON for malformed input, missing routes and disconnected health', async () => {
  assert.equal((await request('/health')).status, 503);
  assert.equal((await request('/unknown')).status, 404);
  const result = await request('/api/v1/users/check', {
    method: 'POST', headers: { 'content-type': 'application/json' }, body: '{broken',
  });
  assert.equal(result.status, 400);
  assert.equal(result.body.status, 'error');
});

test('database failures return a generic 500 without leaking details', async t => {
  t.mock.method(Article, 'findOne', () => ({ lean: async () => { throw new Error('private DB details'); } }));
  t.mock.method(console, 'error', () => {});
  const { status, body } = await request('/api/v1/news/detail/sample');
  assert.equal(status, 500);
  assert.equal(JSON.stringify(body).includes('private DB details'), false);
});
