const { test } = require('node:test');
const assert = require('node:assert/strict');
const { decodeArticleText } = require('../src/utils/articleText');
const { toArticleDto } = require('../src/controllers/news.controller');
const { _internal: ai } = require('../src/services/geminiAi.service');
const { _internal: crawler } = require('../src/services/rssCrawler.service');
const Article = require('../src/models/Article.model');
const axios = require('axios');

test('Vietnamese HTML entities from the reported headlines become readable text', () => {
  assert.equal(decodeArticleText('Văn h&oacute;a chia sẻ tri thức nội bộ tại GELEX'),
    'Văn hóa chia sẻ tri thức nội bộ tại GELEX');
  assert.equal(decodeArticleText('đ&aacute;nh gi&aacute; giao th&ocirc;ng s&acirc;n bay'),
    'đánh giá giao thông sân bay');
});

test('numeric and nested entities decode without dropping literal text or newlines', () => {
  assert.equal(decodeArticleText('gi&#225; &amp; &#x111;ầu &amp;agrave;'), 'giá & đầu à');
  assert.equal(decodeArticleText('A < B\n<script>literal</script> &lt;b&gt; &unknown;'),
    'A < B\n<script>literal</script> <b> &unknown;');
  assert.equal(decodeArticleText('Tin tiếng Việt bình thường'), 'Tin tiếng Việt bình thường');
  assert.equal(decodeArticleText(null), null);
});

test('DTO cleans historical records without modifying database values or URL fields', () => {
  const row = { title: 'Văn h&oacute;a', card_summary: 'gi&aacute;', main_grid_summary: 'dầu &amp; USD',
    full_content: 'D&ograve;ng 1\nD&ograve;ng 2', source_name: 'B&aacute;o',
    original_url: 'https://example.com/?a=1&amp;b=2' };
  const dto = toArticleDto(row);
  assert.equal(dto.title, 'Văn hóa');
  assert.equal(dto.card_summary, 'giá');
  assert.equal(dto.main_grid_summary, 'dầu & USD');
  assert.equal(dto.full_content, 'Dòng 1\nDòng 2');
  assert.equal(dto.source_name, 'Báo');
  assert.equal(dto.original_url, row.original_url);
  assert.equal(row.title, 'Văn h&oacute;a');
  assert.equal(toArticleDto(row, { includeContent: false }).full_content, null);
});

test('AI preprocessing handles accented entities and strips actual HTML', () => {
  assert.equal(ai.stripHtml('<p>Gi&aacute; dầu&nbsp;v&agrave; USD</p>'), 'Giá dầu và USD');
});

test('crawler decodes RSS title and fallback content before saving new articles', async t => {
  const key = process.env.GEMINI_API_KEY;
  delete process.env.GEMINI_API_KEY;
  t.after(() => { if (key === undefined) delete process.env.GEMINI_API_KEY; else process.env.GEMINI_API_KEY = key; });
  t.mock.method(Article, 'exists', async () => false);
  t.mock.method(axios, 'get', async () => ({ data: '<html></html>' }));
  let saved;
  t.mock.method(Article, 'create', async row => { saved = row; });
  await crawler.processFeedItem({ link: 'https://example.com/news', title: 'Văn h&oacute;a',
    rawDescription: '<p>Gi&aacute; dầu v&agrave; USD</p>', isoDate: '2026-11-24T01:00:00Z' },
    { source_name: 'Test', category_hint: 'KINH_TE', url: 'https://example.com/rss' });
  assert.equal(saved.title, 'Văn hóa');
  assert.equal(saved.full_content, 'Giá dầu và USD');
  assert.equal(saved.card_summary, 'Giá dầu và USD');
});
