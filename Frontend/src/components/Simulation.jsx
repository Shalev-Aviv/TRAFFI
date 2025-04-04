import { useEffect, useRef } from "react";
import './Simulation.css';
import * as THREE from "three";
import { GLTFLoader } from "three/examples/jsm/loaders/GLTFLoader";
//import Junction from "./junction.glb";

function Simulation() {
    const canvasRef = useRef(null);
    const sceneRef = useRef(new THREE.Scene());
    const carObjects = useRef([]);
    let socket = null;
    let camera, renderer;
    let planeHeight, planeWidth;

    const CANVAS_WIDTH_EM = 70;
    const CANVAS_HEIGHT_EM = 33.33;

    useEffect(() => {
        document.documentElement.style.setProperty('--canvas-width', `${CANVAS_WIDTH_EM}em`);
        document.documentElement.style.setProperty('--canvas-height', `${CANVAS_HEIGHT_EM}em`);

        const scene = sceneRef.current;
        scene.background = new THREE.Color('white');
        const aspectRatio = CANVAS_WIDTH_EM / CANVAS_HEIGHT_EM;

        // Camera
        camera = new THREE.PerspectiveCamera(
            50,
            aspectRatio,
            0.1,
            1000);
        camera.position.set(0, 100, 0);
        camera.lookAt(0, 0, 0);

        // test cube to find the center of the scene
        // const testGeometry = new THREE.BoxGeometry(10, 10, 10);
        // const testMaterial = new THREE.MeshBasicMaterial({ color: 0xff0000 });
        // const testCube = new THREE.Mesh(testGeometry, testMaterial);
        // scene.add(testCube);

        const gltfLoader = new GLTFLoader();
        let lodedModel; // Declare lodedModel here
        gltfLoader.load('/junction.glb', (gltfScene) => {
              lodedModel = gltfScene.scene;
              scene.add(lodedModel);
            },
        );

        // Renderer
        renderer = new THREE.WebGLRenderer({
            canvas: canvasRef.current,
            antialias: true
        });
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

        // Add directional light
        const directionalLight = new THREE.DirectionalLight(0x1c1c1c, 50);
        directionalLight.position.set(0, 10, 0);
        scene.add(directionalLight);
        // const directionalLight2 = new THREE.DirectionalLight(0x1c1c1c, 1);
        // directionalLight2.position.set(0, 7, -7);
        // scene.add(directionalLight2);

        const animate = () => {
            carObjects.current.forEach(car => {
                car.position.y -= 0.1;
            });
            renderer.render(scene, camera);
            requestAnimationFrame(animate);
        };
        animate();

        socket = new WebSocket("ws://localhost:8080/cars");
        socket.onmessage = (event) => {
            const data = JSON.parse(event.data);
            addCarToScene(data.lane, data.type);
        };

        return () => {
            socket?.close();
            window.removeEventListener("resize", updateSize);
        };
    }, []);

    const addCarToScene = (lane, type) => {
        const scene = sceneRef.current;
        const geometry = new THREE.CircleGeometry(2, 32);
        const material = new THREE.MeshBasicMaterial({
            color: (type === "POLICE" || type === "AMBULANCE") ? 0x0000ff : 0x000000
        });
        const carMesh = new THREE.Mesh(geometry, material);
        carMesh.rotation.x = -Math.PI / 2;
        const xPos = THREE.MathUtils.clamp(lane * 5, -planeWidth / 4, planeWidth / 4);
        const startY = camera.position.y - 5;
        carMesh.position.set(xPos, startY, 0);
        carMesh.renderOrder = 1;
        scene.add(carMesh);
        carObjects.current.push(carMesh);
    };

    return <canvas ref={canvasRef} className="ThreeJS"></canvas>;
}

export default Simulation;