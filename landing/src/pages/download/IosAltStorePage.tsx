/** @jsxImportSource @emotion/react */
import Viewer from "react-viewer";
import {useState} from "react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

const imgSrc = (name: string) => `/media/ios-install/${name}`;

const images = [
    "altstore-download.png",
    "altstore-classic.png",
    "altserver-tray.png",
    "altserver-menu.png",
    "trust-error.png",
    "trust-settings.png",
    "altstore-login.png",
    "install-1.png",
    "install-2.png",
    "install-3.png",
    "install-4.png",
].map(name => ({src: imgSrc(name), name}));

const imageLabels: Record<string, string> = {
    "altstore-download.png": "Кнопка Get AltStore на сайте AltStore",
    "altstore-classic.png": "Кнопка AltServer Windows в разделе AltStore Classic",
    "altserver-tray.png": "Запуск AltServer от имени администратора в Windows",
    "altserver-menu.png": "Меню AltServer с пунктом Install AltStore",
    "trust-error.png": "Настройки iOS для доверия профилю разработчика",
    "trust-settings.png": "Настройки iOS для включения Developer Mode",
    "altstore-login.png": "Вход в Apple ID в настройках AltStore",
    "install-1.png": "IPA-файл Mai App в Telegram",
    "install-2.png": "Кнопка Поделиться на экране IPA-файла",
    "install-3.png": "Кнопка Еще в меню Поделиться iOS",
    "install-4.png": "Выбор AltStore в списке приложений",
};

const requirements = [
    "iPhone для установки Mai App",
    "ПК с Windows",
    "Кабель для подключения iPhone",
    "iTunes и iCloud на ПК",
    "Apple ID для входа в AltStore",
    "Иностранный iCloud, например из ЕС",
];

const steps = [
    {
        title: "Скачайте AltStore Classic",
        body: <>
            Откройте <a href="https://altstore.io/" target="_blank" rel="noreferrer">altstore.io</a>,
            нажмите <b>Get AltStore</b> и выберите <b>AltStore Classic</b>.
        </>,
        images: ["altstore-download.png", "altstore-classic.png"],
    },
    {
        title: "Установите AltServer на ПК",
        body: <>
            Скачайте архив, распакуйте его и установите AltServer. После установки найдите AltServer
            в Windows и запустите его от имени администратора.
        </>,
        images: ["altserver-tray.png"],
    },
    {
        title: "Поставьте AltStore на iPhone",
        body: <>
            Подключите iPhone к ПК кабелем. Затем откройте системный трей, нажмите на иконку AltServer,
            выберите <b>Install AltStore</b> и свой iPhone.
        </>,
        images: ["altserver-menu.png"],
    },
    {
        title: "Разрешите доверие профилю",
        body: <>
            Если AltStore не открывается из-за недоверенного разработчика, откройте настройки iOS:
            <b> General</b> {"->"} <b>Profiles & Device Management</b> и нажмите <b>Trust</b>.
        </>,
        images: ["trust-error.png"],
    },
    {
        title: "Включите режим разработчика",
        body: <>
            Если iOS попросит включить Developer Mode, откройте <b>Privacy & Security</b> {"->"} <b>Developer Mode</b>
            и включите переключатель.
        </>,
        images: ["trust-settings.png"],
    },
    {
        title: "Войдите в AltStore",
        body: <>Откройте AltStore на iPhone, перейдите в настройки и войдите в Apple ID.</>,
        images: ["altstore-login.png"],
    },
    {
        title: "Установите Mai App",
        body: <>
            Откройте файл <b>maiapp-sideload.ipa</b> на iPhone, нажмите <b>Поделиться</b>,
            выберите <b>Еще</b>, затем <b>AltStore</b>.
        </>,
        images: ["install-1.png", "install-2.png", "install-3.png", "install-4.png"],
    },
];

function InstallImage({name, onClick}: { name: string, onClick: () => void }) {
    return (
        <button className="ios-install-image" type="button" onClick={onClick} aria-label={imageLabels[name]}>
            <img src={imgSrc(name)} alt={imageLabels[name]}/>
        </button>
    );
}

export default function IosAltStorePage({onBack}: { onBack: () => void }) {
    const [viewerIndex, setViewerIndex] = useState(0);
    const [viewerVisible, setViewerVisible] = useState(false);

    const open = (name: string) => {
        const index = images.findIndex(image => image.name === name);
        setViewerIndex(Math.max(index, 0));
        setViewerVisible(true);
    };

    return (
        <div className="ios-install-page">
            <Viewer
                activeIndex={viewerIndex}
                visible={viewerVisible}
                images={images}
                onClose={() => setViewerVisible(false)}
                onMaskClick={() => setViewerVisible(false)}
            />

            <button className="ios-install-back" onClick={onBack}>
                <FontAwesomeIcon icon={["fas", "arrow-left"]}/>
                <span>Назад</span>
            </button>

            <section className="ios-install-hero">
                <div>
                    <p className="ios-install-kicker">iOS sideload</p>
                    <h2>Установка через AltStore на Windows</h2>
                    <p>
                        Способ для ручной установки Mai App, пока приложение недоступно в App Store.
                        Инструкция рассчитана на подключение по кабелю.
                    </p>
                </div>
                <div className="ios-install-note">
                    <b>Важно</b>
                    <span>Раз в 7 дней нужно подключать iPhone к ПК и продлевать подпись в AltStore.</span>
                </div>
            </section>

            <section className="ios-install-warning">
                <div>
                    <b>Делайте установку по кабелю</b>
                    <p>Если AltServer и iPhone не находят друг друга по Wi-Fi, это нормально. Кабель надежнее и понятнее.</p>
                </div>
            </section>

            <section className="ios-install-section">
                <h3>Что понадобится</h3>
                <div className="ios-install-requirements">
                    {requirements.map(requirement => (
                        <span key={requirement}>{requirement}</span>
                    ))}
                </div>
            </section>

            <section className="ios-install-section">
                <h3>Порядок установки</h3>
                <div className="ios-install-steps">
                    {steps.map((step, index) => (
                        <article className="ios-install-step" key={step.title}>
                            <div className="ios-install-step-number">{index + 1}</div>
                            <div className="ios-install-step-content">
                                <h4>{step.title}</h4>
                                <p>{step.body}</p>
                                {step.images.length > 0 && (
                                    <div className="ios-install-images">
                                        {step.images.map(name => (
                                            <InstallImage key={name} name={name} onClick={() => open(name)}/>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </article>
                    ))}
                </div>
            </section>

            <section className="ios-install-footer-note">
                <p>
                    Запустите приложение после установки. По вопросам можно написать автору инструкции:
                    {" "}
                    <a href="https://t.me/Qirieshka_clwn" target="_blank" rel="noreferrer">@Qirieshka_clwn</a>.
                </p>
            </section>
        </div>
    );
}
