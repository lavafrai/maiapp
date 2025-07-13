import {BrowserRouter, Route, Routes} from "react-router-dom";
import HomePage from "./pages/home/HomePage.tsx";

function App() {
    // const [count, setCount] = useState(0)

    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<HomePage/>}></Route>
                <Route path="/download" element={<>Download</>}></Route>
            </Routes>
        </BrowserRouter>
    )
}

export default App
