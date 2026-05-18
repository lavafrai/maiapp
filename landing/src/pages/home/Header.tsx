/** @jsxImportSource @emotion/react */
import Row from "../../layout/Row.tsx";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {type IconName, library} from "@fortawesome/fontawesome-svg-core";
import {faEnvelope, faHeart} from "@fortawesome/free-regular-svg-icons";
import useWindowDimensions from "../../utils/Dimensions.tsx";

library.add(faEnvelope, faHeart);


function Header() {
    return <header css={{
        margin: "0 auto",
        width: "100%",
        maxWidth: "900px",
        padding: "8px 12px",
        boxSizing: "border-box",
    }}>
        <Row horizontalAlignment="space-between" verticalAlignment="center">
            <a css={{
                fontSize: "1.4em",
                fontWeight: 600,
                lineHeight: "48px",
                color: "var(--primary)",
                userSelect: "none",
                margin: 0,
                textDecoration: "none",
            }} href="https://lavafrai.ru" target="_blank">lava_frai</a>
            <Row>
                <HeaderMenuItem text="Связаться" icon="envelope" href="https://lavafrai.ru"/>
                <HeaderMenuItem text="Пожертвования" icon="heart" href="https://pay.cloudtips.ru/p/e930707c"/>
            </Row>
        </Row>
    </header>
}

const HeaderMenuItem = ({text, icon, href}: { text: string, icon: IconName, href: string }) => {
    let dimensions = useWindowDimensions();
    let isTextVisible = dimensions.width > 900;
    let textDisplay = isTextVisible ? "inline" : "none";

    return <a css={{
        padding: "10px 12px",
        margin: "6px",
        borderRadius: "6px",
        cursor: "pointer",
        userSelect: "none",
        whiteSpace: "nowrap",
        backgroundColor: "var(--background)",
        color: "var(--foreground)",
        transition: "background-color 0.3s ease",
        textDecoration: "none",

        "&:hover": {
            backgroundColor: "#fff1"
        },

        "& > span": {
            marginLeft: "8px",
            fontSize: "16px"
        },
    }} href={href}>
        <FontAwesomeIcon icon={["far", icon]} fontWeight={800}/>
        <span css={{display: textDisplay}}>{text}</span>
    </a>
}

export default Header;
