import test from 'node:test';
import assert from 'node:assert/strict';
import {apiUrl, exportLinks, fetchJson, normalizeSearch, parseFormats, parseScheduleOptions, resolveSource} from '../src/pages/export/api.ts';

const ics = {id: 'ics', title: 'Календарь', description: 'Подписка', extension: 'ics', mime_type: 'text/calendar; charset=utf-8', subscription: true};
const uid = 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee';

test('normalizes whitespace, Unicode dashes and case in catalog searches', () => {
    assert.equal(normalizeSearch('  М4О‑306Б–23  '), 'м4о-306б-23');
    assert.equal(resolveSource('group', '  м4о‑306б–23 ', [{id: 'М4О-306Б-23', label: 'М4О-306Б-23'}]).id, 'М4О-306Б-23');
});

test('accepts manual group identifiers when the catalog is unavailable', () => {
    assert.equal(resolveSource('group', 'М4О‑306Б‑23', []).id, 'М4О-306Б-23');
    assert.throws(() => resolveSource('group', '../other', []));
    assert.throws(() => resolveSource('group', 'not a group', []));
});

test('teacher subscriptions use UID, never an unstable name lookup', () => {
    const options = [{id: uid, label: 'Иванов Иван Иванович'}];
    assert.equal(resolveSource('teacher', 'Иванов Иван Иванович', options).id, uid);
    assert.equal(resolveSource('teacher', uid.toUpperCase(), []).id, uid);
    assert.throws(() => resolveSource('teacher', 'Иванов Иван Иванович', []));
});

test('ambiguous teacher names require an explicit UID', () => {
    const options = [{id: uid, label: 'Тестовый Преподаватель'}, {id: '11111111-2222-3333-4444-555555555555', label: 'Тестовый Преподаватель'}];
    assert.throws(() => resolveSource('teacher', 'Тестовый Преподаватель', options), /несколько/);
});

test('discovery supports future download-only formats without an ICS switch', () => {
    const xlsx = {...ics, id: 'university-xlsx', title: 'Таблица', extension: 'xlsx', mime_type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', subscription: false};
    assert.deepEqual(parseFormats([ics, xlsx]), [ics, xlsx]);
    const links = exportLinks('/api/v1', 'https://maiapp.example', 'group', 'М4О-306Б-23', xlsx);
    assert.equal(links.webcal, null);
    assert.match(links.download, /\/university-xlsx\?download=true$/);
});

test('rejects empty, malformed, duplicated and unsafe format metadata', () => {
    for (const value of [null, {}, [], [ics, ics], [{...ics, id: '../ics'}], [{...ics, extension: 'ics"'}], [{...ics, subscription: 'true'}]]) {
        assert.throws(() => parseFormats(value));
    }
});

test('decodes actual group and teacher response shapes, ignoring invalid entries', () => {
    assert.deepEqual(parseScheduleOptions('group', [{name: 'М4О-306Б-23', fac: '4'}, null, 2]), [{id: 'М4О-306Б-23', label: 'М4О-306Б-23'}]);
    assert.deepEqual(parseScheduleOptions('teacher', [{name: {name: 'Преподаватель'}, uid: {uid}}, {}, null]), [{id: uid, label: 'Преподаватель'}]);
    assert.throws(() => parseScheduleOptions('group', {data: []}));
});

test('feed URLs encode Cyrillic exactly once and preserve the API prefix', () => {
    const links = exportLinks('/api/v1/', 'https://maiapp.lavafrai.ru', 'group', 'М4О-306Б-23', ics);
    assert.equal(decodeURIComponent(new URL(links.feed).pathname), '/api/v1/exports/group/М4О-306Б-23/ics');
    assert.ok(!links.feed.includes('%25D0'));
    assert.equal(new URL(links.download).searchParams.get('download'), 'true');
    assert.equal(links.webcal, links.feed.replace('https:', 'webcal:'));
});

test('explicit development API origins work without hard-coded production URLs', () => {
    assert.equal(apiUrl('http://localhost:8080/', 'exports', 'http://localhost:5173'), 'http://localhost:8080/exports');
    assert.equal(apiUrl('/api/v1', 'groups', 'https://maiapp.example'), 'https://maiapp.example/api/v1/groups');
});

test('invalid API bases are rejected before network requests', () => {
    for (const base of ['javascript:alert(1)', 'https://user:pass@example.com/', 'https://example.com/?key=x', 'https://example.com/#x']) {
        assert.throws(() => apiUrl(base, 'exports', 'https://maiapp.example'));
    }
});

test('HTTP errors are not interpreted as empty catalogs', async t => {
    t.mock.method(globalThis, 'fetch', async () => new Response('{}', {status: 503, headers: {'content-type': 'application/json'}}));
    await assert.rejects(fetchJson('https://example.test', new AbortController().signal), /недоступен/);
});

test('HTML challenges are reported explicitly instead of parsed as data', async t => {
    t.mock.method(globalThis, 'fetch', async () => new Response('<html>challenge</html>', {headers: {'content-type': 'text/html'}}));
    await assert.rejects(fetchJson('https://example.test', new AbortController().signal), /веб-страницу/);
});

test('JSON loads pass the caller abort signal through to fetch', async t => {
    const controller = new AbortController();
    t.mock.method(globalThis, 'fetch', async (_url, options) => {
        assert.equal(options.signal, controller.signal);
        return new Response(JSON.stringify([ics]), {headers: {'content-type': 'application/json; charset=utf-8'}});
    });
    assert.deepEqual(await fetchJson('https://example.test', controller.signal), [ics]);
});

test('network failures remain failures rather than successful empty data', async t => {
    t.mock.method(globalThis, 'fetch', async () => { throw new TypeError('offline'); });
    await assert.rejects(fetchJson('https://example.test', new AbortController().signal), /offline/);
});
