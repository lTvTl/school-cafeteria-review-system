const assert = require('assert');
const fs = require('fs');
const path = require('path');
const vm = require('vm');
const source = fs.readFileSync(path.resolve(__dirname, '../../main/webapp/resources/js/home-menu.js'), 'utf8');
let reduced = false;
function card() {
    const summary = {addEventListener(type, handler) { this.click = handler; }};
    const body = {
        stop(clear, finish) { assert(clear); assert.strictEqual(finish, false); this.done = null; },
        hide() { this.hidden = true; },
        css(name, value) { assert.strictEqual(name, 'display'); assert.strictEqual(value, ''); this.hidden = false; },
        slideDown(duration, done) { this.direction = 'down'; this.duration = duration; this.done = done; },
        slideUp(duration, done) { this.direction = 'up'; this.duration = duration; this.done = done; }
    };
    return {open: true, body, summary, querySelector(s) { return s === 'summary' ? summary : body; }};
}
const cards = [card(), card()];
vm.runInNewContext(source, {
    jQuery: body => body,
    document: {querySelectorAll: () => cards},
    window: {matchMedia: () => ({matches: reduced})}
});
function click(c) { let prevented = false; c.summary.click({preventDefault() { prevented = true; }}); assert(prevented); }
const first = cards[0];
click(first);
assert(first.open, 'Keep native details open until slide-up completes');
assert.strictEqual(first.body.direction, 'up');
assert.strictEqual(first.body.duration, 250);
first.body.done();
assert.strictEqual(first.open, false);
click(first);
assert(first.open);
assert.strictEqual(first.body.direction, 'down');
click(first);
assert.strictEqual(first.body.direction, 'up', 'Rapid clicks reverse the requested state');
first.body.done();
assert.strictEqual(first.open, false);
assert(cards[1].open, 'Cards toggle independently');
reduced = true;
click(first);
assert.strictEqual(first.body.duration, 0);
first.body.done();
assert(first.open);
console.log('PASS: animated close/open, rapid reversal, independent cards, reduced motion');
