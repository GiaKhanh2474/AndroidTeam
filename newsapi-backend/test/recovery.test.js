const { test } = require('node:test');
const assert = require('node:assert/strict');
const { createCycleRunner } = require('../src/jobs/cycleRunner');
const { retryFields, createRetryPendingArticles } = require('../src/services/aiRetry.service');

test('whole cycle rejects overlap during market, retry and RSS, then unlocks', async () => {
  let release;
  const gate = () => new Promise(resolve => { release = resolve; });
  const runner = createCycleRunner({ market:gate, retryAi:gate, news:gate });
  const first = runner.runFullCycle();
  for (let stage = 0; stage < 3; stage++) {
    assert.equal(runner.isCycleRunning(),true);
    assert.equal((await runner.runFullCycle()).skipped,true);
    release({ok:true});
    await new Promise(resolve=>setImmediate(resolve));
  }
  assert.deepEqual(Object.keys(await first),['market','ai_retry','news']);
  assert.equal(runner.isCycleRunning(),false);
});

test('step failures do not strand the cycle lock or prevent subsequent stages', async () => {
  let newsCalls=0;
  const runner=createCycleRunner({market:async()=>{throw Error('market unavailable');},
    retryAi:async()=>{throw Error('database unavailable');}, news:async()=>++newsCalls});
  assert.equal((await runner.runFullCycle()).market.error,'market unavailable');
  assert.equal(runner.isCycleRunning(),false);
  await runner.runFullCycle();
  assert.equal(newsCalls,2);
});

function fakeModel(rows) {
  const updates=[];
  return {updates,find(filter){
    assert.ok(filter.$or.some(item=>item.ai_status?.$exists===false));
    return {sort:()=>({limit:n=>{assert.equal(n,5);return {lean:async()=>rows};}})};
  },async updateOne(filter,update){updates.push({filter,update});}};
}
const article={_id:'existing',title:'Title',full_content:'Saved full content',category:'KINH_TE',ai_attempts:1};
const success={ai_used:true,category:'CONG_NGHE',main_grid_summary:'New main summary',card_summary:'New card summary',ai_score:90,is_main_featured:true};

test('Gemini recovery updates the existing document without duplicate insertion', async()=>{
  const model=fakeModel([article]);
  const retry=createRetryPendingArticles({model,ready:()=>true,pause:async()=>{},analyze:async input=>{
    assert.equal(input.content,article.full_content);return success;
  }});
  assert.equal((await retry()).completed,1);
  assert.deepEqual(model.updates[0].filter,{_id:'existing'});
  const data=model.updates[0].update.$set;
  assert.equal(data.ai_status,'COMPLETED');assert.equal(data.next_retry_at,null);
  assert.equal(data.ai_score,90);assert.equal(data.is_main_featured,true);
  assert.equal(data.full_content,undefined);
});

test('failed retries preserve legacy summaries and schedule bounded backoff', async()=>{
  const model=fakeModel([article]);
  await createRetryPendingArticles({model,ready:()=>true,pause:async()=>{},analyze:async()=>({ai_used:false})})();
  const data=model.updates[0].update.$set;
  assert.equal(data.ai_status,'PENDING');assert.equal(data.ai_attempts,2);
  assert.ok(data.next_retry_at>Date.now());assert.equal(data.main_grid_summary,undefined);
  assert.equal(data.ai_score,undefined);
  assert.equal(retryFields({ai_used:false},5).ai_status,'FAILED');
  assert.equal(retryFields({ai_used:false},5).next_retry_at,null);
});

test('missing credentials or quota cooldown does not consume retries',async()=>{
  const retry=createRetryPendingArticles({ready:()=>false,model:{find(){throw Error('must not query');}}});
  assert.equal((await retry()).processed,0);
});

test('quota cooldown after first article stops the remaining batch',async()=>{
  let available=true;
  const model=fakeModel([article,{...article,_id:'second'}]);
  const retry=createRetryPendingArticles({model,ready:()=>available,pause:async()=>{},analyze:async()=>{
    available=false;return {ai_used:false};
  }});
  assert.equal((await retry()).processed,1);assert.equal(model.updates.length,1);
});
