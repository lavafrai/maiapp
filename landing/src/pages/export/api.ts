export type SourceKind = 'group' | 'teacher';
export interface ScheduleOption { id: string; label: string }
export interface ExportFormat {
    id: string;
    title: string;
    description: string;
    extension: string;
    mime_type: string;
    subscription: boolean;
}

export function normalizeSearch(value: string): string {
    return value.normalize('NFKC').trim().toLocaleLowerCase('ru').replace(/[‐‑‒–—−]/g, '-');
}

export function resolveSource(kind: SourceKind, input: string, options: ScheduleOption[]): ScheduleOption {
    const matches = options.filter(option => normalizeSearch(option.label) === normalizeSearch(input));
    if (matches.length === 1) return matches[0];
    if (matches.length > 1) throw new Error('Найдено несколько преподавателей с этим именем. Введите UID нужного расписания.');
    const id = input.trim().replace(/[‐‑‒–—−]/g, '-');
    if (kind === 'teacher' && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(id)) {
        return {id: id.toLowerCase(), label: id};
    }
    if (kind === 'group' && /^(([МТ])([\dИУ]+?)([ОВЗ]))-((\d+?)(Б|С|А|СВ|БВ|М)к?и?)-(\d+?)$/.test(id)) {
        return {id, label: id};
    }
    throw new Error(kind === 'group'
        ? 'Выберите группу из списка или введите её полное название, например М4О-306Б-23.'
        : 'Выберите преподавателя из списка или введите UID его расписания.');
}

function object(value: unknown): value is Record<string, unknown> {
    return typeof value === 'object' && value !== null;
}

export function parseFormats(value: unknown): ExportFormat[] {
    if (!Array.isArray(value)) throw new Error('Сервер вернул некорректный список форматов.');
    const formats: ExportFormat[] = [];
    const ids = new Set<string>();
    for (const item of value) {
        if (!object(item) || typeof item.id !== 'string' || !/^[a-z0-9-]+$/.test(item.id)
            || typeof item.title !== 'string' || typeof item.description !== 'string'
            || typeof item.extension !== 'string' || !/^[a-z0-9]+$/.test(item.extension)
            || typeof item.mime_type !== 'string' || typeof item.subscription !== 'boolean'
            || ids.has(item.id)) throw new Error('Сервер вернул некорректное описание формата.');
        ids.add(item.id);
        formats.push({id: item.id, title: item.title, description: item.description,
            extension: item.extension, mime_type: item.mime_type, subscription: item.subscription});
    }
    if (!formats.length) throw new Error('Пока нет доступных форматов экспорта.');
    return formats;
}

export function parseScheduleOptions(kind: SourceKind, value: unknown): ScheduleOption[] {
    if (!Array.isArray(value)) throw new Error('Не удалось прочитать справочник расписаний.');
    const options: ScheduleOption[] = [];
    for (const item of value) {
        if (!object(item)) continue;
        if (kind === 'group' && typeof item.name === 'string') {
            options.push({id: item.name, label: item.name});
        } else if (kind === 'teacher' && object(item.name) && object(item.uid)
            && typeof item.name.name === 'string' && typeof item.uid.uid === 'string') {
            options.push({id: item.uid.uid, label: item.name.name});
        }
    }
    return options.sort((a, b) => a.label.localeCompare(b.label, 'ru'));
}

export function apiUrl(base: string, path: string, origin: string): string {
    const url = new URL(base, origin);
    if (!['http:', 'https:'].includes(url.protocol) || url.username || url.password
        || url.search || url.hash) throw new Error('Некорректный адрес API.');
    url.pathname = url.pathname.replace(/\/$/, '') + '/' + path.replace(/^\//, '');
    return url.href;
}

export function exportLinks(base: string, origin: string, kind: SourceKind, id: string, format: ExportFormat) {
    if (!/^[a-z0-9-]+$/.test(format.id)) throw new Error('Некорректный формат.');
    const feed = apiUrl(base, `exports/${kind}/${encodeURIComponent(id)}/${format.id}`, origin);
    const download = new URL(feed);
    download.searchParams.set('download', 'true');
    return {feed, download: download.href,
        webcal: format.subscription ? feed.replace(/^https?:/, 'webcal:') : null};
}

export async function fetchJson(url: string, signal: AbortSignal): Promise<unknown> {
    const response = await fetch(url, {signal});
    if (!response.ok) throw new Error('Сервер временно недоступен. Попробуйте ещё раз.');
    if (!response.headers.get('content-type')?.includes('application/json')) {
        throw new Error('Вместо данных сервер вернул веб-страницу. Попробуйте обновить страницу позднее.');
    }
    return response.json();
}
