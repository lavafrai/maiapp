/** @jsxImportSource @emotion/react */

import {type ReactNode, type CSSProperties} from "react";
import useWindowDimensions from "../utils/Dimensions";


type CombinedProps = {
    children: ReactNode[] | ReactNode;
    rowVerticalAlignment?: CSSProperties['justifyContent'];
    rowHorizontalAlignment?: CSSProperties['alignItems'];
    columnVerticalAlignment?: CSSProperties['justifyContent'];
    columnHorizontalAlignment?: CSSProperties['alignItems'];
    gap?: CSSProperties['gap'];
};

function Combined({
                    children,
                    rowVerticalAlignment = "flex-start",
                    rowHorizontalAlignment = "stretch",
                    columnVerticalAlignment = "flex-start",
                    columnHorizontalAlignment = "stretch",
                    gap = 0
                }: CombinedProps) {
    let dimensions = useWindowDimensions();
    let direction = (dimensions.width > 900 ? 'row' : 'column') as CSSProperties['flexDirection'];
    let justifyContent = (dimensions.width > 900 ? rowHorizontalAlignment : columnVerticalAlignment) as CSSProperties['justifyContent'];
    let alignItems = (dimensions.width > 900 ? rowVerticalAlignment : columnHorizontalAlignment) as CSSProperties['alignItems'];

    return <div
        css={{
            display: 'flex',
            flexDirection: direction,
            justifyContent: justifyContent,
            alignItems: alignItems,
            gap: gap,
        }}
    >
        {children}
    </div>
}

export default Combined;
