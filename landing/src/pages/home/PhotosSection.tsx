/** @jsxImportSource @emotion/react */
import Viewer from "react-viewer";
import screenshot1 from "../../assets/screenshots/screenshot1.png";
import screenshot2 from "../../assets/screenshots/screenshot2.png";
import screenshot3 from "../../assets/screenshots/screenshot3.png";
import screenshot4 from "../../assets/screenshots/screenshot4.png";
import screenshot5 from "../../assets/screenshots/screenshot5.png";
import screenshot6 from "../../assets/screenshots/screenshot6.png";
import {useState} from "react";


export default function PhotosSection() {
    const [ visible, setVisible ] = useState(false);
    const [ currentImage, setCurrentImage ] = useState(0);

    const openScreenshot = (image: number) => {
        setCurrentImage(image);
        setVisible(true);
    }

    let images = [
        {src: screenshot1},
        {src: screenshot2},
        {src: screenshot3},
        {src: screenshot4},
        {src: screenshot5},
        {src: screenshot6}
    ]

    return <section>
        <ul id="screenshots" css={{
            display: "flex",
            padding: "0",
            overflowX: "auto",

            "& > li > img": {
                height: "200px",
                borderRadius: "4px",
                marginRight: "16px",
            },

            "& > li:last-child > img": {
                marginRight: "0",
            },

            "& > li": {
                listStyle: "none",
                flexShrink: 0,
            }
        }}>
            {images.map((image, index) => (
                <li key={index} onClick={() => openScreenshot(index)}>
                    <img src={image.src} alt={`Screenshot ${index + 1}`} />
                </li>
            ))}
        </ul>

        <Viewer
            activeIndex={currentImage}
            visible={visible}
            onClose={() => { setVisible(false) }}
            onMaskClick={() => { setVisible(false) }}
            images={images}></Viewer>
    </section>
}
