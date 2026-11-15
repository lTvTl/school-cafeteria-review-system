// Node built-in tests for popup event behavior; no new application dependency.
const assert = require('assert');
const fs = require('fs');
const vm = require('vm');
const categoryCode = fs.readFileSync('src/main/webapp/resources/js/menu-category.js', 'utf8');
function category(blocked) {
    const callbacks = {}, opened = [], xhrs = [], averages = {};
    const links = ['2', '5'].map(id => ({
        href: 'http://localhost:8080/controller/menu/' + id,
        getAttribute: () => id,
        addEventListener: (name, fn) => callbacks[id] = fn,
        closest: () => ({querySelector: () => averages[id] || (averages[id] = {textContent: '0.00'})})
    }));
    const win = {location: {origin: 'http://localhost:8080'}, screenX: 0, screenY: 0, outerWidth: 1440, outerHeight: 1000,
        open: (url, name, features) => { const popup = {closed: false, focusCount: 0, focus() {this.focusCount++;}}; opened.push({url, name, features, popup}); return blocked ? null : popup; },
        addEventListener: (name, fn) => callbacks[name] = fn};
    function Xhr() {xhrs.push(this); this.open = (method, url) => {this.method = method; this.url = url;}; this.setRequestHeader = () => {}; this.send = () => {};}
    vm.runInNewContext(categoryCode, {window: win, document: {querySelectorAll: () => links}, XMLHttpRequest: Xhr});
    return {callbacks, opened, xhrs, averages, click(id) {const event = {prevented: false, preventDefault() {this.prevented = true;}}; callbacks[id](event); return event;}};
}
let c = category(false);
assert(c.click('2').prevented);
assert(c.opened[0].features.includes('width=520,height=760'));
assert(c.opened[0].features.includes('scrollbars=yes,resizable=yes'));
assert(c.opened[0].url.includes('/controller/menu/2'));
assert(c.click('2').prevented); assert.strictEqual(c.opened.length, 1); assert.strictEqual(c.opened[0].popup.focusCount, 2);
c.click('5'); assert.strictEqual(c.opened.length, 2); assert.notStrictEqual(c.opened[0].name, c.opened[1].name);
c.opened[0].popup.closed = true; c.click('2'); assert.strictEqual(c.opened.length, 3);
assert.strictEqual(category(true).click('2').prevented, false);
const message = {origin: 'http://localhost:8080', source: c.opened[2].popup, data: {type: 'menu-review-saved', menuId: '2'}};
c.callbacks.message({...message, origin: 'https://attacker.example'});
c.callbacks.message({...message, source: {}});
c.callbacks.message({...message, data: {type: 'menu-review-saved', menuId: '<script>'}});
assert.strictEqual(c.xhrs.length, 0);
c.callbacks.message(message); assert.strictEqual(c.xhrs[0].url, 'http://localhost:8080/controller/menu/2/summary');
c.xhrs[0].status = 200; c.xhrs[0].responseText = '{"avgRating":4.25}'; c.xhrs[0].onload();
assert.strictEqual(c.averages['2'].textContent, '4.25');
c.callbacks.message(message); c.callbacks.message(message);
c.xhrs[2].status = 200; c.xhrs[2].responseText = '{"avgRating":3.5}'; c.xhrs[2].onload();
c.xhrs[1].status = 200; c.xhrs[1].responseText = '{"avgRating":5}'; c.xhrs[1].onload();
assert.strictEqual(c.averages['2'].textContent, '3.50');

const popupCode = fs.readFileSync('src/main/webapp/resources/js/menu-popup.js', 'utf8');
function popup(saved, hasParent) {
    const sent = [], callbacks = {}; let closed = false;
    const opener = hasParent ? {closed: false, postMessage: (data, origin) => sent.push({data, origin})} : null;
    const win = {opener, location: {origin: 'http://localhost:8080'}, close: () => closed = true};
    const document = {body: {getAttribute: key => key === 'data-review-saved' ? saved : '2'},
        getElementById: () => ({addEventListener: (name, fn) => callbacks.close = fn}),
        querySelectorAll: () => [], querySelector: () => ({href: '/controller/category'})};
    vm.runInNewContext(popupCode, {window: win, document});
    return {sent, callbacks, win, closed: () => closed};
}
assert.strictEqual(popup('false', true).sent.length, 0);
const p = popup('true', true); assert.strictEqual(p.sent.length, 1); assert.strictEqual(p.sent[0].origin, 'http://localhost:8080');
assert.strictEqual(p.sent[0].data.menuId, '2'); p.callbacks.close(); assert(p.closed());
const direct = popup('true', false); direct.callbacks.close(); assert.strictEqual(direct.win.location.href, '/controller/category');
console.log('PASS: popup dimensions, context path, reuse, separate windows, blocked fallback, origin/source/payload checks, stale response guard, committed notification, close/direct-page fallback');
