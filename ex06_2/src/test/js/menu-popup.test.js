// Node built-in tests for popup event behavior; no new application dependency.
var assert = require('assert');
var fs = require('fs');
var vm = require('vm');
var categoryCode = fs.readFileSync('src/main/webapp/resources/js/menu-category.js', 'utf8');

function category(blocked) {
    var callbacks = {}, opened = [], xhrs = [], averages = {};
    var links = ['2', '5'].map(function (id) {
        return {
            href: 'http://localhost:8080/controller/menu/' + id,
            getAttribute: function () { return id; },
            addEventListener: function (name, fn) { callbacks[id] = fn; },
            closest: function () {
                return {
                    querySelector: function () {
                        return averages[id] || (averages[id] = { textContent: '0.00' });
                    }
                };
            }
        };
    });
    var win = {
        location: { origin: 'http://localhost:8080' },
        screenX: 0,
        screenY: 0,
        outerWidth: 1440,
        outerHeight: 1000,
        open: function (url, name, features) {
            var popupWin = {
                closed: false,
                focusCount: 0,
                focus: function () { this.focusCount++; }
            };
            opened.push({ url: url, name: name, features: features, popup: popupWin });
            return blocked ? null : popupWin;
        },
        addEventListener: function (name, fn) { callbacks[name] = fn; }
    };
    function Xhr() {
        xhrs.push(this);
        this.open = function (method, url) { this.method = method; this.url = url; };
        this.setRequestHeader = function () {};
        this.send = function () {};
    }
    vm.runInNewContext(categoryCode, {
        window: win,
        document: { querySelectorAll: function () { return links; } },
        XMLHttpRequest: Xhr
    });
    return {
        callbacks: callbacks,
        opened: opened,
        xhrs: xhrs,
        averages: averages,
        click: function (id) {
            var event = {
                prevented: false,
                preventDefault: function () { this.prevented = true; }
            };
            callbacks[id](event);
            return event;
        }
    };
}

var c = category(false);
assert(c.click('2').prevented);
assert(c.opened[0].features.includes('width=520,height=760'));
assert(c.opened[0].features.includes('scrollbars=yes,resizable=yes'));
assert(c.opened[0].url.includes('/controller/menu/2'));
assert(c.click('2').prevented);
assert.strictEqual(c.opened.length, 1);
assert.strictEqual(c.opened[0].popup.focusCount, 2);
c.click('5');
assert.strictEqual(c.opened.length, 2);
assert.notStrictEqual(c.opened[0].name, c.opened[1].name);
c.opened[0].popup.closed = true;
c.click('2');
assert.strictEqual(c.opened.length, 3);
assert.strictEqual(category(true).click('2').prevented, false);

var message = {
    origin: 'http://localhost:8080',
    source: c.opened[2].popup,
    data: { type: 'menu-review-saved', menuId: '2' }
};
c.callbacks.message({ origin: 'https://attacker.example', source: message.source, data: message.data });
c.callbacks.message({ origin: message.origin, source: {}, data: message.data });
c.callbacks.message({ origin: message.origin, source: message.source, data: { type: 'menu-review-saved', menuId: '<script>' } });
assert.strictEqual(c.xhrs.length, 0);

c.callbacks.message(message);
assert.strictEqual(c.xhrs[0].url, 'http://localhost:8080/controller/menu/2/summary');
c.xhrs[0].status = 200;
c.xhrs[0].responseText = '{"avgRating":4.25}';
c.xhrs[0].onload();
assert.strictEqual(c.averages['2'].textContent, '4.25');

c.callbacks.message(message);
c.callbacks.message(message);
c.xhrs[2].status = 200;
c.xhrs[2].responseText = '{"avgRating":3.5}';
c.xhrs[2].onload();
c.xhrs[1].status = 200;
c.xhrs[1].responseText = '{"avgRating":5}';
c.xhrs[1].onload();
assert.strictEqual(c.averages['2'].textContent, '3.50');

var popupCode = fs.readFileSync('src/main/webapp/resources/js/menu-popup.js', 'utf8');
function popup(saved, hasParent) {
    var sent = [], callbacks = {};
    var closed = false;
    var opener = hasParent ? {
        closed: false,
        postMessage: function (data, origin) { sent.push({ data: data, origin: origin }); }
    } : null;
    var win = {
        opener: opener,
        location: { origin: 'http://localhost:8080' },
        close: function () { closed = true; }
    };
    var doc = {
        body: { getAttribute: function (key) { return key === 'data-review-saved' ? saved : '2'; } },
        getElementById: function () {
            return { addEventListener: function (name, fn) { callbacks.close = fn; } };
        },
        querySelectorAll: function () { return []; },
        querySelector: function () { return { href: '/controller/category' }; }
    };
    vm.runInNewContext(popupCode, { window: win, document: doc });
    return {
        sent: sent,
        callbacks: callbacks,
        win: win,
        closed: function () { return closed; }
    };
}

assert.strictEqual(popup('false', true).sent.length, 0);
var p = popup('true', true);
assert.strictEqual(p.sent.length, 1);
assert.strictEqual(p.sent[0].origin, 'http://localhost:8080');
assert.strictEqual(p.sent[0].data.menuId, '2');
p.callbacks.close();
assert(p.closed());

var direct = popup('true', false);
direct.callbacks.close();
assert.strictEqual(direct.win.location.href, '/controller/category');
console.log('PASS: popup dimensions, context path, reuse, separate windows, blocked fallback, origin/source/payload checks, stale response guard, committed notification, close/direct-page fallback');
