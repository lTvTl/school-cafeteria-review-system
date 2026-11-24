// Exercise shared component events with independently scoped native-radio groups.
const assert = require('assert');
const fs = require('fs');
const vm = require('vm');
function target() {
    return {events: {}, addEventListener(name, fn) { this.events[name] = fn; }};
}
function picker(selected) {
    const group = target(), form = target();
    group.inputs = [1,2,3,4,5].map(value => {
        const label = target(), icon = {};
        label.classList = {toggle(name, enabled) { label.filled = enabled; }};
        label.querySelector = () => icon;
        return {value: String(value), checked: value === selected, nextElementSibling: label, form};
    });
    group.querySelectorAll = () => group.inputs;
    group.stars = () => group.inputs.map(i => i.nextElementSibling.querySelector().textContent).join('');
    group.hover = score => group.inputs[score - 1].nextElementSibling.events.pointerenter({pointerType: 'mouse'});
    group.choose = score => { group.inputs.forEach(i => { i.checked = Number(i.value) === score; }); group.events.change(); };
    group.score = () => Number((group.inputs.find(i => i.checked) || {value: 0}).value);
    return group;
}
const main = picker(0), popup = picker(0), edit = picker(4);
vm.runInNewContext(fs.readFileSync('src/main/webapp/resources/js/star-rating.js','utf8'), {
    document: {querySelectorAll: selector => selector === '.star-picker' ? [main,popup,edit] : []},
    window: {setTimeout: fn => fn()}
});
for (const group of [main,popup]) {
    assert.strictEqual(group.stars(), '☆☆☆☆☆');
    group.hover(3); assert.strictEqual(group.stars(),'★★★☆☆'); assert.strictEqual(group.score(),0);
    group.events.pointerleave(); assert.strictEqual(group.stars(),'☆☆☆☆☆');
    group.choose(3); group.events.pointerleave(); assert.strictEqual(group.stars(),'★★★☆☆');
    group.choose(5); group.hover(2); assert.strictEqual(group.stars(),'★★☆☆☆'); assert.strictEqual(group.score(),5);
    group.events.pointerleave(); assert.strictEqual(group.stars(),'★★★★★');
    group.hover(2); group.choose(2); assert.strictEqual(group.stars(),'★★☆☆☆'); assert.strictEqual(group.score(),2);
    group.inputs[4].nextElementSibling.events.pointerenter({pointerType:'touch'});
    assert.strictEqual(group.stars(),'★★☆☆☆', 'Touch must not leave a sticky preview');
    group.choose(5); assert.strictEqual(group.stars(),'★★★★★', 'Native touch/radio change commits');
    group.hover(1); group.events.keydown(); assert.strictEqual(group.stars(),'★★★★★');
    group.choose(4); assert.strictEqual(group.stars(),'★★★★☆', 'Native arrow-key change paints selection');
}
assert.strictEqual(edit.stars(),'★★★★☆');
edit.hover(2); edit.events.pointercancel(); assert.strictEqual(edit.stars(),'★★★★☆');
edit.choose(1); assert.strictEqual(main.score(),4); assert.strictEqual(popup.score(),4);
edit.inputs.forEach(i => { i.checked = Number(i.value) === 4; }); edit.inputs[0].form.events.reset();
assert.strictEqual(edit.stars(),'★★★★☆');
console.log('PASS: initial/edit values, hover without commit, leave restoration, click, touch, keyboard change, independent groups, reset');
