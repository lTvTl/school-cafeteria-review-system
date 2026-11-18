const assert = require('assert');
const fs = require('fs');
const path = require('path');
const vm = require('vm');
const nodes = new Map();
function $(id) {
    if (!nodes.has(id)) nodes.set(id, {
        text(value) { this.value = value; return this; },
        attr(name, value) { this[name] = value; return this; },
        prop(name, value) { this[name] = value; return this; },
        on(name, fn) { this[name] = fn; return this; }
    });
    return nodes.get(id);
}
const requests = [];
$.ajax = options => {
    assert.strictEqual(options.url, '/controller/admin/status');
    assert.strictEqual(options.dataType, 'json');
    assert.strictEqual(options.timeout, 5000);
    const req = {done(fn) { this.success = fn; return this; }, fail(fn) { this.failure = fn; return this; }, always(fn) { this.finish = fn; return this; }};
    requests.push(req);
    return req;
};
let scheduled;
let now = 100000;
class Clock extends Date { static now() { return now; } }
vm.runInNewContext(fs.readFileSync(path.resolve(__dirname, '../../main/webapp/resources/js/admin-status.js'), 'utf8'), {
    jQuery: $, document: {getElementById: () => ({getAttribute: name => ({'data-status-url':'/controller/admin/status', 'data-memory-usage':'20', 'data-active-users':'2'}[name])})},
    Date: Clock,
    setTimeout(fn, delay) { assert.strictEqual(delay, 2000); scheduled = fn; return 1; },
    clearTimeout() { scheduled = null; }
});
assert.strictEqual(requests.length, 0);
now += 2000; scheduled();
$('#admin-refresh').click();
assert.strictEqual(requests.length, 1, 'No overlapping polling/manual requests');
assert.strictEqual($('#admin-refresh').disabled, true);
requests[0].success({serverStatus: 'NORMAL', activeUserCnt: 3, memoryUsage: 25.25});
requests[0].finish();
assert.strictEqual($('#admin-active-users').value, 3);
assert.strictEqual($('#admin-memory-usage').value, '25.3%');
assert.strictEqual($('#admin-server-status').value, '정상');
assert.strictEqual($('#admin-refresh').disabled, false);
now += 2000; scheduled();
requests[1].failure(); requests[1].finish();
assert.strictEqual($('#admin-server-status').value, '확인 불가');
assert.strictEqual($('#admin-active-users').value, '—', 'Do not show stale counts after connection/auth failure');
now += 2000; scheduled();
requests[2].success({serverStatus:'BUSY', activeUserCnt:4, memoryUsage:95}); requests[2].finish();
assert.strictEqual($('#admin-server-status').value, '혼잡');
now += 2000; scheduled();
requests[3].success('<html>login</html>'); requests[3].finish();
assert.strictEqual($('#admin-server-status').value, '확인 불가');
assert.strictEqual(typeof scheduled, 'function', 'Retry even after failure');
console.log('PASS: status polling, context path, no overlapping requests, connection/auth failure, recovery');

assert($('#admin-memory-line').d.includes(' M'));
assert($('#admin-memory-line').d.split(' M').length > 2, 'Missing samples must break the line');
now += 2000; scheduled();
requests[4].success({serverStatus:'NORMAL', activeUserCnt:12, memoryUsage:0}); requests[4].finish();
assert.strictEqual($('#admin-users-max').value, '15 세션');
assert($('#admin-memory-line').d.includes('600.00 160.00'), 'Zero usage at chart baseline');
now += 61000; scheduled();
requests[5].success({serverStatus:'BUSY', activeUserCnt:0, memoryUsage:100}); requests[5].finish();
assert.strictEqual($('#admin-users-max').value, '5 세션');
assert.strictEqual($('#admin-memory-line').d.trim(), 'M600.00 0.00', 'Expired samples removed; full usage at top');
assert(!/NaN|Infinity/.test($('#admin-users-line').d));
console.log('PASS: graph scaling, error gaps, 60-second expiry, zero/full usage');

function sample(count, elapsed) {
    now += elapsed;
    scheduled();
    const request = requests[requests.length - 1];
    request.success({serverStatus: 'NORMAL', activeUserCnt: count, memoryUsage: 25});
    request.finish();
    assert.strictEqual($('#admin-active-users').value, count);
}
for (const [count, maximum] of [[5, 5], [6, 10], [10, 10], [11, 15], [15, 15], [100, 100]]) {
    sample(count, 61000);
    assert.strictEqual($('#admin-users-max').value, maximum + ' 세션');
    const expectedY = (160 * (1 - count / maximum)).toFixed(2);
    assert.strictEqual($('#admin-users-line').d.trim(), 'M600.00 ' + expectedY);
}
sample(2, 2000);
assert.strictEqual($('#admin-users-max').value, '100 세션', 'Keep the peak visible during the 60-second window');
sample(2, 61000);
assert.strictEqual($('#admin-users-max').value, '5 세션', 'Shrink axis when the peak expires');
assert(!/NaN|Infinity/.test($('#admin-users-line').d));
console.log('PASS: 5/6/10/11/15/100 sessions, unclipped graph coordinates, peak retention and axis shrink');
