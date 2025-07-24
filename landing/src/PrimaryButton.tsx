/** @jsxImportSource @emotion/react */
import type {IconProp} from "@fortawesome/fontawesome-svg-core";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {If, Then} from "react-if";


export default function PrimaryButton({
    text,
    icon,
    image,
    onClick,
    href,
    target,
}: {
    text: string,
    icon?: IconProp,
    image?: string,
    onClick?: () => void,
    href?: string,
    target?: string,
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
            alignItems: "center",

            "& > span": {
                marginLeft: "4px",
            },
        }} onClick={onClick} href={href} target={target}>
            <If condition={icon != undefined}>
                <Then>
                    <FontAwesomeIcon icon={icon!} size="1x"/>
                </Then>
            </If>
            <If condition={image != undefined}>
                <Then>
                    <div css={{
                        width: "1.2em",
                        height: "1.2em",
                        backgroundBlendMode: "luminosity",
                        maskImage: `url(${image})`,
                        background: "var(--background)",
                        maskSize: "contain",
                        maskPosition: "center",
                        maskRepeat: "no-repeat",
                    }}/>
                </Then>
            </If>
            <span>{text}</span>
        </a>
    );
}