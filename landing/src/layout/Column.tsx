/** @jsxImportSource @emotion/react */

import type { ReactNode, CSSProperties } from "react";


type ColumnProps = {
    children: ReactNode[] | ReactNode;
    verticalAlignment?: CSSProperties['justifyContent'];
    horizontalAlignment?: CSSProperties['alignItems'];
    gap?: CSSProperties['gap'];
};

const Column = ({
                    children,
                    verticalAlignment = "flex-start",
                    horizontalAlignment = "stretch",
                    gap = 0
                }: ColumnProps) => (
    <div
        css={{
            display: 'flex',
            flexDirection: 'column',
            justifyContent: verticalAlignment,
            alignItems: horizontalAlignment,
            gap: gap,
        }}
    >
        {children}
    </div>
);

export default Column;