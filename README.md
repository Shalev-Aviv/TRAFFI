# TRAFFI🚦

## Overview
TRAFFI is a dynamic traffic light control simulation aimed at optimizing traffic flow by reducing car wait times at junctions. The project features a user-friendly interface where users can create and simulate different junctions. The system will intelligently adjust traffic light timings based on real-time conditions to improve efficiency. The backend will control Three.js objects to visualize the junction in 3D.

## Features✨
- **React-based GUI:** An intuitive front-end interface for junction selection and traffic visualization.
- **Graph-based Traffic Control:** The system will use graph structures to model junctions and determine optimal traffic light patterns.
- **Dynamic Traffic Light Adjustments:** Prioritizes emergency vehicles and optimizes green light distribution.
- **Real-time Simulation:** Cars will be dynamically added and removed during the simulation.
- **Three.js Integration:** The junction visualization will be implemented using Three.js, controlled by the backend.

## Tech Stack🛠️
- **Frontend:** <a href="https://react.dev/" target="_blank">React<a>, <a href="https://threejs.org/" target="_blank">Three.js<a>
- **Backend:** <a href="https://spring.io/projects/spring-boot" target="_blank">Spring Boot<a>
- **Two-Way Communication channels:** <a href="https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API" target="_blank">WebSockets<a>

## Installation & Usage🚀
### Prerequisites
- <a href="https://nodejs.org/en" target="_blank">Node.js & npm<a> (for the frontend)
- <a href="https://www.oracle.com/java/technologies/downloads/" target="_blank">JDK<a> & <a href="https://maven.apache.org/download.cgi" target="_blank">Apache Maven<a> (for the backend)
### Recommended Versions
- Node ^=20.12.0
- npm ^=10.5.0
- JDK ^=21
- Apache Maven ^=3.9.9
### Running the Backend
```sh
cd Backend
mvn spring-boot:run
```
### First time running the Frontend
```sh
cd Frontend
npm install
npm start
```
### Second time running the Frontend
```sh
cd Frontend
npm start
```

## Roadmap🗺️
- [x] Frontend
  - [x] Create the hero section
  - [x] Create navbar
  - [x] Adding smooth scroll
  - [x] Implement Three.js visualization
- [x] Backend
  - [x] JSON parsing
  - [x] Car class
  - [x] Lane class
  - [x] TrafficLight class
  - [x] TraffiApplication class
  - [x] Junction class
- [x] Both
  - [x] Create `Start Simulaion` logic
  - [x] Car's web socket
  - [x] traffic light's web socket
  - [x] Implement `Pause/Resume` logic
  - [x] Provide statistics

## Contribution🤝
Contributions are welcome! Feel free to submit issues or pull requests.

## License📄
This project is licensed under the MIT License.