/** @jsxImportSource @emotion/react */

import type { ReactNode, CSSProperties } from "react";


type RowProps = {
    children: ReactNode[] | ReactNode;
    verticalAlignment?: CSSProperties['alignItems'];
    horizontalAlignment?: CSSProperties['justifyContent'];
    gap?: CSSProperties['gap'];
};

const Row = ({
                 children,
                 verticalAlignment = "center",
                 horizontalAlignment = "flex-start",
                 gap = 0
             }: RowProps) => (
    <div
        css={{
            display: 'flex',
            flexDirection: 'row',
            alignItems: verticalAlignment,
            justifyContent: horizontalAlignment,
            gap: gap,
        }}
    >
        {children}
    </div>
);

export default Row;
