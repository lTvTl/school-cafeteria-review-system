const assert = require('assert');
const {chromium} = require(process.env.PLAYWRIGHT_MODULE || 'playwright');
const base = process.env.REVIEW_BASE_URL;
const menuId = process.env.REVIEW_TEST_MENU;
async function login(page, user) {
    await page.goto(base + '/customLogin');
    await page.locator('input[name="username"]').fill(user);
    await page.locator('input[name="password"]').fill(process.env.REVIEW_TEST_PASSWORD);
    await Promise.all([page.waitForNavigation(), page.locator('form[action$="/login"] button[type="submit"]').click()]);
    assert(page.url().includes('/main'), 'Login must reach main');
}
async function stars(picker) { return (await picker.locator('[data-star-icon]').allTextContents()).join(''); }
async function rating(page, picker, initial) {
    assert.strictEqual(await stars(picker), '★'.repeat(initial) + '☆'.repeat(5-initial));
    await picker.locator('label').nth(2).hover(); assert.strictEqual(await stars(picker),'★★★☆☆');
    assert.strictEqual(await picker.locator('input:checked').count(),initial ? 1 : 0);
    await page.mouse.move(0,0); assert.strictEqual(await stars(picker),'★'.repeat(initial)+'☆'.repeat(5-initial));
    await picker.locator('label').nth(4).click();
    await picker.locator('label').nth(1).hover(); assert.strictEqual(await stars(picker),'★★☆☆☆');
    assert.strictEqual(await picker.locator('input:checked').inputValue(),'5');
    await page.mouse.move(0,0); assert.strictEqual(await stars(picker),'★★★★★');
    await picker.locator('label').nth(1).click(); assert.strictEqual(await picker.locator('input:checked').inputValue(),'2');
    await picker.locator('input:checked').focus(); await page.keyboard.press('ArrowRight');
    assert.strictEqual(await picker.locator('input:checked').inputValue(),'3'); assert.strictEqual(await stars(picker),'★★★☆☆');
}
async function submit(page, form) {
    await Promise.all([page.waitForNavigation(),form.locator('button[type="submit"]').click()]);
}
(async () => {
    const browser = await chromium.launch({headless:true, executablePath:process.env.CHROME_EXECUTABLE});
    try {
        const context=await browser.newContext({viewport:{width:1280,height:1000}});
        const page=await context.newPage();
        const errors=[]; page.on('pageerror', e=>errors.push(e.message));
        let response=await page.goto(base+'/main'); assert.strictEqual(response.status(),200);
        assert.strictEqual(await page.locator('#daily-reviews .review-form').count(),0,'Anonymous read only');
        await login(page,process.env.REVIEW_TEST_ALICE);
        const beforeMenu=await (await context.request.get(base+'/menu/'+menuId+'/summary')).json();
        const compose=page.locator('#daily-reviews .daily-review-form');
        await compose.locator('textarea').fill('whole meal');
        assert.strictEqual(await compose.evaluate(form=>form.checkValidity()),false,'Unselected stars block submit');
        await rating(page,compose.locator('.star-picker'),0);
        await compose.locator('.star-picker label').nth(3).click();
        const text="<script>window.__dailyXss=1</script> 오늘의 전체 학식";
        await compose.locator('textarea').fill(text);
        await page.locator('.meal-card summary').first().click();
        await page.waitForFunction(()=>!document.querySelector('.meal-card').open);
        await submit(page,compose);
        assert(page.url().endsWith('/main#daily-reviews')); assert.strictEqual(await page.locator('.meal-card').first().getAttribute('open'),null);
        assert.strictEqual(await page.locator('#daily-own-review .daily-review-text').textContent(),text);
        assert.strictEqual(await page.evaluate(()=>window.__dailyXss),undefined);
        assert(await page.locator('#daily-reviews .alert-success').count());
        assert.deepStrictEqual(await (await context.request.get(base+'/menu/'+menuId+'/summary')).json(),beforeMenu);
        const dailyBeforeFood=await page.locator('.daily-rating-summary').innerText();
        const own=page.locator('#daily-own-review'); await own.locator('summary').click();
        await rating(page,own.locator('.star-picker'),4);
        await own.locator('.star-picker label').nth(3).click();
        await submit(page,own.locator('form.review-form'));
        const beforeDuplicate=await page.locator('.daily-rating-summary').innerText();
        const csrf=await page.locator('#daily-own-review input[name="_csrf"]').first().inputValue();
        response=await context.request.post(base+'/daily-reviews',{form:{_csrf:csrf,starScore:'1',reviewText:'duplicate',serveDate:'2000-01-01',userid:process.env.REVIEW_TEST_BOB},maxRedirects:0});
        assert.strictEqual(response.status(),302); await page.goto(base+'/main');
        assert.strictEqual(await page.locator('.daily-rating-summary').innerText(),beforeDuplicate);

        const category=await context.newPage(); await category.goto(base+'/category?type=main');
        const [popup]=await Promise.all([category.waitForEvent('popup'),category.locator('.menu-popup-link[data-menu-id="'+menuId+'"]').click()]);
        await popup.waitForLoadState(); assert.strictEqual(popup.viewportSize().width,520); assert.strictEqual(popup.viewportSize().height,760);
        const menuForm=popup.locator('.write-section .review-form');
        await rating(popup,menuForm.locator('.star-picker'),0);
        await menuForm.locator('.star-picker label').nth(4).click(); await menuForm.locator('textarea').fill('individual food');
        await submit(popup,menuForm);
        assert.strictEqual((await (await context.request.get(base+'/menu/'+menuId+'/summary')).json()).avgRating,5);
        await category.waitForFunction(id=>document.querySelector('.menu-popup-link[data-menu-id="'+id+'"]').closest('tr').textContent.includes('5.00'),menuId);
        await page.goto(base+'/main'); assert.strictEqual(await page.locator('.daily-rating-summary').innerText(),dailyBeforeFood);
        await popup.locator('.edit-review summary').click(); await rating(popup,popup.locator('.edit-review .star-picker'),5);
        assert.strictEqual(await popup.locator('.write-section input:checked').count(),0,'Separate forms keep independent radio groups');
        await popup.locator('.edit-review textarea').fill('updated food'); await submit(popup,popup.locator('.edit-review form'));
        assert.strictEqual((await (await context.request.get(base+'/menu/'+menuId+'/summary')).json()).avgRating,3);
        await page.goto(base+'/main'); assert.strictEqual(await page.locator('.daily-rating-summary').innerText(),dailyBeforeFood);
        await page.screenshot({path:'target/server-tools/daily-review-desktop.png',fullPage:true});

        const mobile=await browser.newContext({viewport:{width:390,height:844},isMobile:true,hasTouch:true});
        const touch=await mobile.newPage(); await login(touch,process.env.REVIEW_TEST_BOB);
        const touchForm=touch.locator('#daily-reviews .review-form');
        await touchForm.locator('.star-picker label').nth(2).tap();
        assert.strictEqual(await touchForm.locator('input:checked').inputValue(),'3');
        await touchForm.locator('textarea').fill('mobile whole meal');
        await touch.screenshot({path:'target/server-tools/daily-review-mobile.png',fullPage:true});
        await submit(touch,touchForm);
        assert.strictEqual(await touch.locator('#daily-own-review .daily-review-score').textContent(),'★ 3점');
        await mobile.close();

        page.on('dialog',dialog=>dialog.accept());
        await submit(page,page.locator('#daily-own-review .daily-remove-review'));
        assert.strictEqual(await page.locator('#daily-own-review').count(),0);
        assert.strictEqual((await (await context.request.get(base+'/menu/'+menuId+'/summary')).json()).avgRating,3);
        const again=page.locator('#daily-reviews .review-form'); await again.locator('.star-picker label').nth(1).click(); await again.locator('textarea').fill('registered again'); await submit(page,again);
        assert.strictEqual(await page.locator('#daily-own-review .daily-review-score').textContent(),'★ 2점');
        assert.deepStrictEqual(errors,[]);
        console.log('PASS: real Chrome desktop/mobile; guest read, CSRF forms, PRG, hover/restore/click/arrows/touch, edit initial values, duplicate guard, XSS escaping, independent review averages, parent refresh, deletion/re-registration, meal collapse persistence');
    } finally { await browser.close(); }
})().catch(error=>{console.error(error);process.exitCode=1;});
