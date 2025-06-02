import "./App.css";
import Scrollbar from "./components/Scrollbar.jsx";
import Navbar from "./components/Navbar.jsx";
import Header from "./components/Header.jsx";
import SecondHeader from "./components/SecondHeader.jsx";
import MatrixPopUp from "./components/MatrixPopUp.jsx";
import Simulation from "./components/Simulation.jsx";
import Text from "./components/Text.jsx";

function App() {
  return (
    <div className="App">
      <Navbar />
      <Scrollbar>
        <div className="Hero">
          <Header />
          <SecondHeader />
        </div>
        <MatrixPopUp />
        <Simulation />
        <Text />
      </Scrollbar>
    </div>
  );
}

export default App;
