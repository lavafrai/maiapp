import {BrowserRouter, Route, Routes} from "react-router-dom";
import HomePage from "./pages/home/HomePage.tsx";
import DownloadPage from "./pages/download/DownloadPage.tsx";
import NotFound from "./pages/notfound/NotFound.tsx";

function App() {
    // const [count, setCount] = useState(0)

    return (
        <BrowserRouter>
            <Routes>
                <Route path='*' element={<NotFound/>} />
                <Route path="/" element={<HomePage/>}></Route>
                <Route path="/download" element={<DownloadPage/>}></Route>
            </Routes>
        </BrowserRouter>
    )
}

export default App
