import {useEffect, useRef, useState, type FormEvent} from 'react';
import {Link} from 'react-router-dom';
import Header from '../home/Header';
import Footer from '../home/Footer';
import '../home/HomePage.css';
import './ExportPage.css';
import {apiUrl, exportLinks, fetchJson, parseFormats, parseScheduleOptions, resolveSource,
    type ExportFormat, type ScheduleOption, type SourceKind} from './api';

const API_BASE = import.meta.env.VITE_MAIAPP_API_BASE || '/api/v1';
type PreparedExport = ReturnType<typeof exportLinks> & {
    label: string; format: ExportFormat; state: string | null;
};

export default function ExportPage() {
    const [kind, setKind] = useState<SourceKind>('group');
    const [input, setInput] = useState('');
    const [formats, setFormats] = useState<ExportFormat[]>([]);
    const [formatId, setFormatId] = useState('');
    const [options, setOptions] = useState<ScheduleOption[]>([]);
    const [loading, setLoading] = useState(true);
    const [catalogError, setCatalogError] = useState('');
    const [formatsError, setFormatsError] = useState('');
    const [error, setError] = useState('');
    const [retry, setRetry] = useState(0);
    const [preparing, setPreparing] = useState(false);
    const [prepared, setPrepared] = useState<PreparedExport | null>(null);
    const [copyStatus, setCopyStatus] = useState('');
    const request = useRef<AbortController | null>(null);
    const linkInput = useRef<HTMLInputElement>(null);
    const selected = formats.find(format => format.id === formatId);

    useEffect(() => {
        const controller = new AbortController();
        setFormatsError('');
        Promise.resolve().then(() => fetchJson(apiUrl(API_BASE, 'exports', location.origin), controller.signal))
            .then(parseFormats).then(result => {
                if (controller.signal.aborted) return;
                setFormats(result);
                setFormatId(previous => result.some(format => format.id === previous) ? previous : result[0].id);
            }).catch(cause => {
                if (!controller.signal.aborted) setFormatsError(cause instanceof Error ? cause.message : 'Не удалось загрузить форматы.');
            });
        return () => controller.abort();
    }, [retry]);

    useEffect(() => {
        const controller = new AbortController();
        setLoading(true);
        setCatalogError('');
        setOptions([]);
        Promise.resolve().then(() => fetchJson(apiUrl(API_BASE, kind === 'group' ? 'groups' : 'teachers', location.origin), controller.signal))
            .then(value => parseScheduleOptions(kind, value)).then(result => {
                if (!controller.signal.aborted) setOptions(result);
            }).catch(cause => {
                if (!controller.signal.aborted) setCatalogError(cause instanceof Error ? cause.message : 'Справочник недоступен.');
            }).finally(() => { if (!controller.signal.aborted) setLoading(false); });
        return () => controller.abort();
    }, [kind, retry]);

    useEffect(() => () => request.current?.abort(), []);

    function resetResult() {
        request.current?.abort();
        setPreparing(false);
        setPrepared(null);
        setError('');
        setCopyStatus('');
    }

    async function prepare(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        resetResult();
        if (!selected) return;
        const controller = new AbortController();
        request.current = controller;
        try {
            const source = resolveSource(kind, input, options);
            const links = exportLinks(API_BASE, location.origin, kind, source.id, selected);
            setPreparing(true);
            const response = await fetch(links.feed, {method: 'HEAD', signal: controller.signal, cache: 'no-cache'});
            if (!response.ok) throw new Error(response.status === 503
                ? 'Расписание пока не удалось подготовить. Попробуйте позднее: пустой календарь опубликован не будет.'
                : 'Не удалось подготовить экспорт. Проверьте выбранное расписание.');
            if (!response.headers.get('content-type')?.startsWith(selected.mime_type.split(';')[0])) {
                throw new Error('Сервер вернул не файл расписания. Повторите попытку позднее.');
            }
            if (!controller.signal.aborted) setPrepared({...links, label: source.label, format: selected,
                state: response.headers.get('x-maiapp-publication-state')});
        } catch (cause) {
            if (!controller.signal.aborted) setError(cause instanceof Error ? cause.message : 'Не удалось подготовить экспорт.');
        } finally {
            if (!controller.signal.aborted) setPreparing(false);
        }
    }

    async function copyLink() {
        if (!prepared) return;
        try {
            await navigator.clipboard.writeText(prepared.feed);
            setCopyStatus('Ссылка скопирована. Добавьте её как подписку, а не импорт файла.');
        } catch {
            linkInput.current?.focus();
            linkInput.current?.select();
            setCopyStatus('Ссылка выделена. Скопируйте её вручную.');
        }
    }

    return <>
        <Header/>
        <main className="export-page">
            <Link className="export-back" to="/">← На главную MAIapp</Link>
            <div className="export-intro">
                <span className="export-eyebrow">Без установки приложения</span>
                <h1>Расписание там,<br/>где удобно тебе.</h1>
                <p>Выбери расписание и формат. Подключи обновляемый календарь или сохрани файл.</p>
            </div>
            <div className="export-layout">
                <section className="export-panel" aria-labelledby="export-form-title">
                    <h2 id="export-form-title">Экспорт расписания</h2>
                    <form onSubmit={prepare}>
                        <fieldset className="export-kind">
                            <legend>Чьё расписание</legend>
                            {(['group', 'teacher'] as const).map(value => <label key={value}>
                                <input type="radio" name="source" value={value} checked={kind === value}
                                    onChange={() => { resetResult(); setKind(value); setInput(''); }}/>
                                {value === 'group' ? 'Группа' : 'Преподаватель'}
                            </label>)}
                        </fieldset>
                        <label className="export-field" htmlFor="schedule-search">
                            {kind === 'group' ? 'Название группы' : 'Преподаватель или UID'}
                            <input id="schedule-search" list="schedule-options" value={input} required autoComplete="off"
                                aria-describedby="schedule-help" placeholder={kind === 'group' ? 'М4О-306Б-23' : 'Начните вводить фамилию'}
                                onChange={event => { resetResult(); setInput(event.target.value); }}/>
                        </label>
                        <datalist id="schedule-options">
                            {options.map(option => <option key={option.id} value={option.label}>{option.id}</option>)}
                        </datalist>
                        <p id="schedule-help" className="export-hint">
                            {loading ? 'Загружаем справочник…' : kind === 'teacher'
                                ? 'Справочник преподавателей может быть неполным. Можно вставить UID расписания из API.'
                                : 'Выберите подсказку или введите полное название группы.'}
                        </p>
                        {catalogError && <div className="export-notice" role="status">
                            {catalogError} Расписание можно указать вручную.
                            <button type="button" className="export-text-button" onClick={() => setRetry(value => value + 1)}>Повторить загрузку</button>
                        </div>}
                        <label className="export-field" htmlFor="export-format">Формат
                            <select id="export-format" value={formatId} disabled={!formats.length}
                                onChange={event => { resetResult(); setFormatId(event.target.value); }}>
                                {!formats.length && <option value="">Загружаем форматы…</option>}
                                {formats.map(format => <option key={format.id} value={format.id}>{format.title}</option>)}
                            </select>
                        </label>
                        {selected && <p className="export-hint">{selected.description}</p>}
                        {formatsError && <div className="export-notice" role="alert">{formatsError}
                            <button type="button" className="export-text-button" onClick={() => setRetry(value => value + 1)}>Повторить загрузку</button>
                        </div>}
                        <button className="export-button export-primary" disabled={preparing || !selected || !input.trim()}>
                            {preparing ? 'Подготавливаем расписание…' : 'Получить расписание'}
                        </button>
                        {error && <p className="export-error" role="alert">{error}</p>}
                    </form>
                </section>
                <aside className="export-explainer">
                    <span className="export-eyebrow">Одна ссылка — актуальные пары</span>
                    <h2>Подписка, а не копия</h2>
                    <p>Добавьте ссылку один раз. Сторонний календарь будет сам проверять изменения расписания.</p>
                    <div className="export-note">
                        <strong>При сбое остаётся последняя корректная версия.</strong>
                        <p>Ошибка загрузки не превращает календарь в пустой. Обновление появится после восстановления источника и проверки данных.</p>
                    </div>
                    <p className="export-hint">Скорость обновления зависит от календарного приложения. Подписка не заменяет проверку срочных изменений в MAIapp.</p>
                </aside>
            </div>
            {prepared && <section className="export-panel export-result" aria-labelledby="export-result-title" aria-live="polite">
                <span className="export-eyebrow">Готово</span>
                <h2 id="export-result-title">{prepared.label}</h2>
                {(prepared.state === 'stale' || prepared.state === 'held') && <p className="export-notice">
                    Выдаётся последняя проверенная версия. Новые данные ещё проверяются или источник временно недоступен.
                </p>}
                {prepared.format.subscription && <>
                    <label className="export-field" htmlFor="subscription-url">Постоянная ссылка для подписки
                        <input ref={linkInput} id="subscription-url" type="url" readOnly value={prepared.feed}
                            onFocus={event => event.target.select()}/>
                    </label>
                    <div className="export-actions">
                        <button type="button" className="export-button export-primary" onClick={copyLink}>Скопировать ссылку</button>
                        {prepared.webcal && <a className="export-button" href={prepared.webcal}>Открыть в календаре</a>}
                    </div>
                    <p className="export-hint" role="status">{copyStatus || 'Подписка только для чтения. Изменения из стороннего календаря не отправляются в MAIapp.'}</p>
                    <details className="export-instructions"><summary>Как подключить подписку</summary>
                        <p><strong>Google Calendar:</strong> в браузере на компьютере откройте «Другие календари» → «+» → «Добавить по URL». Вставьте постоянную ссылку.</p>
                        <p><strong>Apple Calendar:</strong> используйте «Открыть в календаре» или добавьте новую подписку по URL.</p>
                        <p><strong>Outlook:</strong> «Добавить календарь» → «Подписаться из Интернета», затем вставьте ссылку.</p>
                    </details>
                </>}
                <div className="export-download">
                    <a className="export-button" href={prepared.download}>Скачать .{prepared.format.extension}</a>
                    <p className="export-hint">{prepared.format.subscription
                        ? 'Скачанный файл — разовая копия. Для автоматических обновлений используйте подписку по ссылке.'
                        : 'Файл содержит опубликованную версию расписания на момент скачивания.'}</p>
                </div>
            </section>}
            <p className="export-scope">Сейчас доступны общедоступные расписания групп и преподавателей МАИ.
                Личные события и пометки из приложения не публикуются.</p>
        </main>
        <Footer/>
    </>;
}
