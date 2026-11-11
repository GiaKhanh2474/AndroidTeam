const { test } = require('node:test');
const assert = require('node:assert/strict');
const { getModelName } = require('../src/config/gemini');
const { fallbackAnalyze, _internal } = require('../src/services/geminiAi.service');
const Article = require('../src/models/Article.model');
const { getMainGrids } = require('../src/controllers/news.controller');
const { FEEDS } = require('../src/services/rssCrawler.service');

test('Gemini defaults to current Flash and supports a deployment override', () => {
  const old = process.env.GEMINI_MODEL;
  try {
    delete process.env.GEMINI_MODEL;
    assert.equal(getModelName(), 'gemini-3.8-flash');
    process.env.GEMINI_MODEL = '   ';
    assert.equal(getModelName(), 'gemini-3.8-flash');
    process.env.GEMINI_MODEL = ' gemini-flash-latest ';
    assert.equal(getModelName(), 'gemini-flash-latest');
  } finally {
    if (old === undefined) delete process.env.GEMINI_MODEL;
    else process.env.GEMINI_MODEL = old;
  }
});

test('AI summaries reject undersized output; fallback retains 18/45 words', () => {
  const words = n => Array.from({length:n}, (_,i) => `word${i}`).join(' ');
  assert.equal(_internal.normalizeAiResult({ main_grid_summary:words(14), card_summary:words(40) }, {}), null);
  assert.equal(_internal.normalizeAiResult({ main_grid_summary:words(18), card_summary:words(34) }, {}), null);
  const result = _internal.normalizeAiResult({main_grid_summary:words(25), card_summary:words(60), ai_score:90, category:'KINH_TE'}, {});
  assert.equal(_internal.countWords(result.main_grid_summary),20);
  assert.equal(_internal.countWords(result.card_summary),50);
  assert.equal(result.is_main_featured,true);
  const fallback = fallbackAnalyze({ content:words(80), category_hint:'KINH_TE' });
  assert.equal(_internal.countWords(fallback.main_grid_summary),18);
  assert.equal(_internal.countWords(fallback.card_summary),45);
});

test('main grid queries only featured articles sorted by score, up to three per category', async () => {
  const old = Article.find;
  const calls=[];
  try {
    Article.find = filter => {
      calls.push(filter);
      assert.equal(filter.is_main_featured,true);
      return { sort(order) {
        assert.deepEqual(order,{ai_score:-1,published_at:-1});
        return { limit(count) { assert.equal(count,3); return {lean:async()=>[]}; } };
      } };
    };
    let body;
    await getMainGrids({}, {json:value=>{body=value;}});
    assert.deepEqual(calls.map(c=>c.category).sort(),['CHINH_TRI','CONG_NGHE','KINH_TE']);
    assert.deepEqual(body,{kinhTe:[],chinhTri:[],congNghe:[]});
  } finally { Article.find=old; }
});

test('crawler includes all four specified publishers', () => {
  const sources = new Set(FEEDS.map(f=>f.source_name));
  for (const source of ['VnExpress','Tuổi Trẻ','CafeF','Thanh Niên']) assert.ok(sources.has(source));
});
