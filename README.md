# TRAFFI🚦

## Overview
TRAFFI is a dynamic traffic light control simulation aimed at optimizing traffic flow by reducing car wait times at junctions. The project features a user-friendly interface where users can create and simulate different junctions. The system will intelligently adjust traffic light timings based on real-time conditions to improve efficiency. The backend will control Three.js objects to visualize the junction in 3D.

## Features✨
- **React-based GUI**: An intuitive front-end interface for junction selection and traffic visualization.
- **Graph-based Traffic Control**: The system will use graph structures to model junctions and determine optimal traffic light patterns.
- **Dynamic Traffic Light Adjustments**: Prioritizes emergency vehicles and optimizes green light distribution.
- **Real-time Simulation**: Cars will be dynamically added and removed during the simulation.
- **Three.js Integration**: The junction visualization will be implemented using Three.js, controlled by the backend.

## Tech Stack🛠️
- **Frontend**: React 18 (Implemented), Three.js (Planned)
- **Backend**: Java 21 (In progress)

## Installation & Usage🚀
### Prerequisites
- Node.js & npm (for the frontend)
- JDK & Apache Maven (for the backend, once implemented)
### Recommended Versions
- Node 20.12.0
- npm 10.5.0
- JDK 21
- Apache Maven 3.9.9

### Running the Frontend
```sh
cd Frontend
npm install
npm install locomotive-scroll
npm start
```

### Running the Backend
```sh
cd Backend
mvn spring-boot:run
```

## Roadmap🗺️
- [x] Implement GUI in React
  - [x] Create the hero section
  - [x] Create the navigation area
  - [x] Adding smooth scroll
  - [x] Develop the start button and its popup
- [ ] Implement backend
  - [x] Implement weight system to prioritize traffic lights
  - [x] Car class
  - [x] Lane class
  - [x] TrafficLight class
  - [ ] Junction class (⏳ 80% done)
  - [x] JSON parsing
  - [x] Graph-theory algorithm to find the best set of traffic lights to get green color
- [ ] Implement Three.js visualization
- [ ] Optimize traffic flow algorithms

## Contribution🤝
Contributions are welcome! Feel free to submit issues or pull requests.

## License📄
This project is licensed under the MIT License.
