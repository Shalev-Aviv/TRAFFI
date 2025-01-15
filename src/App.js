import './App.css';
import Master from './components/Master.jsx'
import Header from './components/Header.jsx'
import SecondHeader from './components/SecondHeader.jsx'

function App() {
  return (
    <div className="App">
      <Master />
      <div className='Hero'>
        <Header />
        <SecondHeader />
      </div>
    </div>
  );
}

export default App;