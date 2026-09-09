import {Link} from 'react-router-dom';
import './ExportPage.css';

export default function ExportEntry() {
    return <section className="export-entry" aria-label="Расписание без приложения">
        <div><strong>Расписание в твоём календаре</strong>
            <p>Обновляемая подписка и экспорт — без установки MAIapp.</p></div>
        <Link className="export-button export-primary" to="/export">Получить расписание →</Link>
    </section>;
}
