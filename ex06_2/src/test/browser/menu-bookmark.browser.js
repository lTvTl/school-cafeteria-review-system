const assert=require('assert');
const {chromium}=require(process.env.PLAYWRIGHT_MODULE||'playwright');
const base=process.env.BOOKMARK_BASE_URL, id=process.env.BOOKMARK_TEST_MENU;
const name=process.env.BOOKMARK_TEST_MENU_NAME;
const buttonSelector='.bookmark-button[data-menu-id="'+id+'"]';
async function login(page,user){
    await page.goto(base+'/customLogin');await page.locator('input[name="username"]').fill(user);await page.locator('input[name="password"]').fill(process.env.BOOKMARK_TEST_PASSWORD);
    await Promise.all([page.waitForNavigation(),page.locator('form[action$="/login"] button[type="submit"]').click()]);assert(page.url().includes('/main'));
}
async function category(page){await page.goto(base+'/category?type=main');assert.deepStrictEqual(await page.locator('.menu-table thead th').allTextContents(),['음식명','평균 별점','북마크 수']);}
async function state(page,bookmarked,count){
    await page.waitForFunction(({selector,bookmarked,count})=>{
        const b=document.querySelector(selector);return b && b.getAttribute('aria-pressed')===String(bookmarked)&&b.closest('td').querySelector('.bookmark-count').textContent===String(count)&&!b.disabled;
    },{selector:buttonSelector,bookmarked,count});
}
async function csrf(page){return page.locator('.food-category').getAttribute('data-csrf-token');}
async function post(context,path,token,extra={}){return context.request.post(base+path,{headers:token?{'X-CSRF-TOKEN':token}:{},form:extra,maxRedirects:0});}
(async()=>{
    const browser=await chromium.launch({headless:true,executablePath:process.env.CHROME_EXECUTABLE});
    try {
        const guest=await browser.newContext({viewport:{width:1200,height:900}}), g=await guest.newPage();await category(g);
        const guestPages=guest.pages().length;await g.locator(buttonSelector).click();assert.equal(guest.pages().length,guestPages);assert(await g.locator('.bookmark-login').isVisible());await state(g,false,0);
        let response=await post(guest,'/menu/'+id+'/bookmark/add',await csrf(g));assert.equal(response.status(),302);assert(response.headers().location.endsWith('/customLogin'));

        const alice=await browser.newContext({viewport:{width:1200,height:900}}), page=await alice.newPage();const errors=[];page.on('pageerror',e=>errors.push(e.message));await login(page,process.env.BOOKMARK_TEST_ALICE);
        const homeReviewBefore=await page.locator('.daily-rating-summary').innerText();
        assert.equal(await page.locator('.bookmark-button').count(),0);assert.equal(await page.locator('.diet-list li').filter({hasText:name}).locator('.bookmark-heart').count(),0);
        await category(page);
        const categoryUrl=page.url(),pageCount=alice.pages().length;let release,requests=0,signal;
        const reached=new Promise(resolve=>{signal=resolve;});
        await page.route('**/bookmark/add',async route=>{requests++;signal();await new Promise(resolve=>{release=resolve;});await route.continue();});
        await page.locator(buttonSelector).click();await reached;assert(await page.locator(buttonSelector).isDisabled());
        await page.locator(buttonSelector).evaluate(b=>b.dispatchEvent(new MouseEvent('click',{bubbles:true})));assert.equal(requests,1);release();await state(page,true,1);await page.unroute('**/bookmark/add');
        assert.equal(page.url(),categoryUrl);assert.equal(alice.pages().length,pageCount);
        assert(await page.locator(buttonSelector+' .fa-heart').count());await page.reload();await state(page,true,1);
        response=await post(alice,'/menu/'+id+'/bookmark/add',await csrf(page),{userid:process.env.BOOKMARK_TEST_BOB});assert.equal(response.status(),200);assert.equal((await response.json()).bookmarkCnt,1);
        response=await post(alice,'/menu/'+id+'/bookmark/remove',null);assert(response.status()===403||(response.status()===302&&response.headers().location.endsWith('/accessError')));
        response=await post(alice,'/menu/9223372036854775807/bookmark/add',await csrf(page));assert.equal(response.status(),404);
        response=await alice.request.get(base+'/menu/'+id+'/bookmark/add');assert.equal(response.status(),405);
        await page.route('**/bookmark/remove',route=>route.fulfill({status:500,contentType:'application/json',body:'{"message":"test failure"}'}));
        await page.locator(buttonSelector).click();await state(page,true,1);assert(await page.locator('.bookmark-feedback').isVisible());await page.unroute('**/bookmark/remove');

        const [popup]=await Promise.all([page.waitForEvent('popup'),page.locator('.menu-popup-link[data-menu-id="'+id+'"]').click()]);await popup.waitForLoadState();
        assert.equal(popup.viewportSize().width,520);assert.equal(popup.viewportSize().height,760);
        const reviewForm=popup.locator('.write-section .review-form');await reviewForm.locator('.star-picker label').nth(3).click();await reviewForm.locator('textarea').fill('북마크와 별개인 음식 리뷰');
        await Promise.all([popup.waitForNavigation(),reviewForm.locator('button[type="submit"]').click()]);
        await page.waitForFunction(id=>document.querySelector('tr[data-menu-id="'+id+'"] .menu-average').textContent==='4.00',id);await state(page,true,1);await popup.close();
        await page.goto(base+'/main');assert.equal(await page.locator('.bookmark-button').count(),0);
        assert.equal(await page.locator('.daily-rating-summary').innerText(),homeReviewBefore);
        const meal=page.locator('.diet-list li').filter({hasText:name});assert.equal(await meal.locator('.bookmark-heart').count(),1);assert.equal(await meal.locator('.bookmark-heart button,.bookmark-heart a').count(),0);

        const bob=await browser.newContext(), b=await bob.newPage();await login(b,process.env.BOOKMARK_TEST_BOB);await category(b);await state(b,false,1);
        await b.locator(buttonSelector).click();await state(b,true,2);await category(page);await state(page,true,2);
        await page.locator(buttonSelector).focus();await page.keyboard.press('Enter');await state(page,false,1);await b.reload();await state(b,true,1);
        response=await post(alice,'/menu/'+id+'/bookmark/remove',await csrf(page));assert.equal((await response.json()).bookmarkCnt,1);
        await b.locator(buttonSelector).click();await state(b,false,0);await page.reload();await state(page,false,0);
        for(const type of ['side','dessert','drink','etc','main']){await page.locator('.category-buttons a[href$="type='+type+'"]').click();assert.equal(await page.locator('.category-buttons [aria-current="page"]').getAttribute('href'),'/controller/category?type='+type);}
        await state(page,false,0);await page.locator(buttonSelector).click();await state(page,true,1);
        await page.screenshot({path:'target/bookmark-check/category-desktop.png',fullPage:true});
        await page.locator(buttonSelector).click();await state(page,false,0);

        const mobile=await browser.newContext({storageState:await alice.storageState(),viewport:{width:390,height:844},isMobile:true,hasTouch:true});const touch=await mobile.newPage();await category(touch);
        await touch.locator(buttonSelector).tap();await state(touch,true,1);
        assert(await touch.evaluate(()=>document.documentElement.scrollWidth<=window.innerWidth+1));
        assert(await touch.locator('.table-responsive').evaluate(el=>el.scrollWidth<=el.clientWidth+1),'All three columns, including bookmark counts, fit the mobile table');
        assert(await touch.locator('tr[data-menu-id="'+id+'"] .menu-name').evaluate(el=>getComputedStyle(el).whiteSpace==='normal'&&el.scrollWidth<=el.clientWidth+1),'Long food names wrap instead of overlapping ratings');
        await touch.screenshot({path:'target/bookmark-check/category-mobile.png',fullPage:true});
        await touch.locator(buttonSelector).tap();await state(touch,false,0);await mobile.close();
        await page.goto(base+'/main');assert.equal(await page.locator('.diet-list li').filter({hasText:name}).locator('.bookmark-heart').count(),0);assert.equal(await page.locator('.bookmark-button').count(),0);
        assert.deepStrictEqual(errors,[]);
        for(const [context,p] of [[alice,page],[bob,b]]){await category(p);await context.request.post(base+'/customLogout',{form:{_csrf:await csrf(p)},maxRedirects:0});}
        console.log('PASS: three columns, five categories, guest guidance/security, POST+CSRF/404/405, duplicate clicks, failure preservation, persistent personal hearts, cross-user counts, 520x760 popup, unchanged review average flow, read-only home hearts, keyboard and mobile touch');
    } finally {await browser.close();}
})().catch(error=>{console.error(error);process.exitCode=1;});
