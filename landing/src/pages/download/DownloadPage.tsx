/** @jsxImportSource @emotion/react */
import {library} from "@fortawesome/fontawesome-svg-core";
import {faAndroid, faApple, faAppStore, faAppStoreIos, faTelegram} from "@fortawesome/free-brands-svg-icons";
import {useEffect, useState} from "react";
import getRepoLatestRelease from "./github.ts";
import {ShimmerTitle} from "react-shimmer-effects";
import ruStore from "../../assets/ru_store.png";
import "./DownloadPage.css"
import {Else, If, Then} from "react-if";
import {Tab, TabList, TabPanel, Tabs} from "react-tabs";
import 'react-tabs/style/react-tabs.css';
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import PrimaryButton from "../../PrimaryButton.tsx";
import {faArrowLeft, faDesktop} from "@fortawesome/free-solid-svg-icons";
import Footer from "../home/Footer.tsx";

library.add(faAndroid, faApple, faTelegram, faArrowLeft, faAppStore, faAppStoreIos, faDesktop)


function DownloadPage() {
    let [latestVersionInfo, setLatestVersionInfo] = useState<{
        version: string,
        url: string,
        description: string
    } | null>(null);
    useEffect(() => {
        const fetchData = async () => {
            const repoLatestRelease = await getRepoLatestRelease();
            setLatestVersionInfo(repoLatestRelease)
        }
        fetchData().catch(console.error);
    }, []);

    return <div css={{
        minHeight: "100vh",
        display: "flex",
        flexDirection: "column",
        justifyContent: "space-between",
    }}>
        <div css={{
            display: 'flex',
            flexDirection: 'column',
            width: '100vw',
            maxWidth: "900px",
            margin: '0 auto',

        }}>
            <header css={{padding: "8px 12px"}}>
                <h1 style={{fontSize: "1.4em"}}>
                    <a href="/" css={{color: 'var(--on-background)'}}><FontAwesomeIcon icon={['fas', 'arrow-left']}
                                                                                       size='1x'/></a> Загрузить
                    приложение МАИ <a href="https://lavafrai.ru/">by lava_frai</a>
                </h1>
            </header>
            <section style={{
                margin: "0 12px",
                padding: "8px 12px",
                borderRadius: "8px",
                backgroundColor: "var(--card)",
            }}>

                <If condition={latestVersionInfo == null}>
                    <Then>
                        <div css={{
                            opacity: "0.2",
                        }}>
                            <ShimmerTitle line={8} gap={10} variant="secondary"/>
                        </div>
                    </Then>
                    <Else>
                    <span css={{
                        display: "inline-block",
                        fontSize: "1.1em",
                        fontWeight: "bold",
                        marginTop: "8px",
                        marginBottom: "12px",
                        color: "var(--text)",
                    }}>Новейшая версия ({latestVersionInfo?.version}):</span>
                        <p>{latestVersionInfo?.description}</p>
                    </Else>
                </If>
            </section>
            <section>
                <Tabs id="download-tabs">
                    <TabList>
                        <Tab><FontAwesomeIcon icon={['fab', 'android']}/> Android</Tab>
                        <Tab><FontAwesomeIcon icon={['fab', 'apple']}/> iOS</Tab>
                        <Tab><FontAwesomeIcon icon={['fas', 'desktop']}/> Прочее</Tab>
                    </TabList>

                    <TabPanel>
                        <DownloadPanelAndroid/>
                    </TabPanel>
                    <TabPanel>
                        <DownloadPanelIos/>
                    </TabPanel>
                    <TabPanel>
                        <DownloadPanelOther/>
                    </TabPanel>
                </Tabs>
            </section>
        </div>
        <Footer></Footer>
    </div>
}

export default DownloadPage;


function DownloadPanelAndroid() {
    return <div style={{
        display: "flex",
        gap: "8px",
        flexDirection: "column"
    }}>
        <p>Системные требования: Android 6 и выше.</p>
        <p>Загрузите установочный apk пакет из telegram, откройте его и следуйте инструкциям на экране:</p>
        <span>
            <PrimaryButton text="Загрузить из телеграм" icon={['fab', 'telegram']} target="_blank"
                           href="https://t.me/maiapp3"/>
            Скачивайте новейшую версию.
        </span>
        <p>Или, если вы хотите получать автоматические обновления, можете воспользоваться RuStore, скачав приложение из
            него.</p>
        <span>
            <PrimaryButton text="Доступно в RuStore" image={ruStore} target="_blank"
                           href="https://www.rustore.ru/catalog/app/ru.lavafrai.maiapp"/>
        </span>
    </div>
}

function DownloadPanelIos() {
    return <div style={{
        display: "flex",
        gap: "8px",
        flexDirection: "column"
    }}>
        <p>Требуется iOS 15.6 и выше.</p>
        <p>Загрузите приложение из App Store:</p>
        <span>
            <PrimaryButton text="Скачать из App Store" icon={['fab', 'app-store-ios']} target="_blank"
                           href="https://apps.apple.com/us/app/%D1%80%D0%B0%D1%81%D0%BF%D0%B8%D1%81%D0%B0%D0%BD%D0%B8%D0%B5-%D0%BC%D0%B0%D0%B8/id6739470086"/>
        </span>
        <p>Сами понимаете, тут вариантов, как установить, немного, поэтому спасибо профбюро 4-го факультета МАИ за то,
            что оплатили публикацию в AppStore.</p>
        <p>Будет славно, если совершите небольшое пожертвование, чтобы помочь с оплатой подписки Apple Developer.</p>
    </div>
}

function DownloadPanelOther() {
    return <div style={{
        display: "flex",
        gap: "8px",
        flexDirection: "column"
    }}>
        <p>Если опубликованные версии по каким-либо причинам не подошли вашим устройствам, всегда можно собрать проект
            самостоятельно, с соответствующими настройками.</p>
        <p>Исходный код полностью доступен на <a href="https://github.com/lavafrai/maiapp">GitHub</a>.</p>
        <p>Кроме того, приложение можно собрать для использования на десктопных платформах: windows, macos и linux. Эти
            версии не публикуются в связи с тем, что дизайн приложения плохо оптимизирован для работы на несенсорных
            экранах.</p>
    </div>
}