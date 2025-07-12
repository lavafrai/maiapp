/** @jsxImportSource @emotion/react */
import Row from "../../layout/Row.tsx";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {type IconName, library} from "@fortawesome/fontawesome-svg-core";
import {faEnvelope, faHeart} from "@fortawesome/free-regular-svg-icons";

library.add(faEnvelope, faHeart);


function Header() {
    return <header css={{
        margin: "auto",
        maxWidth: "900px",
        padding: "8px 12px",
        boxSizing: "content-box",
    }}>
        <Row horizontalAlignment="space-between" verticalAlignment="center">
            <a css={{
                fontSize: "1.4em",
                fontWeight: 600,
                lineHeight: "48px",
                color: "var(--primary)",
                userSelect: "none",
                margin: 0,
            }}>lava_frai</a>
            <Row>
                <HeaderMenuItem text="Связаться" icon="envelope"/>
                <HeaderMenuItem text="Пожертвования" icon="heart"/>
            </Row>
        </Row>
    </header>
}

const HeaderMenuItem = ({text, icon}: { text: string, icon: IconName }) => {
    return <a css={{
        padding: "10px 12px",
        margin: "6px",
        borderRadius: "6px",
        cursor: "pointer",
        userSelect: "none",
        whiteSpace: "nowrap",
        backgroundColor: "var(--background)",
        transition: "background-color 0.3s ease",
        textDecoration: "none",

        "&:hover": {
            backgroundColor: "#fff1"
        },

        "& > span": {
            marginLeft: "8px",
            fontSize: "16px"
        },
    }}>
        <FontAwesomeIcon icon={["far", icon]} fontWeight={800}/>
        <span>{text}</span>
    </a>
}

export default Header;
