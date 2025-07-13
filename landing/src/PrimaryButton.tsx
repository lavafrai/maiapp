/** @jsxImportSource @emotion/react */
import type {IconProp} from "@fortawesome/fontawesome-svg-core";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";


export default function PrimaryButton({
    text,
    icon,
    onClick,
}: {
    text: string,
    icon: IconProp,
    onClick?: () => void;
}) {
    return (
        <a css={{
            padding: "10px 12px",
            margin: "6px",
            borderRadius: "6px",
            cursor: "pointer",
            userSelect: "none",
            whiteSpace: "nowrap",
            color: "var(--background)",
            backgroundColor: "var(--primary)",
            display: "inline-flex",

            "& > span": {
                marginLeft: "4px",
            },
        }} onClick={onClick}>
            <FontAwesomeIcon icon={icon}/>
            <span>{text}</span>
        </a>
    );
}