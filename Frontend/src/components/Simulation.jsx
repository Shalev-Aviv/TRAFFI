import { useEffect, useRef } from "react";
import './Simulation.css';
import * as THREE from "three";
import { GLTFLoader } from "three/examples/jsm/loaders/GLTFLoader";

function Simulation () {
    const canvasRef = useRef(null);
    const sceneRef = useRef(new THREE.Scene());
    const carObjects = useRef([]);
    const trafficLights = useRef({});
    const trafficLightStatuses = useRef({}); // Added to track traffic light status

    let socketCars = null;
    let socketTraffic = null; // Declare socketTraffic

    let camera, renderer;

    const CANVAS_WIDTH_EM = 70;
    const CANVAS_HEIGHT_EM = 33.33;

    // Helper: map car type to color
    const carColorByType = (type) => {
        return type === "POLICE" || type === "AMBULANCE" ? 0x0000ff : 0xff0000; // Blue for emergency vehicles, red for others
    };

    // Add a mapping for lane starting positions
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
    // Mapping for lane directions. Replace with actual directions for each lane.
    const laneDirections = {
        1: { x: 1, y: 0, z: 0 },  // Move along +X
        2: { x: 1, y: 0, z: 0 },  // Move along +X
        3: { x: -1, y: 0, z: 0 }, // Move along -X
        4: { x: -1, y: 0, z: 0 }, // Move along -X
        5: { x: 0, y: 0, z: 1 }, // Move along +Z
        6: { x: 0, y: 0, z: 1 }, // Move along +Z
        7: { x: 0, y: 0, z: -1 },  // Move along -Z
        8: { x: 0, y: 0, z: -1 },  // Move along -Z
        9: { x: -1, y: 0, z: 0 }, // Move along -X
        10: { x: -1, y: 0, z: 0 }, // Move along -X
        11: { x: 1, y: 0, z: 0 }, // Move along +X
        12: { x: 1, y: 0, z: 0 }, // Move along +X
        13: { x: 0, y: 0, z: -1 }, // Move along -Z
        14: { x: 0, y: 0, z: -1 }, // Move along -Z
        15: { x: 0, y: 0, z: 1 }, // Move along +Z
        16: { x: 0, y: 0, z: 1 }, // Move along +Z
        17: { x: 0, y: 0, z: 1 }, // Move along +Z
        18: { x: 0, y: 0, z: 1 }, // Move along +Z
        19: { x: 0, y: 0, z: -1 }, // Move along -Z
        20: { x: -1, y: 0, z: -1 }, // Move along -Z
        21: { x: -1, y: 0, z: 0 }, // Move along -X
        22: { x: -1, y: 0, z: 0 }, // Move along -X
        23: { x: -1, y: 0, z: 0 }, // Move along -X
        24: { x: 1, y: 0, z: 0 }, // Move along +X
        25: { x: 1, y: 0, z: 0 }, // Move along +X
        26: { x: 0, y: 0, z: -1 }, // Move along -Z
        27: { x: -1, y: 0, z: -1 }, // Move along -Z
        28: { x: 0, y: 0, z: 1 }, // Move along +Z
        29: { x: 0, y: 0, z: 1 }, // Move along +Z
    };
    // Create a car mesh and place it based on its lane number.
    const addCarToScene = (lane, type) => {
        const scene = sceneRef.current;
        const geometry = new THREE.CircleGeometry(0.5, 16);
        const material = new THREE.MeshBasicMaterial({
            color: carColorByType(type)
        });
        const carMesh = new THREE.Mesh(geometry, material);
        carMesh.rotation.x = -Math.PI / 2;
        // Default position if lane not found.
        const startPos = laneStartPositions[lane] || { x: 0, y: 0, z: 0 };
        carMesh.position.set(startPos.x, startPos.y, startPos.z);
    
        // Save the lane and type in the mesh's userData.
        carMesh.userData = { lane, type };
        
        scene.add(carMesh);
        carObjects.current.push(carMesh);
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
                        // Initialize status to red
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

        // Animation loop: update car positions if their controlling light is green.
        const animate = () => {
            carObjects.current.forEach(car => {
                const lane = car.userData.lane;
                const direction = laneDirections[lane] || { x: 0, y: 0, z: 0 }; // Default to no movement if lane not found
        
                // Move the car only if the traffic light for its lane is green
                if (trafficLightStatuses.current[lane]) {
                    car.position.x += direction.x * 0.1; // Adjust speed as needed
                    car.position.y += direction.y * 0.1;
                    car.position.z += direction.z * 0.1;
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

        // Setup WebSocket connection for cars.
        socketCars = new WebSocket("ws://localhost:8080/cars");
        socketCars.onmessage = (event) => {
            const data = JSON.parse(event.data);
            console.log("Received car update:", data);
            addCarToScene(data.lane, data.type);
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