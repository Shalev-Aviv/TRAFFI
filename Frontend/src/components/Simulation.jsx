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

    // Mapping from lane to traffic light ID (null = exiting lane).
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
    // Mapping for lane starting positions
    const laneStartPositions = {
        1: { x: -85, y: 2, z: 6.5 },
        2: { x: -85, y: 2, z: 2 },
        3: { x: -36.5, y: 2, z: -2 },
        4: { x: -36.5, y: 2, z: -6 },
        5: { x: -35, y: 2, z: -38.5 },
        6: { x: -30, y: 2, z: -38.5 },
        7: { x: -25.5, y: 2, z: -10 },
        8: { x: -20, y: 2, z: -10 },
        9: { x: 17.5, y: 2, z: -6.5 },
        10: { x: 17.5, y: 2, z: -2 },
        11: { x: -17, y: 2, z: 2 },
        12: { x: -17, y: 2, z: 8 },
        13: { x: -21, y: 2, z: 39 },
        14: { x: -26, y: 2, z: 39 },
        15: { x: -28.5, y: 2, z: 10 },
        16: { x: -33.5, y: 2, z: 10 },
        17: { x: 20, y: 2, z: -39 },
        18: { x: 25, y: 2, z: -39 },
        19: { x: 28.5, y: 2, z: -10 },
        20: { x: 33, y: 2, z: -10 },
        21: { x: 85, y: 2, z: -7.5 },
        22: { x: 85, y: 2, z: -3 },
        23: { x: 85, y: 2, z: 0.5 },
        24: { x: 36.5, y: 2, z: 2 },
        25: { x: 36.5, y: 2, z: 6.5 },
        26: { x: 35, y: 2, z: 39 },
        27: { x: 30, y: 2, z: 39 },
        28: { x: 26, y: 2, z: 9.5 },
        29: { x: 21, y: 2, z: 9.5 },
    };

    // Mapping for lane movement directions
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
        20: { x: 0, y: 0, z: -1 },
        21: { x: -1, y: 0, z: 0 },
        22: { x: -1, y: 0, z: 0 },
        23: { x: -1, y: 0, z: 0 },
        24: { x: 1, y: 0, z: 0 },
        25: { x: 1, y: 0, z: 0 },
        26: { x: 0, y: 0, z: -1 },
        27: { x: 0, y: 0, z: -1 },
        28: { x: 0, y: 0, z: 1 },
        29: { x: 0, y: 0, z: 1 },
    };
    const trafficLightPositions = {
        1: { x: -38, y: 6, z: 4.7 },
        2: { x: -32.5, y: 6, z: -10 },
        3: { x: -18, y: 6, z: -5 },
        4: { x: -23.5, y: 6, z: 10 },
        5: { x: 18, y: 6, z: 4.5 },
        6: { x: 23.5, y: 6, z: -10.5 },
        7: { x: 38.2, y: 6, z: -5.3 },
        8: { x: 38.2, y: 6, z:  0.6 },
        9: { x: 32.6, y: 6, z: 10.2 }
    };

    // NEW: Junction routing map to define next lane after passing a traffic light
    // Format: [currentLane]: { nextLane, turnType }
    // turnType can be: "straight", "left", "right"
    const junctionRoutes = {
        1: [
            { nextLane: 12, turnType: "straight" },
            { nextLane: 16, turnType: "right" }
        ],
        2: [
            { nextLane: 11, turnType: "straight" }      
        ],
        3: [
            { nextLane: null, turnType: "straight" }
        ],
        4: [
            { nextLane: null, turnType: "straight" }
        ],
        5: [
            { nextLane: 16, turnType: "straight" },
            { nextLane: 4, turnType: "right" }
        ],
        6: [
            { nextLane: 15, turnType: "straight" }
        ],
        7: [
            { nextLane: null, turnType: "straight" }
        ],
        8: [
            { nextLane: null, turnType: "straight" }
        ],
        9: [
            { nextLane: 4, turnType: "straight" },
            { nextLane: 8, turnType: "right" }
        ],
        10: [
            { nextLane: 3, turnType: "straight" }
        ],
        11: [
            { nextLane: 24, turnType: "straight" }
        ],
        12: [
            { nextLane: 29, turnType: "right" },
            { nextLane: 25, turnType: "straight" }
        ],
        13: [
            { nextLane: 8, turnType: "straight" },
            { nextLane: 12, turnType: "right" }
        ],
        14: [
            { nextLane: 7, turnType: "straight" }
        ],
        15: [
            { nextLane: null, turnType: "straight" }
        ],
        16: [
            { nextLane: null, turnType: "straight" }
        ],
        17: [
            { nextLane: 29, turnType: "straight" },
            { nextLane: 9, turnType: "right" }
        ],
        18: [
            { nextLane: 28, turnType: "straight" }
        ],
        19: [
            { nextLane: null, turnType: "straight" }
        ],
        20: [
            { nextLane: null, turnType: "straight" }
        ],
        21: [
            { nextLane: 9, turnType: "straight" },
            { nextLane: 20, turnType: "right" }
        ],
        22: [
            { nextLane: 10, turnType: "straight" }
        ],
        23: [
            { nextLane: 28, turnType: "right" }
        ],
        24: [
            { nextLane: null, turnType: "straight" }
        ],
        25: [
            { nextLane: null, turnType: "straight" }
        ],
        26: [
            { nextLane: 20, turnType: "straight" },
            { nextLane: 25, turnType: "right" }
        ],
        27: [
            { nextLane: 19, turnType: "straight" }
        ],
        28: [
            { nextLane: null, turnType: "straight" }
        ],
        29: [
            { nextLane: null, turnType: "straight" }
        ]
    };

    // NEW: Define next traffic light for each current traffic light
    // This maps a car's current traffic light to the next traffic light it will encounter
    const nextTrafficLightMapping = {
        1: { nextLight: 5, transitionPoint: { x: -25, y: 2, z: 5 } },
        2: { nextLight: 4, transitionPoint: { x: -30, y: 2, z: 0 } },
        3: { nextLight: null, transitionPoint: null },
        4: { nextLight: null, transitionPoint: null },
        5: { nextLight: 9, transitionPoint: { x: 25, y: 2, z: 5 } },
        6: { nextLight: 8, transitionPoint: { x: 30, y: 2, z: -5 } },
        7: { nextLight: 3, transitionPoint: { x: 0, y: 2, z: -5 } },
        8: { nextLight: 5, transitionPoint: { x: 25, y: 2, z: 2 } },
        9: { nextLight: 6, transitionPoint: { x: 30, y: 2, z: 0 } }
    };

    // Move smoothTransitionCarLane here, before it's used in changeCarLane
    const smoothTransitionCarLane = (carMesh, newLane, duration = 1000, onComplete) => {
        const startPos = carMesh.position.clone();
        const targetPosData = laneStartPositions[newLane] || { x: 0, y: 0, z: 0 };
        const targetPos = new THREE.Vector3(targetPosData.x, targetPosData.y, targetPosData.z);
        
        let startTime = null;

        const animateTransition = (time) => {
            if (!startTime) startTime = time;
            const elapsed = time - startTime;
            const t = Math.min(elapsed / duration, 1); // normalized time [0,1]
            carMesh.position.lerpVectors(startPos, targetPos, t);
            if (t < 1) {
                requestAnimationFrame(animateTransition);
            } else {
                if (onComplete) onComplete();
            }
        };
        requestAnimationFrame(animateTransition);
    };

    // Create a car mesh; if new then add it and if it already exists, update its lane & starting position.
    const upsertCarMesh = (carId, lane, type) => {
        const scene = sceneRef.current;
        let mesh = carMeshes.current.get(carId);
        if (!mesh) {
            const geometry = new THREE.CircleGeometry(0.75, 16);
            const material = new THREE.MeshBasicMaterial({ color: carColorByType(type) });
            mesh = new THREE.Mesh(geometry, material);
            mesh.rotation.x = -Math.PI / 2;
            // Set the starting position based on the lane.
            const startPos = laneStartPositions[lane] || { x: 0, y: 0, z: 0 };
            mesh.position.set(startPos.x, startPos.y, startPos.z);
            // Initialize userData only once for new cars.
            mesh.userData = {
                carId,
                lane,
                type,
                stopped: false,
                queuePosition: 0,       
                hasCrossedLight: false,
                progressAlongLane: 0, // Add progress tracking for cars
                controllingTrafficLight: laneToTrafficLightMapping[lane], // Store current controlling light
                nextTrafficLightChecked: false, // Track if we've checked for the next light
            };
            carMeshes.current.set(carId, mesh);
            scene.add(mesh);
        } else {
            // Update properties only if they actually change.
            if (mesh.userData.lane !== lane) {
                mesh.userData.lane = lane;
                mesh.userData.hasCrossedLight = false;
                mesh.userData.progressAlongLane = 0; // Reset progress when changing lanes
                mesh.userData.controllingTrafficLight = laneToTrafficLightMapping[lane]; // Update controlling light
                mesh.userData.nextTrafficLightChecked = false; // Reset next light check
                // Optionally reposition the car using smoothTransitionCarLane or directly:
                const startPos = laneStartPositions[lane] || { x: 0, y: 0, z: 0 };
                mesh.position.set(startPos.x, startPos.y, startPos.z);
            }
            if (mesh.userData.type !== type) {
                mesh.userData.type = type;
                mesh.material.color.setHex(carColorByType(type));
            }
        }
        return mesh;
    };

    // NEW: Function to change a car's lane after passing a traffic light
    const changeCarLane = (carMesh) => {
        const currentLane = carMesh.userData.lane;
        if (!junctionRoutes[currentLane] || junctionRoutes[currentLane].length === 0) {
            return false;
        }
        const routeOption = junctionRoutes[currentLane][0];
        const newLane = routeOption.nextLane;
        
        if (!newLane) return false; // If no next lane specified, do nothing.

        console.log(`Car ${carMesh.userData.carId} changed from lane ${currentLane} to ${newLane} (${routeOption.turnType})`);

        // Update the car's lane assignment and reset flags.
        carMesh.userData.lane = newLane;
        carMesh.userData.hasCrossedLight = false; 
        carMesh.userData.queuePosition = 0;
        carMesh.userData.progressAlongLane = 0; // Reset progress
        // Update the traffic light based on the new lane mapping.
        carMesh.userData.controllingTrafficLight = laneToTrafficLightMapping[newLane];
        carMesh.userData.nextTrafficLightChecked = false; // Reset next light check
        // Mark the car as transitioning so animate loop will skip auto movement.
        carMesh.userData.isTransitioning = true;
        
        // Smoothly transition the car mesh and clear the transitioning flag on completion.
        smoothTransitionCarLane(carMesh, newLane, 1000, () => {
            carMesh.userData.isTransitioning = false;
        });

        return true;
    };

    // Calculate the progress of a car along its lane
    const calculateProgressAlongLane = (carPosition, laneNumber) => {
        const direction = laneDirections[laneNumber] || { x: 0, y: 0, z: 0 };
        const laneStart = laneStartPositions[laneNumber] || { x: 0, y: 0, z: 0 };
        return ((carPosition.x - laneStart.x) * direction.x) + ((carPosition.z - laneStart.z) * direction.z);
    };

    // NEW: Function to check if car should update its controlling traffic light
    const checkAndUpdateTrafficLight = (carMesh) => {
        // Only process cars that have already passed their initial traffic light
        if (!carMesh.userData.hasCrossedLight || carMesh.userData.nextTrafficLightChecked) {
            return;
        }

        const currentLightId = carMesh.userData.controllingTrafficLight;
        
        // If no current light or mapping exists, nothing to do
        if (!currentLightId || !nextTrafficLightMapping[currentLightId]) {
            carMesh.userData.nextTrafficLightChecked = true; // Mark as checked to avoid repeated checks
            return;
        }

        const { nextLight, transitionPoint } = nextTrafficLightMapping[currentLightId];
        
        // If no next light or transition point defined, nothing to do
        if (!nextLight || !transitionPoint) {
            carMesh.userData.nextTrafficLightChecked = true;
            return;
        }

        // Calculate distance to transition point
        const distanceToTransition = Math.sqrt(
            Math.pow(carMesh.position.x - transitionPoint.x, 2) + 
            Math.pow(carMesh.position.z - transitionPoint.z, 2)
        );

        // If car is close enough to transition point, update controlling traffic light
        if (distanceToTransition < 2) { // 2 units threshold
            console.log(`Car ${carMesh.userData.carId} changing control from light ${currentLightId} to light ${nextLight}`);
            carMesh.userData.controllingTrafficLight = nextLight;
            carMesh.userData.hasCrossedLight = false; // Reset crossed flag for new light
            carMesh.userData.nextTrafficLightChecked = true; // Mark as processed
        }
    };

    useEffect(() => {
        // Materials for the traffic lights
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

        // Animation loop: update car positions based on their current lane's direction.
        // In your animation loop, always move cars if there is no mapping.
        // Also remove cars that have exited the visible area (using a removal threshold).
        const moveSpeed = 0.1;
        const removalThreshold = 200; // Example value; adjust as needed

        const getDistanceToLight = (carPos, lightPos, direction) => {
            // For lanes moving in Z direction
            if (direction.z !== 0) {
                // If moving in positive Z (north), light is ahead when car's Z is less than light's Z
                if (direction.z > 0) {
                    return lightPos.z - carPos.z;
                }
                // If moving in negative Z (south), light is ahead when car's Z is greater than light's Z
                return carPos.z - lightPos.z;
            }
            // For lanes moving in X direction
            if (direction.x !== 0) {
                // If moving in positive X (east), light is ahead when car's X is less than light's X
                if (direction.x > 0) {
                    return lightPos.x - carPos.x;
                }
                // If moving in negative X (west), light is ahead when car's X is greater than light's X
                return carPos.x - lightPos.x;
            }
            return 0;
        };
          
        const animate = () => {
            const STOP_DISTANCE = 3; // Increased distance to stop before traffic light
            const CAR_LENGTH = 3;    // Space between cars in queue
            const CAR_MIN_DISTANCE = 2; // Minimum distance between cars to prevent overlap
            const PAST_LIGHT_DISTANCE = 1; // Distance to consider a car has passed the traffic light
            
            // First, calculate progress for all cars
            carMeshes.current.forEach((mesh) => {
                if (!mesh.userData.isTransitioning) {
                    const lane = mesh.userData.lane;
                    mesh.userData.progressAlongLane = calculateProgressAlongLane(mesh.position, lane);
                    
                    // Check if car should update its controlling traffic light
                    checkAndUpdateTrafficLight(mesh);
                }
            });
            
            // Group cars by lane for queue management
            const laneQueues = new Map();
            carMeshes.current.forEach((mesh) => {
                const lane = mesh.userData.lane;
                if (!laneQueues.has(lane)) {
                    laneQueues.set(lane, []);
                }
                laneQueues.get(lane).push(mesh);
            });
        
            // Sort cars in each queue by progress along lane (front-to-back order)
            laneQueues.forEach((queue, lane) => {
                queue.sort((a, b) => {
                    return b.userData.progressAlongLane - a.userData.progressAlongLane;
                });
                
                // Update queue positions
                queue.forEach((mesh, index) => {
                    mesh.userData.queuePosition = index;
                });
            });
        
            // Move cars
            carMeshes.current.forEach((mesh, carId) => {
                // Skip the car if it is currently transitioning between lanes.
                if (mesh.userData.isTransitioning) return;

                const { lane } = mesh.userData;
                const controllingLightId = mesh.userData.controllingTrafficLight;
                const direction = laneDirections[lane] || { x: 0, y: 0, z: 0 };
                
                let shouldMove = true;
                
                // Traffic light handling
                if (controllingLightId) {
                    const lightPos = trafficLightPositions[controllingLightId];
                    if (lightPos) { // Only process if we have position data for this light
                        const distanceToLight = getDistanceToLight(
                            mesh.position,
                            lightPos,
                            direction
                        );
            
                        const stopDistance = STOP_DISTANCE + (mesh.userData.queuePosition * CAR_LENGTH);
                        
                        // Check if car has passed the current traffic light
                        if (!mesh.userData.hasCrossedLight && distanceToLight <= PAST_LIGHT_DISTANCE) {
                            if (trafficLightStatuses.current[controllingLightId]) {
                                mesh.userData.hasCrossedLight = true;
                                console.log(`Car ${carId} passed traffic light ${controllingLightId}`);
                            }
                        }
            
                        // Handle lane change after passing traffic light
                        if (mesh.userData.hasCrossedLight && distanceToLight < -PAST_LIGHT_DISTANCE && trafficLightStatuses.current[controllingLightId]) {
                            // Only change lanes if this is a lane connected to the initial traffic light
                            // (not a secondary traffic light assignment from the next traffic light mapping)
                            if (laneToTrafficLightMapping[lane] === controllingLightId) {
                                changeCarLane(mesh);
                            }
                        }
            
                        // Determine if car should stop at red light
                        if (distanceToLight <= stopDistance) {
                            const isRedLight = !trafficLightStatuses.current[controllingLightId];
                            
                            if (isRedLight && !mesh.userData.hasCrossedLight) {
                                shouldMove = false;
                                mesh.userData.stopped = true;
                            }
                        }
                    }
                }
                
                // Check for cars in front to prevent collisions
                if (shouldMove && mesh.userData.queuePosition > 0) {
                    const laneQueue = laneQueues.get(lane);
                    const carInFront = laneQueue[mesh.userData.queuePosition - 1];
                    if (carInFront) {
                        const distanceToCar = mesh.position.distanceTo(carInFront.position);
                        if (distanceToCar < CAR_MIN_DISTANCE) {
                            shouldMove = false;
                            mesh.userData.stopped = true;
                        }
                    }
                }
                
                if (shouldMove) {
                    mesh.userData.stopped = false;
                    mesh.position.x += direction.x * moveSpeed;
                    mesh.position.y += direction.y * moveSpeed;
                    mesh.position.z += direction.z * moveSpeed;
                    
                    // Update progress after moving
                    mesh.userData.progressAlongLane = calculateProgressAlongLane(mesh.position, lane);
                }
                
                // Remove cars that are far from the camera
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