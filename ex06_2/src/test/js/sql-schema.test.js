// SQL structure checks; actual Oracle execution remains an integration check.
const assert = require('assert');
const fs = require('fs');
const path = require('path');
const dir = path.resolve(__dirname, '../../../sql');
const files = fs.readdirSync(dir, {withFileTypes: true})
    .filter(entry => entry.isDirectory())
    .flatMap(entry => fs.readdirSync(path.join(dir, entry.name))
        .filter(name => name.endsWith('.sql')).map(name => [name, path.join(dir, entry.name, name)]));
assert.strictEqual(new Set(files.map(([name]) => name)).size, files.length, 'Duplicate SQL filenames');
const paths = new Map(files);
const read = name => fs.readFileSync(paths.get(name), 'utf8');
const definitions = new Map();
for (const [file] of files) {
    for (const match of read(file).matchAll(/CREATE (?:UNIQUE )?(TABLE|SEQUENCE|INDEX) (\w+)/gi)) {
        const name = match[2].toUpperCase();
        assert(!definitions.has(name), 'Duplicate creation: ' + name + ' in ' + file);
        definitions.set(name, file);
    }
}
assert.strictEqual(definitions.get('TBL_MENU'), '01_create_menu.sql');
assert.strictEqual(definitions.get('TBL_MENU_REVIEW'), '05_create_menu_review.sql');
assert.strictEqual(definitions.get('TBL_DAILY_DIET'), '09_create_daily_diet.sql');
assert.strictEqual(definitions.get('TBL_BOOKMARK'), '10_create_bookmark.sql');
for (const file of ['09_create_daily_diet.sql','10_create_bookmark.sql']) {
    const sql = read(file).replace(/--[^\n]*/g, '');
    assert(sql.indexOf('RAISE_APPLICATION_ERROR') < sql.indexOf('CREATE TABLE'));
    assert(!/\b(DROP|ALTER|TRUNCATE|DELETE)\b/i.test(sql));
    assert(!/CREATE TABLE tbl_(menu|menu_review)\s*\(/i.test(sql));
    assert(sql.includes('REFERENCES tbl_menu(menu_id)'));
}
assert(read('09_create_daily_diet.sql').includes('actual_menu_id NUMBER(19)'));
assert(read('10_create_bookmark.sql').includes('UNIQUE (userid, menu_id)'));
const fixture = read('11_home_test_data.sql').replace(/--[^\n]*/g, '');
assert(!/\b(CREATE|DROP|TRUNCATE|DELETE)\b/i.test(fixture));
assert(!fixture.includes('tbl_menu_review'));
assert(fixture.includes('IF n <> 10 THEN'));
assert(fixture.includes('ROLLBACK;'));
console.log('PASS: no duplicate object creation; existing menu/review SQL reused; additive home schema and fixture guards');
