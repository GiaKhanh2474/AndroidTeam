/** Single-process lock acquired synchronously, before any asynchronous work. */
function createCycleRunner({ market, retryAi, news, rssBusy = () => false }) {
  let running = false;
  const isCycleRunning = () => running || rssBusy();
  async function runFullCycle() {
    if (isCycleRunning()) return { skipped: true, reason: 'A crawl cycle is already running' };
    running = true;
    try {
      const result = {};
      for (const [name, step] of [['market', market], ['ai_retry', retryAi], ['news', news]]) {
        try { result[name] = await step(); }
        catch (error) { result[name] = { error: error.message }; }
      }
      return result;
    } finally { running = false; }
  }
  return { runFullCycle, isCycleRunning };
}
module.exports = { createCycleRunner };
