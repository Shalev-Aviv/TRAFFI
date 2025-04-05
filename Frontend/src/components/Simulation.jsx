import { useEffect, useRef } from "react";
import './Simulation.css';
import * as THREE from "three";
import { GLTFLoader } from "three/examples/jsm/loaders/GLTFLoader";

function Simulation () {
    const canvasRef = useRef(null);
    const sceneRef = useRef(new THREE.Scene());
    // Instead of an array, use a Map to track car meshes by a unique id
    const carMeshes = useRef(new Map());
    const trafficLights = useRef({});
    const trafficLightStatuses = useRef({});

    let socketCars = null;
    let socketTraffic = null;

    let camera, renderer;

    const CANVAS_WIDTH_EM = 70;
    const CANVAS_HEIGHT_EM = 33.33;

    // Helper: map car type to color
    const carColorByType = (type) => {
        return type === "POLICE" || type === "AMBULANCE" ? 0x0000ff : 0xff0000; // Blue for emergency vehicles, red for others
    };

    // Mapping from lane to traffic light ID.
    const laneToTrafficLightMapping = {
        1: 1,
        2: 1,
        3: null,
        4: null,
        5: 2,
        6: 2,
        7: null,
        8: null,
        9: 3,
        10: 3,
        11: 5,
        12: 5,
        13: 4,
        14: 4,
        15: null,
        16: null,
        17: 6,
        18: 6,
        19: null,
        20: null,
        21: 7,
        22: 7,
        23: 8,
        24: null,
        25: null,
        26: 9,
        27: 9,
        28: null,
        29: null
    };
    // Mapping for lane starting positions (actual values already replaced)
    const laneStartPositions = {
        1: { x: -85, y: 2, z: 6.5 },
        2: { x: -85, y: 2, z: 2 },
        3: { x: -36.5, y: 2, z: -2 },
        4: { x: -36.5, y: 2, z: -6 },
        5: { x: -33, y: 2, z: -38.5 },
        6: { x: -29.5, y: 2, z: -38.5 },
        7: { x: -25.5, y: 2, z: -10 },
        8: { x: -20, y: 2, z: -10 },
        9: { x: 17.5, y: 2, z: -6.5 },
        10: { x: 17.5, y: 2, z: -2 },
        11: { x: -17, y: 2, z: 2 },
        12: { x: -17, y: 2, z: 6.5 },
        13: { x: -20, y: 2, z: 39 },
        14: { x: -25, y: 2, z: 39 },
        15: { x: -28.5, y: 2, z: 10 },
        16: { x: -33.5, y: 2, z: 10 },
        17: { x: 20, y: 2, z: -39 },
        18: { x: 25, y: 2, z: -39 },
        19: { x: 28.5, y: 2, z: -10 },
        20: { x: 33, y: 2, z: -10 },
        21: { x: 85, y: 2, z: -6.5 },
        22: { x: 85, y: 2, z: -2 },
        23: { x: 85, y: 2, z: 0 },
        24: { x: 36.5, y: 2, z: 2 },
        25: { x: 36.5, y: 2, z: 6.5 },
        26: { x: 33.5, y: 2, z: 39 },
        27: { x: 29, y: 2, z: 39 },
        28: { x: 25, y: 2, z: 9.5 },
        29: { x: 20.5, y: 2, z: 9.5 },
    };

    // Mapping for lane movement directions (actual values replaced)
    const laneDirections = {
        1: { x: 1, y: 0, z: 0 },
        2: { x: 1, y: 0, z: 0 },
        3: { x: -1, y: 0, z: 0 },
        4: { x: -1, y: 0, z: 0 },
        5: { x: 0, y: 0, z: 1 },
        6: { x: 0, y: 0, z: 1 },
        7: { x: 0, y: 0, z: -1 },
        8: { x: 0, y: 0, z: -1 },
        9: { x: -1, y: 0, z: 0 },
        10: { x: -1, y: 0, z: 0 },
        11: { x: 1, y: 0, z: 0 },
        12: { x: 1, y: 0, z: 0 },
        13: { x: 0, y: 0, z: -1 },
        14: { x: 0, y: 0, z: -1 },
        15: { x: 0, y: 0, z: 1 },
        16: { x: 0, y: 0, z: 1 },
        17: { x: 0, y: 0, z: 1 },
        18: { x: 0, y: 0, z: 1 },
        19: { x: 0, y: 0, z: -1 },
        20: { x: -1, y: 0, z: -1 },
        21: { x: -1, y: 0, z: 0 },
        22: { x: -1, y: 0, z: 0 },
        23: { x: -1, y: 0, z: 0 },
        24: { x: 1, y: 0, z: 0 },
        25: { x: 1, y: 0, z: 0 },
        26: { x: 0, y: 0, z: -1 },
        27: { x: -1, y: 0, z: -1 },
        28: { x: 0, y: 0, z: 1 },
        29: { x: 0, y: 0, z: 1 },
    };

    // Create a car mesh; if new then add it and if it already exists, update its lane & starting position.
    const upsertCarMesh = (carId, lane, type) => {
        const scene = sceneRef.current;
        let mesh = carMeshes.current.get(carId);
        if (!mesh) {
            const geometry = new THREE.CircleGeometry(0.5, 16);
            const material = new THREE.MeshBasicMaterial({ color: carColorByType(type) });
            mesh = new THREE.Mesh(geometry, material);
            mesh.rotation.x = -Math.PI / 2;
            carMeshes.current.set(carId, mesh);
            scene.add(mesh);
        }
        mesh.userData = { carId, lane, type };
        const startPos = laneStartPositions[lane] || { x: 0, y: 0, z: 0 };
        mesh.position.set(startPos.x, startPos.y, startPos.z);
    };

    useEffect(() => {
        // Materials for traffic lights
        const greenMaterial = new THREE.MeshStandardMaterial({
            color: 0x00ff00,
            emissive: 0x00ff00,
            emissiveIntensity: 0.5
        });
        const redMaterial = new THREE.MeshStandardMaterial({
            color: 0xff0000,
            emissive: 0xff0000,
            emissiveIntensity: 0.5
        });
        // Function to update traffic light color and status.
        const updateTrafficLight = (lightId, status) => {
            const light = trafficLights.current[`trafficLight${lightId}`];
            trafficLightStatuses.current[lightId] = status === "GREEN";
            if (light) {
                light.material = status === "GREEN" ? greenMaterial.clone() : redMaterial.clone();
            }
        };

        // Size adjustments
        document.documentElement.style.setProperty('--canvas-width', `${CANVAS_WIDTH_EM}em`);
        document.documentElement.style.setProperty('--canvas-height', `${CANVAS_HEIGHT_EM}em`);

        const scene = sceneRef.current;
        scene.background = new THREE.Color('white');
        const aspectRatio = CANVAS_WIDTH_EM / CANVAS_HEIGHT_EM;

        // Camera setup
        camera = new THREE.PerspectiveCamera(50, aspectRatio, 0.1, 1000);
        camera.position.set(0, 100, 0);
        camera.lookAt(0, 0, 0);

        // Load junction model and store traffic light objects.
        const gltfLoader = new GLTFLoader();
        gltfLoader.load('/junction.glb', (gltfScene) => {
            const loadedModel = gltfScene.scene;
            scene.add(loadedModel);
            for (let i = 1; i <= 9; i++) {
                loadedModel.traverse((object) => {
                    if (object.name === `trafficLight${i}`) {
                        console.log(`Found traffic light: ${object.name}`);
                        trafficLights.current[object.name] = object;
                        if (object.material) {
                            object.material = redMaterial.clone();
                        }
                        // Initialize status to red.
                        trafficLightStatuses.current[i] = false;
                    }
                });
            }
        });

        // Renderer setup
        renderer = new THREE.WebGLRenderer({ canvas: canvasRef.current, antialias: true });
        renderer.setClearColor('white', 1);
        const updateSize = () => {
            const fontSize = parseFloat(getComputedStyle(document.documentElement).fontSize);
            const widthPx = CANVAS_WIDTH_EM * fontSize;
            const heightPx = CANVAS_HEIGHT_EM * fontSize;
            renderer.setSize(widthPx, heightPx);
            camera.aspect = widthPx / heightPx;
            camera.updateProjectionMatrix();
        };
        updateSize();
        window.addEventListener("resize", updateSize);

        // Add a directional light
        const directionalLight = new THREE.DirectionalLight(0x1c1c1c, 50);
        directionalLight.position.set(0, 10, 0);
        scene.add(directionalLight);

        // Animation loop: update car positions based on their current lane’s direction.
        // In your animation loop, always move cars if there is no mapping.
        // Also remove cars that have exited the visible area (using a removal threshold).
        const moveSpeed = 0.1;
        const removalThreshold = 200; // Example value; adjust as needed
            
        const animate = () => {
            carMeshes.current.forEach((mesh, carId) => {
                const { lane } = mesh.userData;
                const controllingLightId = laneToTrafficLightMapping[lane];
                const direction = laneDirections[lane] || { x: 0, y: 0, z: 0 };
                
                // If no traffic light controls this lane, always move.
                if (controllingLightId === null || controllingLightId === undefined) {
                    mesh.position.x += direction.x * moveSpeed;
                    mesh.position.y += direction.y * moveSpeed;
                    mesh.position.z += direction.z * moveSpeed;
                }
                // Otherwise, only move if the mapped traffic light is green.
                else if (trafficLightStatuses.current[controllingLightId]) {
                    mesh.position.x += direction.x * moveSpeed;
                    mesh.position.y += direction.y * moveSpeed;
                    mesh.position.z += direction.z * moveSpeed;
                }
            
                // Remove the car if it moves too far from the camera.
                // You could use distanceTo() from the camera position.
                if (mesh.position.distanceTo(camera.position) > removalThreshold) {
                    scene.remove(mesh);
                    carMeshes.current.delete(carId);
                }
            });
            renderer.render(scene, camera);
            requestAnimationFrame(animate);
        };
        animate();

        // Setup WebSocket connection for traffic light data.
        socketTraffic = new WebSocket("ws://localhost:8080/traffic");
        socketTraffic.onmessage = (event) => {
            const data = JSON.parse(event.data);
            console.log("Received traffic light update:", data);
            updateTrafficLight(data.lightId, data.status);
        };

        /* 
          Setup WebSocket connection for car data.
          Expecting the message to include at least:
          {
             "carId": "unique identifier",
             "lane": <new lane>,
             "type": "PRIVATE" | "MOTORCYCLE" | "POLICE" | "AMBULANCE"
          }
          If the car already exists, update its lane (and reset its position so that its movement direction changes).
        */
        socketCars = new WebSocket("ws://localhost:8080/cars");
        socketCars.onmessage = (event) => {
            const data = JSON.parse(event.data);
            console.log("Received car update:", data);
            // Upsert the car mesh based on its unique ID.
            upsertCarMesh(data.carId, data.lane, data.type);

            /* 
              NOTE: If you find that the connection between lanes and traffic lights isn't
              correct (for example, if lane 2 is not controlled by traffic light 1), verify your
              backend logic. You may need to adjust either the mapping in the backend or in the
              frontend so that each car’s lane properly determines which traffic light controls it.
            */
        };

        return () => {
            socketCars?.close();
            socketTraffic?.close();
            window.removeEventListener("resize", updateSize);
        };
    }, []);

    return <canvas ref={canvasRef} className="ThreeJS"></canvas>;
}

export default Simulation;