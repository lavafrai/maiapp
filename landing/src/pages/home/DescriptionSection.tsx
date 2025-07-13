/** @jsxImportSource @emotion/react */
import Row from "../../layout/Row.tsx";
import logo from "../../assets/maiapp_round.webp";
import PrimaryButton from "../../PrimaryButton.tsx";
import {faDownload} from "@fortawesome/free-solid-svg-icons";
import {library} from "@fortawesome/fontawesome-svg-core";
library.add(faDownload);


export default function DescriptionSection() {
    return <section>
        <Row>
            <img src={logo} alt="maiapp logo" width="96px" height="96px"/>
            <div css={{ marginLeft: "16px", flex: 1 }}>
                <h1 css={{
                    margin: "16px 0 4px",
                    fontWeight: "300",
                    fontSize: "1.9em",
                    lineHeight: "1.5em",
                }}>
                    MAI app
                </h1>
                <p>
                    Приложение с информацией и расписанием для студентов и преподавателей Московского Авиационного Института.
                </p>
            </div>
        </Row>
        <div style={{height: "8px"}}></div>
        <PrimaryButton text="Скачать" icon={["fas", "download"]}/>
    </section>
}
