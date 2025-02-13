import React, { useState, useEffect } from 'react';
import './MatrixPopUp.css';

const MatrixPopUp = () => {
    const [showPopup, setShowPopup] = useState(false);
    const [text, setText] = useState('');

    const togglePopup = () => {
        setShowPopup(!showPopup);
    };

    const handleTextChange = (e) => {
        setText(e.target.value);
    };

    const handleSend = () => {
        const fileData = JSON.stringify({ message: text || "12345" });
        const blob = new Blob([fileData], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'message.json';
        a.click();
        URL.revokeObjectURL(url);
        setShowPopup(false); // Close the popup after sending
        // Note: Saving directly to a backend folder isn't possible from frontend code.
        // This is a security restriction. You would need to send the data to a backend
        // endpoint which then saves the file on the server.
        // The current implementation allows the user to download the file.
        };

        return (
        <div>
            <button className="start-button" onClick={togglePopup}>Start</button>
            {showPopup && (
            <div className="popup">
                <div className="popup-inner">
                    <textarea type="text" value={text || "G\n[\n[ 1 , 1 , 1 , 0 , 0 ] # node 1\n[ 1 , 0 , 1 , 0 , 1 ] # node 2\n]"} onChange={handleTextChange} />
                    <div className="popup-buttons">
                        <button className='send' onClick={handleSend}>Send</button>
                        <button className='close' onClick={togglePopup}>Close</button>
                    </div>
                </div>
            </div>
            )}
        </div>
        );
    };

    export default MatrixPopUp;
