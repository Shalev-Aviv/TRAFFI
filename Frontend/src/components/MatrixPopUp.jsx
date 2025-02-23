import React, { useState } from 'react';
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

    const handleSend = async () => {
        const jsonData = { message: text };

        try {
            const response = await fetch('http://localhost:8080/api/json', { // Replace with your backend endpoint
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(jsonData),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(`HTTP error! status: ${response.status}, message: ${errorData.message || response.statusText}`);
            }

            const responseData = await response.json();
            console.log('Success:', responseData);
            // Handle success, e.g., display a success message to the user
            setShowPopup(false); // Close the popup after successful send

        } catch (error) {
            console.error('Error sending JSON:', error);
            // Handle error, e.g., display an error message to the user
        }
    };

    return (
        <div>
            <button className="start-button" onClick={togglePopup}>Start</button>
            {showPopup &&
                ReactDOM.createPortal(
                    <div className="popup">
                        <div className="popup-inner">
                            <textarea value={text} onChange={handleTextChange} />
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