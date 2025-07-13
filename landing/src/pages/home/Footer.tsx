/** @jsxImportSource @emotion/react */
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {library} from "@fortawesome/fontawesome-svg-core";
import {faHeart, faEnvelope} from "@fortawesome/free-solid-svg-icons";
import {faGithub, faTelegram} from "@fortawesome/free-brands-svg-icons";
import Combined from "../../layout/Combined.tsx";

library.add(faHeart, faGithub, faEnvelope, faTelegram);


function Footer() {
    return <footer css={{
        padding: "24px 0",
        margin: "0 auto",
        width: "100%",
        maxWidth: "900px",
        background: "var(--background)",
        color: "var(--foreground-inaccent)",
        fontWeight: "bold",
        lineHeight: "30px",
    }}>
        <Combined rowHorizontalAlignment="space-between" rowVerticalAlignment="center" columnHorizontalAlignment="center">
            <span>(c) 2024 - lava_frai (Владимир Курдюков)</span>
            <span>With <FontAwesomeIcon icon={["fas", "heart"]}/> by. @lava_frai</span>
            <span css={{
                fontSize: "24px",
                display: "flex",
                alignItems: "center",
                "& > a": {
                    marginLeft: "8px",
                    textDecoration: "none",
                    color: "var(--foreground-inaccent)",
                    verticalAlign: "middle",
                },
            }}>
                <a href="https://github.com/lavaFrai/maiapp" target="_blank"><FontAwesomeIcon icon={["fab", "github"]}/></a>
                <a href="mailto:lavafrai@yandex.ru" target="_blank"><FontAwesomeIcon icon={["fas", "envelope"]}/></a>
                <a href="https://t.me/maiapp3" target="_blank"><FontAwesomeIcon icon={["fab", "telegram"]}/></a>
            </span>
        </Combined>
    </footer>
}


export default Footer;