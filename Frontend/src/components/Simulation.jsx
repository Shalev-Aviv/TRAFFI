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
    return type === "POLICE" || type === "AMBULANCE" ? 0x00ff00 : 0xff0000;
  };

  // Mapping from lane to controlling traffic light (null = exiting lane)
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

  const laneStartPositions = {
    1: { x: -90, y: 2, z: 7.3 },
    2: { x: -90, y: 2, z: 2.4 },
    3: { x: -37, y: 2, z: -2.5 },
    4: { x: -37, y: 2, z: -7.4 },
    5: { x: -35, y: 2, z: -41.5 }, 
    6: { x: -30, y: 2, z: -41.5 },
    7: { x: -26, y: 2, z: -9 }, 
    8: { x: -21, y: 2, z: -9 },
    9: { x: 19, y: 2, z: -7.4 }, 
    10: { x: 19, y: 2, z: -2.5 },
    11: { x: -19, y: 2, z: 2.4 },
    12: { x: -19, y: 2, z: 7.3 },
    13: { x: -21, y: 2, z: 41 },
    14: { x: -26, y: 2, z: 41 },
    15: { x: -30, y: 2, z: 9 },
    16: { x: -35, y: 2, z: 9 },
    17: { x: 21, y: 2, z: -41 },
    18: { x: 26, y: 2, z: -41 },
    19: { x: 30, y: 2, z: -9 },
    20: { x: 35, y: 2, z: -9 },
    21: { x: 90, y: 2, z: -7.4 },
    22: { x: 90, y: 2, z: -3 },
    23: { x: 90, y: 2, z: 0.7 },
    24: { x: 37, y: 2, z: 4.2 },
    25: { x: 37, y: 2, z: 7.6 },
    26: { x: 35, y: 2, z: 41 },
    27: { x: 30, y: 2, z: 41 },
    28: { x: 25.7, y: 2, z: 9 },
    29: { x: 21, y: 2, z: 11 }
  };

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
    29: { x: 0, y: 0, z: 1 }
  };

  const trafficLightPositions = {
    1: { x: -38, y: 6, z: 4.7 },
    2: { x: -32.5, y: 6, z: -10 },
    3: { x: -18, y: 6, z: -5 },
    4: { x: -23.5, y: 6, z: 10 },
    5: { x: 18, y: 6, z: 4.5 },
    6: { x: 23.5, y: 6, z: -10.5 },
    7: { x: 38.2, y: 6, z: -5.3 },
    8: { x: 38.2, y: 6, z: 0.6 },
    9: { x: 32.6, y: 6, z: 10.2 }
  };

  // Junction routing map for lane changes after passing a traffic light
  // For each current lane, the possible next lanes are defined.
  const junctionRoutes = {
    1: [{ nextLane: 12, turnType: "straight" }, { nextLane: 16, turnType: "right" }],
    2: [{ nextLane: 11, turnType: "straight" }],
    3: [{ nextLane: null, turnType: "straight" }],
    4: [{ nextLane: null, turnType: "straight" }],
    5: [{ nextLane: 16, turnType: "straight" }, { nextLane: 4, turnType: "right" }],
    6: [{ nextLane: 15, turnType: "straight" }],
    7: [{ nextLane: null, turnType: "straight" }],
    8: [{ nextLane: null, turnType: "straight" }],
    9: [{ nextLane: 4, turnType: "straight" }, { nextLane: 8, turnType: "right" }],
    10: [{ nextLane: 3, turnType: "straight" }],
    11: [{ nextLane: 24, turnType: "straight" }],
    12: [{ nextLane: 29, turnType: "right" }, { nextLane: 25, turnType: "straight" }],
    13: [{ nextLane: 8, turnType: "straight" }, { nextLane: 12, turnType: "right" }],
    14: [{ nextLane: 7, turnType: "straight" }],
    15: [{ nextLane: null, turnType: "straight" }],
    16: [{ nextLane: null, turnType: "straight" }],
    17: [{ nextLane: 29, turnType: "straight" }, { nextLane: 9, turnType: "right" }],
    18: [{ nextLane: 28, turnType: "straight" }],
    19: [{ nextLane: null, turnType: "straight" }],
    20: [{ nextLane: null, turnType: "straight" }],
    21: [{ nextLane: 9, turnType: "straight" }, { nextLane: 20, turnType: "right" }],
    22: [{ nextLane: 10, turnType: "straight" }],
    23: [{ nextLane: 28, turnType: "right" }],
    24: [{ nextLane: null, turnType: "straight" }],
    25: [{ nextLane: null, turnType: "straight" }],
    26: [{ nextLane: 20, turnType: "straight" }, { nextLane: 25, turnType: "right" }],
    27: [{ nextLane: 19, turnType: "straight" }],
    28: [{ nextLane: null, turnType: "straight" }],
    29: [{ nextLane: null, turnType: "straight" }]
  };

  // Next traffic light mapping.
  // If multiple options exist, the backend should supply the desired next lane.
  const nextTrafficLightMapping = {
    1: [{ nextLight: 5, transitionPoint: { x: -25, y: 2, z: 5 } }, null],
    2: null,
    3: null,
    4: [{ nextLight: 5, transitionPoint: { x: -25, y: 2, z: 5 } }, null],
    5: null,
    6: [{ nextLight: 3, transitionPoint: { x: 0, y: 2, z: -5 } }, null],
    7: [{ nextLight: 3, transitionPoint: { x: 0, y: 2, z: -5 } }, null],
    8: null,
    9: null
  };

  // Smoothly transition a car mesh between lanes.
  const smoothTransitionCarLane = (carMesh, newLane, duration = 500, onComplete) => {
    const startPos = carMesh.position.clone();
    const targetPosData = laneStartPositions[newLane] || { x: 0, y: 0, z: 0 };
    const targetPos = new THREE.Vector3(targetPosData.x, targetPosData.y, targetPosData.z);
    let startTime = null;
    const animateTransition = (time) => {
      if (!startTime) startTime = time;
      const elapsed = time - startTime;
      const t = Math.min(elapsed / duration, 1);
      carMesh.position.lerpVectors(startPos, targetPos, t);
      if (t < 1) {
        requestAnimationFrame(animateTransition);
      } else {
        if (onComplete) onComplete();
      }
    };
    requestAnimationFrame(animateTransition);
  };

  // Create or update a car mesh.
  const upsertCarMesh = (carId, lane, type) => {
    const scene = sceneRef.current;
    let mesh = carMeshes.current.get(carId);
    if (!mesh) {
      const geometry = new THREE.CircleGeometry(0.75, 16);
      const material = new THREE.MeshBasicMaterial({ color: carColorByType(type) });
      mesh = new THREE.Mesh(geometry, material);
      mesh.rotation.x = -Math.PI / 2;
      const startPos = laneStartPositions[lane] || { x: 0, y: 0, z: 0 };
      mesh.position.set(startPos.x, startPos.y, startPos.z);
      mesh.userData = {
        carId,
        lane,
        type,
        stopped: false,
        queuePosition: 0,
        hasCrossedLight: false,
        progressAlongLane: 0,
        controllingTrafficLight: laneToTrafficLightMapping[lane],
        nextTrafficLightChecked: false
      };
      carMeshes.current.set(carId, mesh);
      scene.add(mesh);
    } else {
      if (mesh.userData.lane !== lane) {
        mesh.userData.lane = lane;
        mesh.userData.hasCrossedLight = false;
        mesh.userData.progressAlongLane = 0;
        mesh.userData.controllingTrafficLight = laneToTrafficLightMapping[lane];
        mesh.userData.nextTrafficLightChecked = false;
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

  // Change a car's lane after passing a traffic light.
  const changeCarLane = (carMesh) => {
    const currentLane = carMesh.userData.lane;
    if (!junctionRoutes[currentLane] || junctionRoutes[currentLane].length === 0) {
      return false;
    }
    // Here, we expect the backend to supply the correct next lane,
    // so we simply use the first option if only one exists.
    const routeOptions = junctionRoutes[currentLane];
    if (routeOptions.length !== 1) {
      // Multiple possible routes – wait for backend update.
      return false;
    }
    const routeOption = routeOptions[0];
    const newLane = routeOption.nextLane;
    if (!newLane) return false;
    console.log(`Car ${carMesh.userData.carId} changed from lane ${currentLane} to ${newLane} (${routeOption.turnType})`);
    carMesh.userData.lane = newLane;
    carMesh.userData.hasCrossedLight = false;
    carMesh.userData.queuePosition = 0;
    carMesh.userData.progressAlongLane = 0;
    carMesh.userData.controllingTrafficLight = laneToTrafficLightMapping[newLane];
    carMesh.userData.nextTrafficLightChecked = false;
    carMesh.userData.isTransitioning = true;
    smoothTransitionCarLane(carMesh, newLane, 1000, () => {
      carMesh.userData.isTransitioning = false;
    });
    return true;
  };

  const calculateProgressAlongLane = (carPosition, laneNumber) => {
    const direction = laneDirections[laneNumber] || { x: 0, y: 0, z: 0 };
    const laneStart = laneStartPositions[laneNumber] || { x: 0, y: 0, z: 0 };
    return ((carPosition.x - laneStart.x) * direction.x) + ((carPosition.z - laneStart.z) * direction.z);
  };

  // Check if a car should update its controlling traffic light.
  // If multiple next light options exist, we do not choose automatically,
  // waiting instead for a backend update.
  const checkAndUpdateTrafficLight = (carMesh) => {
    if (!carMesh.userData.hasCrossedLight || carMesh.userData.nextTrafficLightChecked) {
      return;
    }
    const currentLightId = carMesh.userData.controllingTrafficLight;
    let mapping = nextTrafficLightMapping[currentLightId];
    if (!mapping) {
      carMesh.userData.nextTrafficLightChecked = true;
      return;
    }
    let option = null;
    if (Array.isArray(mapping)) {
      if (mapping.filter(o => o !== null).length === 1) {
        option = mapping.find(o => o !== null);
      } else {
        // Multiple options: do not auto-select; wait for backend to update the lane.
        return;
      }
    } else {
      option = mapping;
    }
    if (!option || !option.nextLight || !option.transitionPoint) {
      carMesh.userData.nextTrafficLightChecked = true;
      return;
    }
    const { nextLight, transitionPoint } = option;
    const distanceToTransition = Math.sqrt(
      Math.pow(carMesh.position.x - transitionPoint.x, 2) +
      Math.pow(carMesh.position.z - transitionPoint.z, 2)
    );
    if (distanceToTransition < 2) {
      console.log(`Car ${carMesh.userData.carId} changing control from light ${currentLightId} to light ${nextLight}`);
      carMesh.userData.controllingTrafficLight = nextLight;
      carMesh.userData.hasCrossedLight = false;
      carMesh.userData.nextTrafficLightChecked = true;
    }
  };

  useEffect(() => {
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
    const updateTrafficLight = (lightId, status) => {
      const light = trafficLights.current[`trafficLight${lightId}`];
      trafficLightStatuses.current[lightId] = status === "GREEN";
      if (light) {
        light.material = status === "GREEN" ? greenMaterial.clone() : redMaterial.clone();
      }
    };

    document.documentElement.style.setProperty('--canvas-width', `${CANVAS_WIDTH_EM}em`);
    document.documentElement.style.setProperty('--canvas-height', `${CANVAS_HEIGHT_EM}em`);
    const scene = sceneRef.current;
    scene.background = new THREE.Color('white');
    const aspectRatio = CANVAS_WIDTH_EM / CANVAS_HEIGHT_EM;
    camera = new THREE.PerspectiveCamera(50, aspectRatio, 0.1, 1000);
    camera.position.set(0, 100, 0);
    camera.lookAt(0, 0, 0);

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
            trafficLightStatuses.current[i] = false;
          }
        });
      }
    });

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

    const directionalLight = new THREE.DirectionalLight(0x1c1c1c, 50);
    directionalLight.position.set(0, 10, 0);
    scene.add(directionalLight);

    const moveSpeed = 0.1;
    const removalThreshold = 200;
    const getDistanceToLight = (carPos, lightPos, direction) => {
      if (direction.z !== 0) {
        return direction.z > 0 ? (lightPos.z - carPos.z) : (carPos.z - lightPos.z);
      }
      if (direction.x !== 0) {
        return direction.x > 0 ? (lightPos.x - carPos.x) : (carPos.x - lightPos.x);
      }
      return 0;
    };

    const animate = () => {
      const STOP_DISTANCE = 3;
      const CAR_LENGTH = 3;
      const CAR_MIN_DISTANCE = 2;
      const PAST_LIGHT_DISTANCE = 1;

      carMeshes.current.forEach((mesh) => {
        if (!mesh.userData.isTransitioning) {
          const lane = mesh.userData.lane;
          mesh.userData.progressAlongLane = calculateProgressAlongLane(mesh.position, lane);
          checkAndUpdateTrafficLight(mesh);
        }
      });

      const laneQueues = new Map();
      carMeshes.current.forEach((mesh) => {
        const lane = mesh.userData.lane;
        if (!laneQueues.has(lane)) {
          laneQueues.set(lane, []);
        }
        laneQueues.get(lane).push(mesh);
      });

      laneQueues.forEach((queue) => {
        queue.sort((a, b) => b.userData.progressAlongLane - a.userData.progressAlongLane);
        queue.forEach((mesh, index) => {
          mesh.userData.queuePosition = index;
        });
      });

      carMeshes.current.forEach((mesh, carId) => {
        if (mesh.userData.isTransitioning) return;
        const { lane } = mesh.userData;
        const controllingLightId = mesh.userData.controllingTrafficLight;
        const direction = laneDirections[lane] || { x: 0, y: 0, z: 0 };
        let shouldMove = true;

        if (controllingLightId) {
          const lightPos = trafficLightPositions[controllingLightId];
          if (lightPos) {
            const distanceToLight = getDistanceToLight(mesh.position, lightPos, direction);
            const stopDistance = STOP_DISTANCE + (mesh.userData.queuePosition * CAR_LENGTH);
            if (!mesh.userData.hasCrossedLight && distanceToLight <= PAST_LIGHT_DISTANCE) {
              if (trafficLightStatuses.current[controllingLightId]) {
                mesh.userData.hasCrossedLight = true;
                console.log(`Car ${carId} passed traffic light ${controllingLightId}`);
              }
            }
            if (mesh.userData.hasCrossedLight && distanceToLight < -PAST_LIGHT_DISTANCE && trafficLightStatuses.current[controllingLightId]) {
              if (laneToTrafficLightMapping[lane] === controllingLightId) {
                changeCarLane(mesh);
              }
            }
            if (distanceToLight <= stopDistance) {
              const isRedLight = !trafficLightStatuses.current[controllingLightId];
              if (isRedLight && !mesh.userData.hasCrossedLight) {
                shouldMove = false;
                mesh.userData.stopped = true;
              }
            }
          }
        }

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
          mesh.userData.progressAlongLane = calculateProgressAlongLane(mesh.position, lane);
        }

        if (mesh.position.distanceTo(camera.position) > removalThreshold) {
          scene.remove(mesh);
          carMeshes.current.delete(carId);
        }
      });

      renderer.render(scene, camera);
      requestAnimationFrame(animate);
    };
    animate();

    socketTraffic = new WebSocket("ws://localhost:8080/traffic");
    socketTraffic.onmessage = (event) => {
      const data = JSON.parse(event.data);
      console.log("Received traffic light update:", data);
      updateTrafficLight(data.lightId, data.status);
    };

    socketCars = new WebSocket("ws://localhost:8080/cars");
    socketCars.onmessage = (event) => {
      const data = JSON.parse(event.data);
      console.log("Received car update:", data);
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