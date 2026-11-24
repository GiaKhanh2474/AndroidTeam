const cheerio = require('cheerio');

/** Decode RSS/legacy HTML entities into plain text, never interpret literal tags. */
function decodeArticleText(value) {
  if (typeof value !== 'string' || !value.includes('&')) return value;
  let text = value;
  // Some feeds escape entities twice. Bound the work for malformed/nested input.
  for (let pass = 0; pass < 3 && text.includes('&'); pass++) {
    const decoded = cheerio.load(text.replace(/</g, '&lt;'), {}, false).text();
    if (decoded === text) break;
    text = decoded;
  }
  return text;
}

module.exports = { decodeArticleText };
