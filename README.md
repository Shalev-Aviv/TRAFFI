# TRAFFI

## Overview
TRAFFI is a dynamic traffic light control simulation aimed at optimizing traffic flow by reducing car wait times at junctions. The project features a user-friendly interface where users can create and simulate different junctions. The system will intelligently adjust traffic light timings based on real-time conditions to improve efficiency. The backend will control Three.js objects for visualizing the junction in 3D.

## Features
- **React-based GUI**: An intuitive front-end interface for junction selection and traffic visualization.
- **Graph-based Traffic Control**: The system will use graph structures to model junctions and determine optimal traffic light patterns.
- **Dynamic Traffic Light Adjustments**: Prioritizes emergency vehicles and optimizes green light distribution.
- **Real-time Simulation**: Cars will be dynamically added and removed during the simulation.
- **Three.js Integration**: The junction visualization will be implemented using Three.js, controlled by the backend.

## Tech Stack
- **Frontend**: React 18 (Implemented), Three.js (Planned)
- **Backend**: Java 21 (Coming soon)

## Installation & Usage
### Prerequisites
- Node.js and npm (for the frontend)
- JDK 21 (for the backend, once implemented)
### Recommended
- VS-Code (To run the frontend)
- IntelliJ (To run the backend)

### Running the Frontend
```sh
cd Frontend
npm install
npm install locomotive-scroll
npm start
```

### Running the Backend (Coming soon)
```sh
cd Backend
javac Main.java
java Main
```

## Roadmap
- [x] Implement GUI in React
- [ ] Implement backend
- [ ] Integrate frontend and backend
- [ ] Implement Three.js visualization
- [ ] Optimize traffic flow algorithms

## Contribution
Contributions are welcome! Feel free to submit issues or pull requests.

## License
This project is licensed under the MIT License.

