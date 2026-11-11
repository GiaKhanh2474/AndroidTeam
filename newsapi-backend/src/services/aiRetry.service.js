const Article = require('../models/Article.model');
const { analyzeArticle, canAnalyzeNow } = require('./geminiAi.service');

const MAX_ATTEMPTS = 5;
function retryFields(ai, attempts, now = Date.now()) {
  const completed = ai.ai_used === true;
  return {
    ai_status: completed ? 'COMPLETED' : attempts >= MAX_ATTEMPTS ? 'FAILED' : 'PENDING',
    ai_attempts: attempts,
    next_retry_at: completed || attempts >= MAX_ATTEMPTS ? null
      : new Date(now + Math.min(24 * 60, 10 * 2 ** Math.max(0, attempts - 1)) * 60000),
  };
}

// Dependencies are injectable so recovery can be tested without credentials or MongoDB.
function createRetryPendingArticles({ model = Article, analyze = analyzeArticle,
  ready = canAnalyzeNow, pause = ms => new Promise(resolve => setTimeout(resolve, ms)) } = {}) {
  return async function retryPendingArticles() {
    const summary = { processed: 0, completed: 0, deferred: 0 };
    if (!ready()) return summary;
    const rows = await model.find({
      $or: [
        { ai_status: 'PENDING', next_retry_at: { $lte: new Date() } },
        { ai_status: 'PENDING', next_retry_at: null },
        // Old records have no provenance. Re-evaluate once, never infer failure from score 50.
        { ai_status: { $exists: false } },
      ],
    }).sort({ next_retry_at: 1, published_at: -1 }).limit(5).lean();
    const configuredDelay = Number(process.env.GEMINI_DELAY_MS);
    const delay = Number.isFinite(configuredDelay) && configuredDelay >= 0 ? configuredDelay : 6500;
    for (const row of rows) {
      if (!ready()) break;
      const ai = await analyze({ title: row.title, content: row.full_content,
        source_name: row.source_name, category_hint: row.category, feed_url: row.original_url });
      const fields = retryFields(ai, (row.ai_attempts || 0) + 1);
      if (ai.ai_used) {
        Object.assign(fields, { category: ai.category, main_grid_summary: ai.main_grid_summary,
          card_summary: ai.card_summary, ai_score: ai.ai_score, is_main_featured: ai.is_main_featured });
        summary.completed++;
      } else summary.deferred++;
      // On failure retain the existing content, score and summaries, including legacy articles.
      await model.updateOne({ _id: row._id }, { $set: fields });
      summary.processed++;
      if (ready() && delay > 0) await pause(delay);
    }
    return summary;
  };
}

module.exports = { retryFields, createRetryPendingArticles,
  retryPendingArticles: createRetryPendingArticles() };
