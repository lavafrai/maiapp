/** @jsxImportSource @emotion/react */
import DescriptionSection from "./DescriptionSection.tsx";
import PhotosSection from "./PhotosSection.tsx";
import InfoSection from "./InfoSection.tsx";


function Main() {
    return <main css={{
        width: "100%",
        maxWidth: "900px",
        margin: "0 auto",
        padding: "8px 12px",
        boxSizing: "border-box",
    }}>
        <DescriptionSection/>
        <PhotosSection/>
        <InfoSection/>
    </main>
}

export default Main;