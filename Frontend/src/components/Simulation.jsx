import { useEffect, useRef } from "react";
import './Simulation.css';
import * as THREE from "three";
import { GLTFLoader } from "three/examples/jsm/loaders/GLTFLoader";

function Simulation() {
  const canvasRef = useRef(null);
  const sceneRef = useRef(new THREE.Scene());
  const carMeshes = useRef(new Map());
  const trafficLights = useRef({});
  const trafficLightStatuses = useRef({});

  let socketCars = null;
  let socketTraffic = null;
  let camera, renderer;

  const CANVAS_WIDTH_EM = 70;
  const CANVAS_HEIGHT_EM = 33.33;

  const carColorByType = (type) => {
    // return type === "POLICE" || type === "AMBULANCE" ? 0xff0000 : 0xffffff;
    if(type === "POLICE") return 0x0000ff; // Blue for police
    if(type === "AMBULANCE") return 0xff0000; // Red for ambulance
    if(type === "PRIVATE") return 0xffa500; // Orange for private
    if(type === "MOTORCYCLE") return 0x00ff00; // Green for motorcycle
  };

  // Mapping from lane to controlling traffic light (null = exiting lane)
  const laneToTrafficLightMapping = {
    1: 1, 2: 1, 3: null, 4: null, 5: 2, 6: 2, 7: null, 8: null,
    9: 3, 10: 3, 11: 5, 12: 5, 13: 4, 14: 4, 15: null, 16: null,
    17: 6, 18: 6, 19: null, 20: null, 21: 7, 22: 7, 23: 8, 24: null,
    25: null, 26: 9, 27: 9, 28: null, 29: null
  };

  const laneStartPositions = {
    1: { x: -90, y: 2, z: 7.3 },  2: { x: -90, y: 2, z: 2.4 },
    3: { x: -37, y: 2, z: -2.5 }, 4: { x: -37, y: 2, z: -7.4 },
    5: { x: -35, y: 2, z: -41.5 },6: { x: -30, y: 2, z: -41.5 },
    7: { x: -26, y: 2, z: -9 },   8: { x: -21, y: 2, z: -9 },
    9: { x: 19, y: 2, z: -7.4 },  10: { x: 19, y: 2, z: -2.5 },
    11: { x: -19, y: 2, z: 2.4 }, 12: { x: -19, y: 2, z: 7.3 },
    13: { x: -21, y: 2, z: 41 },  14: { x: -26, y: 2, z: 41 },
    15: { x: -30, y: 2, z: 9 },   16: { x: -35, y: 2, z: 9 },
    17: { x: 21, y: 2, z: -41 },  18: { x: 26, y: 2, z: -41 },
    19: { x: 30, y: 2, z: -9 },   20: { x: 35, y: 2, z: -9 },
    21: { x: 90, y: 2, z: -7.4 }, 22: { x: 90, y: 2, z: -3 },
    23: { x: 90, y: 2, z: 0.7 },  24: { x: 37, y: 2, z: 4.2 },
    25: { x: 37, y: 2, z: 7.6 },  26: { x: 35, y: 2, z: 41 },
    27: { x: 30, y: 2, z: 41 },   28: { x: 25.7, y: 2, z: 9 },
    29: { x: 21, y: 2, z: 11 }
  };

  const laneDirections = {
    1: { x: 1, y: 0, z: 0 },  2: { x: 1, y: 0, z: 0 },  3: { x: -1, y: 0, z: 0 },
    4: { x: -1, y: 0, z: 0 }, 5: { x: 0, y: 0, z: 1 },  6: { x: 0, y: 0, z: 1 },
    7: { x: 0, y: 0, z: -1 }, 8: { x: 0, y: 0, z: -1 }, 9: { x: -1, y: 0, z: 0 },
    10: { x: -1, y: 0, z: 0 },11: { x: 1, y: 0, z: 0 }, 12: { x: 1, y: 0, z: 0 },
    13: { x: 0, y: 0, z: -1 },14: { x: 0, y: 0, z: -1 },15: { x: 0, y: 0, z: 1 },
    16: { x: 0, y: 0, z: 1 }, 17: { x: 0, y: 0, z: 1 }, 18: { x: 0, y: 0, z: 1 },
    19: { x: 0, y: 0, z: -1 },20: { x: 0, y: 0, z: -1 },21: { x: -1, y: 0, z: 0 },
    22: { x: -1, y: 0, z: 0 },23: { x: -1, y: 0, z: 0 },24: { x: 1, y: 0, z: 0 },
    25: { x: 1, y: 0, z: 0 }, 26: { x: 0, y: 0, z: -1 },27: { x: 0, y: 0, z: -1 },
    28: { x: 0, y: 0, z: 1 }, 29: { x: 0, y: 0, z: 1 }
  };

  // Original traffic light positions
  const trafficLightPositions = {
    1: { x: -38, y: 6, z: 4.7 }, 2: { x: -32.5, y: 6, z: -10 },
    3: { x: -18, y: 6, z: -5 },  4: { x: -23.5, y: 6, z: 10 },
    5: { x: 18, y: 6, z: 4.5 },  6: { x: 23.5, y: 6, z: -10.5 },
    7: { x: 38.2, y: 6, z: -5.3 },8: { x: 38.2, y: 6, z: 0.6 },
    9: { x: 32.6, y: 6, z: 10.2 }
  };

  // *** FIX: Define stop line positions for each controlled lane ***
  const laneStopPositions = {
    1: { x: trafficLightPositions[1].x, y: 2, z: laneStartPositions[1].z },
    2: { x: trafficLightPositions[1].x, y: 2, z: laneStartPositions[2].z },
    5: { x: laneStartPositions[5].x, y: 2, z: trafficLightPositions[2].z },
    6: { x: laneStartPositions[6].x, y: 2, z: trafficLightPositions[2].z },
    9: { x: trafficLightPositions[3].x, y: 2, z: laneStartPositions[9].z },
    10:{ x: trafficLightPositions[3].x, y: 2, z: laneStartPositions[10].z },
    11:{ x: trafficLightPositions[5].x, y: 2, z: laneStartPositions[11].z },
    12:{ x: trafficLightPositions[5].x, y: 2, z: laneStartPositions[12].z },
    13:{ x: laneStartPositions[13].x, y: 2, z: trafficLightPositions[4].z },
    14:{ x: laneStartPositions[14].x, y: 2, z: trafficLightPositions[4].z },
    17:{ x: laneStartPositions[17].x, y: 2, z: trafficLightPositions[6].z },
    18:{ x: laneStartPositions[18].x, y: 2, z: trafficLightPositions[6].z },
    21:{ x: trafficLightPositions[7].x, y: 2, z: laneStartPositions[21].z },
    22:{ x: trafficLightPositions[7].x, y: 2, z: laneStartPositions[22].z },
    23:{ x: trafficLightPositions[8].x, y: 2, z: laneStartPositions[23].z },
    26:{ x: laneStartPositions[26].x, y: 2, z: trafficLightPositions[9].z },
    27:{ x: laneStartPositions[27].x, y: 2, z: trafficLightPositions[9].z }
  };

  // Junction routing map
  const junctionRoutes = {
    1: [{ nextLane: 12, turnType: "straight" }, { nextLane: 16, turnType: "right" }],
    2: [{ nextLane: 11, turnType: "straight" }], 3: [{ nextLane: null, turnType: "straight" }],
    4: [{ nextLane: null, turnType: "straight" }], 5: [{ nextLane: 16, turnType: "straight" }, { nextLane: 4, turnType: "right" }],
    6: [{ nextLane: 15, turnType: "straight" }], 7: [{ nextLane: null, turnType: "straight" }],
    8: [{ nextLane: null, turnType: "straight" }], 9: [{ nextLane: 4, turnType: "straight" }, { nextLane: 8, turnType: "right" }],
    10: [{ nextLane: 3, turnType: "straight" }], 11: [{ nextLane: 24, turnType: "straight" }],
    12: [{ nextLane: 29, turnType: "right" }, { nextLane: 25, turnType: "straight" }],
    13: [{ nextLane: 8, turnType: "straight" }, { nextLane: 12, turnType: "right" }],
    14: [{ nextLane: 7, turnType: "straight" }], 15: [{ nextLane: null, turnType: "straight" }],
    16: [{ nextLane: null, turnType: "straight" }], 17: [{ nextLane: 29, turnType: "straight" }, { nextLane: 9, turnType: "right" }],
    18: [{ nextLane: 28, turnType: "straight" }], 19: [{ nextLane: null, turnType: "straight" }],
    20: [{ nextLane: null, turnType: "straight" }], 21: [{ nextLane: 9, turnType: "straight" }, { nextLane: 20, turnType: "right" }],
    22: [{ nextLane: 10, turnType: "straight" }], 23: [{ nextLane: 28, turnType: "right" }],
    24: [{ nextLane: null, turnType: "straight" }], 25: [{ nextLane: null, turnType: "straight" }],
    26: [{ nextLane: 20, turnType: "straight" }, { nextLane: 25, turnType: "right" }],
    27: [{ nextLane: 19, turnType: "straight" }], 28: [{ nextLane: null, turnType: "straight" }],
    29: [{ nextLane: null, turnType: "straight" }]
  };

  // Next traffic light mapping.
  const nextTrafficLightMapping = {
    1: [{ nextLight: 5, transitionPoint: { x: -25, y: 2, z: 5 } }, null], 2: null, 3: null,
    4: [{ nextLight: 5, transitionPoint: { x: -25, y: 2, z: 5 } }, null], 5: null,
    6: [{ nextLight: 3, transitionPoint: { x: 0, y: 2, z: -5 } }, null],
    7: [{ nextLight: 3, transitionPoint: { x: 0, y: 2, z: -5 } }, null], 8: null, 9: null
  };

  // Smoothly transition a car mesh between lanes. (Reverted: Removed Y rotation)
  const smoothTransitionCarLane = (carMesh, newLane, duration = 1000, onComplete) => {
    const startPos = carMesh.position.clone();
    const targetPosData = laneStartPositions[newLane] || { x: startPos.x, y: startPos.y, z: startPos.z };
    const targetPos = new THREE.Vector3(targetPosData.x, targetPosData.y, targetPosData.z);
    let startTime = null;
    const animateTransition = (time) => {
      if (!startTime) startTime = time;
      const elapsed = time - startTime;
      const t = Math.min(elapsed / duration, 1);
      carMesh.position.lerpVectors(startPos, targetPos, t);
      // No rotation needed for circle geometry during transition

      if (t < 1) {
        requestAnimationFrame(animateTransition);
      } else {
        carMesh.position.copy(targetPos);
        // No final rotation needed for circle geometry
        if (onComplete) onComplete();
      }
    };
    requestAnimationFrame(animateTransition);
  };

  // Create or update a car mesh. (Reverted: Back to CircleGeometry and MeshBasicMaterial)
  const upsertCarMesh = (carId, lane, type) => {
    const scene = sceneRef.current;
    let mesh = carMeshes.current.get(carId);
    if (!mesh) {
        // *** Reverted: Use original CircleGeometry ***
        const geometry = new THREE.CircleGeometry(0.75, 16);
        // *** Reverted: Use original MeshBasicMaterial (unaffected by scene lighting) ***
        const material = new THREE.MeshBasicMaterial({ color: carColorByType(type) });
        mesh = new THREE.Mesh(geometry, material);
        // *** Reverted: Set rotation for circle to lie flat ***
        mesh.rotation.x = -Math.PI / 2;
        // *** Removed: Y rotation based on direction (not needed for circle) ***

        const startPos = laneStartPositions[lane] || { x: 0, y: 0, z: 0 };
        mesh.position.set(startPos.x, startPos.y, startPos.z);

        mesh.userData = {
            carId, lane, type, stopped: false, queuePosition: 0,
            hasCrossedLight: false, progressAlongLane: 0,
            controllingTrafficLight: laneToTrafficLightMapping[lane],
            nextTrafficLightChecked: false, isTransitioning: false
        };
        carMeshes.current.set(carId, mesh);
        scene.add(mesh);
    } else {
        // Update lane only if different and not currently transitioning
        if (mesh.userData.lane !== lane && !mesh.userData.isTransitioning) {
            mesh.userData.lane = lane;
            mesh.userData.hasCrossedLight = false;
            mesh.userData.progressAlongLane = 0;
            mesh.userData.controllingTrafficLight = laneToTrafficLightMapping[lane];
            mesh.userData.nextTrafficLightChecked = false;
            const startPos = laneStartPositions[lane] || { x: 0, y: 0, z: 0 };
            mesh.position.set(startPos.x, startPos.y, startPos.z);
            // *** Removed: Y rotation update (not needed for circle) ***
        }
        // Update type/color if different
        if (mesh.userData.type !== type) {
            mesh.userData.type = type;
            mesh.material.color.setHex(carColorByType(type));
        }
    }
    return mesh;
  };

  // Change a car's lane after passing a traffic light.
  const changeCarLane = (carMesh, routeOption) => {
      const currentLane = carMesh.userData.lane;
      const newLane = routeOption.nextLane;

      if (!newLane) {
          console.log(`Car ${carMesh.userData.carId} exiting from lane ${currentLane}`);
          sceneRef.current.remove(carMesh);
          carMeshes.current.delete(carMesh.userData.carId);
          return false;
      }

      console.log(`Car ${carMesh.userData.carId} changing from lane ${currentLane} to ${newLane} (${routeOption.turnType})`);

      carMesh.userData.lane = newLane;
      carMesh.userData.hasCrossedLight = false;
      carMesh.userData.queuePosition = 0;
      carMesh.userData.progressAlongLane = 0;
      carMesh.userData.controllingTrafficLight = laneToTrafficLightMapping[newLane];
      carMesh.userData.nextTrafficLightChecked = false;
      carMesh.userData.isTransitioning = true;

      smoothTransitionCarLane(carMesh, newLane, 1000, () => {
          carMesh.userData.isTransitioning = false;
          carMesh.userData.progressAlongLane = calculateProgressAlongLane(carMesh.position, newLane);
      });

      return true;
  };


  const calculateProgressAlongLane = (carPosition, laneNumber) => {
    const direction = laneDirections[laneNumber] || { x: 0, y: 0, z: 0 };
    const laneStart = laneStartPositions[laneNumber] || { x: 0, y: 0, z: 0 };
    const carVec = new THREE.Vector3(carPosition.x - laneStart.x, 0, carPosition.z - laneStart.z);
    const dirVec = new THREE.Vector3(direction.x, 0, direction.z).normalize();
    // Ensure dirVec has non-zero length before dot product if direction could be {0,0,0}
    if (dirVec.lengthSq() === 0) return 0;
    return carVec.dot(dirVec);
  };

  // Check if a car should update its controlling traffic light.
  const checkAndUpdateTrafficLight = (carMesh) => {
    if (!carMesh.userData.hasCrossedLight || carMesh.userData.nextTrafficLightChecked) {
        return;
    }

    const currentLightId = carMesh.userData.controllingTrafficLight;
    if (!currentLightId) {
        carMesh.userData.nextTrafficLightChecked = true;
        return;
    }

    let mappingArray = nextTrafficLightMapping[currentLightId];

    if (!mappingArray || !Array.isArray(mappingArray)) {
        carMesh.userData.nextTrafficLightChecked = true;
        return;
    }

    const validOptions = mappingArray.filter(o => o !== null && o.nextLight && o.transitionPoint);

    let optionToUse = null;

    if (validOptions.length === 0) {
        carMesh.userData.nextTrafficLightChecked = true;
        return;
    } else if (validOptions.length === 1) {
        optionToUse = validOptions[0];
    } else {
        // Keep previous logic: Warn and don't auto-select if multiple options
        console.warn(`Car ${carMesh.userData.carId} at light ${currentLightId} has multiple next light options. Cannot auto-select.`);
        carMesh.userData.nextTrafficLightChecked = true;
        return;
    }

    const { nextLight, transitionPoint } = optionToUse;
    const carPos = carMesh.position;
    const transPoint = new THREE.Vector3(transitionPoint.x, transitionPoint.y, transitionPoint.z);
    const distanceToTransition = carPos.distanceTo(transPoint);
    const TRANSITION_THRESHOLD = 3; // Keep threshold reasonable

    if (distanceToTransition < TRANSITION_THRESHOLD) {
        console.log(`Car ${carMesh.userData.carId} reached transition zone. Changing control from light ${currentLightId} to light ${nextLight}`);
        carMesh.userData.controllingTrafficLight = nextLight;
        carMesh.userData.hasCrossedLight = false; // Reset flag for the new controlling light
        carMesh.userData.nextTrafficLightChecked = true; // Mark as checked for this cycle
    }
  };

  useEffect(() => {
    // *** Reverted: Use MeshStandardMaterial but keep original emissive intensity ***
    const greenMaterial = new THREE.MeshStandardMaterial({
      color: 0x00ff00, emissive: 0x00ff00, emissiveIntensity: 0.5
    });
    const redMaterial = new THREE.MeshStandardMaterial({
      color: 0xff0000, emissive: 0xff0000, emissiveIntensity: 0.5
    });
    // Keep updateTrafficLight function as is (it handles applying materials)
    const updateTrafficLight = (lightId, status) => {
      const lightObject = trafficLights.current[`trafficLight${lightId}`];
      const isGreen = status === "GREEN";
      trafficLightStatuses.current[lightId] = isGreen;
      if (lightObject) {
        let lightMesh = lightObject;
        // Traverse logic might be needed if GLB structure has separate bulb meshes
        lightObject.traverse((child) => {
          if (child.isMesh && child.name.toLowerCase().includes('bulb')) { // Example check
             lightMesh = child;
          }
        });
        // Apply material to the found mesh (or the main object if no specific bulb found)
         lightMesh.material = isGreen ? greenMaterial.clone() : redMaterial.clone();
      } else {
         console.log(`Traffic light mesh ${lightId} not found in scene.`);
      }
    };

    document.documentElement.style.setProperty('--canvas-width', `${CANVAS_WIDTH_EM}em`);
    document.documentElement.style.setProperty('--canvas-height', `${CANVAS_HEIGHT_EM}em`);
    const scene = sceneRef.current;
    // *** Reverted: Set background back to white ***
    scene.background = new THREE.Color('white');
    const aspectRatio = CANVAS_WIDTH_EM / CANVAS_HEIGHT_EM;
    camera = new THREE.PerspectiveCamera(50, aspectRatio, 0.1, 1000);
    // *** Reverted: Set camera position back to original ***
    camera.position.set(0, 100, 0);
    camera.lookAt(0, 0, 0);

    const gltfLoader = new GLTFLoader();
    gltfLoader.load('/junction.glb', (gltfScene) => {
      const loadedModel = gltfScene.scene;
      scene.add(loadedModel);
      loadedModel.traverse((object) => {
        if (object.isMesh) {
           object.material.needsUpdate = true;
           // *** Reverted: Don't force shadow casting/receiving on scene model ***
           // object.castShadow = true;
           // object.receiveShadow = true;
        }
        // Find traffic lights and initialize them visually
        for (let i = 1; i <= 9; i++) {
            if (object.name === `trafficLight${i}`) {
                console.log(`Found traffic light: ${object.name}`);
                trafficLights.current[object.name] = object;
                 // Initialize visually to red using the updated materials
                 updateTrafficLight(i, "RED");
            }
         }
      });
       // Ensure all lights have an initial status and visual state
       for (let i = 1; i <= 9; i++) {
         if (trafficLightStatuses.current[i] === undefined) { // Check if undefined
            updateTrafficLight(i, "RED"); // Default to red
         }
       }
    }, undefined, (error) => {
      console.error('Error loading GLB model:', error);
    });


    renderer = new THREE.WebGLRenderer({ canvas: canvasRef.current, antialias: true });
    // *** Reverted: Set clear color back to white ***
    renderer.setClearColor('white', 1);
    // *** FIX: Use outputColorSpace and SRGBColorSpace (NECESSARY API UPDATE) ***
    renderer.outputColorSpace = THREE.SRGBColorSpace;
    // *** Reverted: Disable shadow map if not needed ***
    renderer.shadowMap.enabled = false; // Or true if you intend to add shadows later

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

    // *** Reverted: Restore original lighting setup ***
    // *** Removed Ambient Light ***
    const directionalLight = new THREE.DirectionalLight(0x1c1c1c, 50);
    directionalLight.position.set(0, 10, 0);
    // *** Removed shadow casting from light ***
    // directionalLight.castShadow = true;
    scene.add(directionalLight);


    // *** FIX: Use updated getDistanceToPoint function for stopping logic ***
    const getDistanceToPoint = (carPos, targetPos, direction) => {
        if (!direction || !targetPos) return Infinity;

        if (Math.abs(direction.x) > Math.abs(direction.z)) { // Primarily X-axis movement
            return direction.x > 0 ? (targetPos.x - carPos.x) : (carPos.x - targetPos.x);
        } else { // Primarily Z-axis movement
            return direction.z > 0 ? (targetPos.z - carPos.z) : (carPos.z - targetPos.z);
        }
    };

    const animate = () => {
      requestAnimationFrame(animate); // Request next frame first

      // *** Reverted: Use original constants where appropriate ***
      const CAR_MOVE_SPEED = 0.1; // Reverted from 0.15 if needed, or keep faster speed
      const STOP_DISTANCE = 3; // Original base stop distance
      const CAR_LENGTH = 3; // Original effective length
      const MIN_FOLLOW_DISTANCE = 2; // Original minimum gap
      const LIGHT_CROSS_THRESHOLD = 1; // Original crossing threshold (PAST_LIGHT_DISTANCE)
      const REMOVAL_DISTANCE = 200; // Original removal distance

      // 1. Update car progress and check for transitions/light updates
      carMeshes.current.forEach((mesh) => {
        if (!mesh.userData.isTransitioning) {
          const lane = mesh.userData.lane;
          // Ensure progress calculation happens even if stopped
          mesh.userData.progressAlongLane = calculateProgressAlongLane(mesh.position, lane);
          checkAndUpdateTrafficLight(mesh);
        }
      });

      // 2. Build lane queues and sort by progress (descending)
      const laneQueues = new Map();
      carMeshes.current.forEach((mesh) => {
        if (!mesh.userData.isTransitioning) {
            const lane = mesh.userData.lane;
            if (!laneQueues.has(lane)) {
                laneQueues.set(lane, []);
            }
            laneQueues.get(lane).push(mesh);
        }
      });

      laneQueues.forEach((queue) => {
          queue.sort((a, b) => b.userData.progressAlongLane - a.userData.progressAlongLane);
          queue.forEach((mesh, index) => {
              mesh.userData.queuePosition = index;
          });
      });

      // 3. Determine movement for each car
      carMeshes.current.forEach((mesh, carId) => {
        if (mesh.userData.isTransitioning) return; // Skip movement logic during transitions

        const { lane, queuePosition } = mesh.userData;
        const controllingLightId = mesh.userData.controllingTrafficLight;
        const direction = laneDirections[lane];
        // *** FIX: Use laneStopPositions for accurate stopping point ***
        const stopPos = laneStopPositions[lane];

        let shouldMove = true;
        let reasonStopped = ""; // For debugging

        // Check traffic light (using lane-specific stop position)
        if (controllingLightId && stopPos && !mesh.userData.hasCrossedLight) {
            // *** FIX: Calculate distance to the specific stop line ***
            const distanceToStopLine = getDistanceToPoint(mesh.position, stopPos, direction);
            const isRedLight = !trafficLightStatuses.current[controllingLightId];
            // *** Use original dynamic stop distance calculation ***
            const dynamicStopDistance = STOP_DISTANCE + (queuePosition * CAR_LENGTH);

            // Check if car is at or before the calculated stop distance for its queue position
            if (distanceToStopLine <= dynamicStopDistance) {
                if (isRedLight) {
                    shouldMove = false;
                    reasonStopped = `Red Light ${controllingLightId}`;
                }
            }

            // Check if car has just crossed the line threshold
             if (distanceToStopLine <= LIGHT_CROSS_THRESHOLD) {
                 if (!mesh.userData.hasCrossedLight) {
                     mesh.userData.hasCrossedLight = true;
                    // console.log(`Car ${carId} crossed stop line for light ${controllingLightId}`);

                    const wasGreen = !isRedLight;
                    if (wasGreen) {
                        // Attempt lane change only immediately after crossing a green light
                        const routeOptions = junctionRoutes[lane];
                        if (routeOptions && routeOptions.length > 0) {
                            // Using simplified logic: take first route if available
                            let routeToTake = routeOptions[0];
                            // Add logic here if you need to select between multiple routes based on type etc.
                            // e.g., find route where turnType matches car's intent (if available)
                            if (!changeCarLane(mesh, routeToTake)) {
                                 return; // Car exited, stop processing
                            }
                        }
                     }
                 }
            }
        }

        // Check car in front (using original spacing logic)
        if (shouldMove && queuePosition > 0) {
            const laneQueue = laneQueues.get(lane);
            const carInFront = laneQueue ? laneQueue[queuePosition - 1] : null;
            if (carInFront) {
                // Use direct distance for simplicity, adjust if needed
                const distanceToCar = mesh.position.distanceTo(carInFront.position);
                // Original check used MIN_FOLLOW_DISTANCE, ensure constants match intent
                if (distanceToCar < MIN_FOLLOW_DISTANCE) {
                    shouldMove = false;
                    reasonStopped = `Car Ahead (${carInFront.userData.carId})`;
                }
            }
        }

        // 4. Apply movement or stop
        if (shouldMove) {
            if (mesh.userData.stopped) {
              mesh.userData.stopped = false;
            }
            mesh.position.x += direction.x * CAR_MOVE_SPEED;
            mesh.position.y += direction.y * CAR_MOVE_SPEED; // Should be 0
            mesh.position.z += direction.z * CAR_MOVE_SPEED;
            // Progress updated at start of loop now
            // mesh.userData.progressAlongLane = calculateProgressAlongLane(mesh.position, lane); // Redundant here
        } else {
           // Mark as stopped if not already
           if (!mesh.userData.stopped) {
             // console.log(`Car ${carId} stopped. Reason: ${reasonStopped}`);
           }
           mesh.userData.stopped = true;
        }

        // 5. Remove cars far away (using original threshold)
        // Check distance from origin (simple) or distance along path if needed
        if (mesh.position.length() > REMOVAL_DISTANCE) {
            console.log(`Removing car ${carId} (too far)`);
            scene.remove(mesh);
            carMeshes.current.delete(carId);
        }
      });

      // 6. Render the scene
      renderer.render(scene, camera);
    };

    animate(); // Start animation loop

    // WebSocket connections (no changes needed here)
    socketTraffic = new WebSocket("ws://localhost:8080/traffic");
    socketTraffic.onopen = () => console.log("Traffic WebSocket Connected");
    socketTraffic.onerror = (error) => console.error("Traffic WebSocket Error:", error);
    socketTraffic.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        if (data.lightId && data.status) {
            updateTrafficLight(data.lightId, data.status);
        } else {
            console.warn("Invalid traffic data received:", data);
        }
      } catch (e) {
          console.error("Failed to parse traffic message:", event.data, e);
      }
    };
    socketTraffic.onclose = () => console.log("Traffic WebSocket Disconnected");


    socketCars = new WebSocket("ws://localhost:8080/cars");
    socketCars.onopen = () => console.log("Cars WebSocket Connected");
    socketCars.onerror = (error) => console.error("Cars WebSocket Error:", error);
    socketCars.onmessage = (event) => {
        try {
            const data = JSON.parse(event.data);
             if (data.carId !== undefined && data.lane !== undefined && data.type !== undefined) {
                upsertCarMesh(data.carId, data.lane, data.type);
            } else {
                 console.warn("Invalid car data received:", data);
            }
        } catch (e) {
             console.error("Failed to parse car message:", event.data, e);
        }
    };
    socketCars.onclose = () => console.log("Cars WebSocket Disconnected");

    // Cleanup function
    return () => {
        console.log("Closing WebSockets and removing resize listener.");
        socketCars?.close();
        socketTraffic?.close();
        window.removeEventListener("resize", updateSize);
    };
  }, []); // Empty dependency array ensures this runs only once on mount

  return <canvas ref={canvasRef} className="ThreeJS"></canvas>;
}

export default Simulation;