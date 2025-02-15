import React, { useState, useEffect, useRef } from 'react';
import ReactDOM from 'react-dom';
import './MatrixPopUp.css';

const MatrixPopUp = () => {
    const [showPopup, setShowPopup] = useState(false);
    const [text, setText] = useState("G = [\n  [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,],   # Node 1\n  [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,],   # Node 2\n  [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,],   # Node 3\n  [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,],   # Node 4\n  [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,],   # Node 5\n  [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,],   # Node 6\n  [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,],   # Node 7\n  [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,],   # Node 8\n  [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,],   # Node 9\n  [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,],   # Node 10\n  [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,],   # Node 11\n  [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,],   # Node 12\n  [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,],   # Node 13\n  [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,],   # Node 14\n  [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,],   # Node 15\n  [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,]    # Node 16\n]");

    const togglePopup = () => {
        setShowPopup(!showPopup);
    };

    const handleTextChange = (e) => {
        setText(e.target.value);
    };

    const handleSend = () => {
        const fileData = JSON.stringify({ message: text });
        const blob = new Blob([fileData], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'message.json';
        a.click();
        URL.revokeObjectURL(url);
        setShowPopup(false);
    };

    return (
        <div>
            <button className="start-button" onClick={togglePopup}>Start</button>
            {showPopup &&
                ReactDOM.createPortal(
                    <div className="popup">
                        <div className="popup-inner">
                            <textarea type="text" value={text} onChange={handleTextChange} />
                            <div className="popup-buttons">
                                <button className='send' onClick={handleSend}>Send</button>
                                <button className='close' onClick={togglePopup}>Close</button>
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
