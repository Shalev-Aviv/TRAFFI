import React, { useState, useRef, useEffect } from 'react';
import ReactDOM from 'react-dom';
import './MatrixPopUp.css';

const trafficLightsMatrix = `{
  "Lights To Lights": [
    [0, 0, 1, 0, 2, 1, 1, 1, 1],
    [0, 0, 0, 1, 1, 1, 1, 1, 1],
    [1, 0, 0, 0, 1, 1, 1, 1, 1],
    [0, 1, 0, 0, 1, 1, 1, 1, 1],
    [1, 1, 1, 1, 0, 0, 1, 0, 0],
    [1, 1, 1, 1, 0, 0, 0, 0, 1],
    [1, 1, 2, 1, 1, 0, 0, 1, 0],
    [1, 1, 1, 1, 0, 0, 1, 0, 0],
    [1, 1, 1, 1, 0, 1, 0, 0, 0]
  ]
}`;

const lightsToLanesMap = `{
  "Lights To Lanes": {
    "Light 1": [1, 2],
    "Light 2": [5, 6],
    "Light 3": [9, 10],
    "Light 4": [13, 14],
    "Light 5": [11, 12],
    "Light 6": [17, 18],
    "Light 7": [21, 22],
    "Light 8": [23],
    "Light 9": [26, 27]
  }
}`;

const lanesToLanesMap = `{
  "Lanes To Lanes": {
    "1": [12, 16],
    "2": [11],
    "3": null,
    "4": null,
    "5": [16, 4],
    "6": [15],
    "7": null,
    "8": null,
    "9": [4, 8],
    "10": [3],
    "11": [24],
    "12": [29],
    "13": [8, 12],
    "14": [7],
    "15": null,
    "16": null,
    "17": [29, 9],
    "18": [28],
    "19": null,
    "20": null,
    "21": [9, 20],
    "22": [10],
    "23": [28],
    "24": null,
    "25": null,
    "26": [20, 25],
    "27": [19],
    "28": null,
    "29": null
  }
}`;

const MatrixPopUp = () => {
    const [showPopup, setShowPopup] = useState(false);
    const [graphText1, setGraphText1] = useState(trafficLightsMatrix);
    const [graphText2, setGraphText2] = useState(lightsToLanesMap);
    const [dictText, setDictText] = useState(lanesToLanesMap);
    const graphRef1 = useRef(null);
    const graphRef2 = useRef(null);
    const dictRef = useRef(null);

    const togglePopup = () => {
        setShowPopup(!showPopup);
    };

    // Prevent empty textarea by setting default if value is empty
    const handleGraph1Change = (e) => {
        setGraphText1(e.target.value || trafficLightsMatrix);
    };
    const handleGraph2Change = (e) => {
        setGraphText2(e.target.value || lightsToLanesMap);
    };
    const handleDictChange = (e) => {
        setDictText(e.target.value || lanesToLanesMap);
    };

    const handleSend = async () => {
        const jsonData = { 
            trafficLightsMatrix: graphText1, 
            lightsToLanesMap: graphText2,
            lanesToLanesMap: dictText 
        };

        try {
            const response = await fetch('http://localhost:8080/api/json', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(jsonData),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(`HTTP error! status: ${response.status}, message: ${errorData.message || response.statusText}`);
            }

            const responseData = await response.json();
            console.log('Success:', responseData);
            setShowPopup(false);
        } catch (error) {
            console.error('Error sending JSON:', error);
        }
    };

    return (
        <div>
            <div className='auter-stroke'>
                <button className="start-button" onClick={togglePopup}>Start simulation</button>
            </div>
            {showPopup &&
                ReactDOM.createPortal(
                    <div className="popup">
                        <div className="popup-inner">
                            <div className="split-container">
                                <div className="left-container">
                                    <textarea className="graph-textarea" ref={graphRef1} value={graphText1} onChange={handleGraph1Change} />
                                    <textarea className="graph-textarea" ref={graphRef2} value={graphText2} onChange={handleGraph2Change} />
                                </div>
                                <div className="right-container">
                                    <textarea className="dict-textarea" ref={dictRef} value={dictText} onChange={handleDictChange} 
                                    />
                                </div>
                            </div>
                            <div className="popup-buttons">
                                <button className="send" onClick={handleSend}>Send</button>
                                <button className="close" onClick={togglePopup}>Close</button>
                            </div>
                        </div>
                    </div>,
                    document.body
                )
            }
        </div>
    );
};

export default MatrixPopUp;