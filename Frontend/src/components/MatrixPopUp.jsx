import React, { useState, useRef, useEffect } from 'react';
import ReactDOM from 'react-dom';
import './MatrixPopUp.css';

const trafficLightsGraph = `Graph = [
  [0, 0, 1, 0, 2, 1, 1, 1, 1,],   # Node 1
  [0, 0, 0, 1, 1, 1, 1, 1, 1,],   # Node 2
  [1, 0, 0, 0, 1, 1, 1, 1, 1,],   # Node 3
  [0, 1, 0, 0, 1, 1, 1, 1, 1,],   # Node 4
  [1, 1, 1, 1, 0, 0, 1, 0, 0,],   # Node 5
  [1, 1, 1, 1, 0, 0, 0, 0, 1,],   # Node 6
  [1, 1, 2, 1, 1, 0, 0, 1, 0,],   # Node 7
  [1, 1, 1, 1, 0, 0, 1, 0, 0,],   # Node 8
  [1, 1, 1, 1, 0, 1, 0, 0, 0,]    # Node 9
]`;

const lanesDict = `Dict = [
   1 : 12, 16,
   2 : 11,
   3 : null,
   4 : null,
   5 : 16, 4,
   6 : 15,
   7 : null,
   8 : null,
   9 : 4, 8,
   10 : 3,
   11 : 24,
   12 : 29,
   13 : 8, 12,
   14 : 7,
   15 : null,
   16 : null,
   17 : 29, 9,
   18 : 28,
   19 : null,
   20 : null,
   21 : 9, 20,
   22 : 10,
   23 : 28,
   24 : null,
   25 : null,
   26 : 20, 25,
   27 : 19,
   28 : null,
   29 : null
]`;

const MatrixPopUp = () => {
    const [showPopup, setShowPopup] = useState(false);
    const [graphText, setGraphText] = useState(trafficLightsGraph);
    const [dictText, setDictText] = useState(lanesDict);
    const graphRef = useRef(null);
    const dictRef = useRef(null);

    const togglePopup = () => {
        setShowPopup(!showPopup);
    };

    const handleGraphChange = (e) => {
        setGraphText(e.target.value);
    };

    const handleDictChange = (e) => {
        setDictText(e.target.value);
    };

    // Auto-resize for graph textarea
    useEffect(() => {
        if (graphRef.current) {
            graphRef.current.style.height = 'auto';
            graphRef.current.style.height = graphRef.current.scrollHeight + 'px';
        }
    }, [graphText]);

    // Auto-resize for dict textarea
    useEffect(() => {
        if (dictRef.current) {
            dictRef.current.style.height = 'auto';
            dictRef.current.style.height = dictRef.current.scrollHeight + 'px';
        }
    }, [dictText]);

    const handleSend = async () => {
        const jsonData = { 
            trafficLightsGraph: graphText, 
            lanesDict: dictText 
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
            <button className="start-button" onClick={togglePopup}>Start</button>
            {showPopup &&
                ReactDOM.createPortal(
                    <div className="popup">
                        <div className="popup-inner">
                            <div className="split-container">
                                <textarea 
                                    className="graph-textarea" 
                                    ref={graphRef} 
                                    value={graphText} 
                                    onChange={handleGraphChange} 
                                />
                                <textarea 
                                    className="dict-textarea" 
                                    ref={dictRef} 
                                    value={dictText} 
                                    onChange={handleDictChange} 
                                />
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
