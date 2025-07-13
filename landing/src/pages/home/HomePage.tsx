/** @jsxImportSource @emotion/react */
import Header from "./Header";
import "./HomePage.css"
import Footer from "./Footer.tsx";
import Main from "./Main.tsx";


function HomePage() {
    return <>
        <div css={{width: "100%"}}>
            <Header/>
            <Main></Main>
        </div>
        <Footer/>
    </>
}

export default HomePage;
