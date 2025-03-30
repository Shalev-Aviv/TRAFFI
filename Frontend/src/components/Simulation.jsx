import './Simulation.css';

import { useEffect, useRef } from "react";
import * as THREE from "three";

function Simulation() {
    const canvasRef = useRef(null);

    useEffect(() => {
        const scene = new THREE.Scene();

        const camera = new THREE.PerspectiveCamera(
            50,
            1, // Placeholder aspect ratio (will be fixed below)
            1,
            1000
        );
        camera.position.z = 96;

        const renderer = new THREE.WebGLRenderer({ canvas: canvasRef.current, antialias: true });
        renderer.setSize(300, 300); // Adjust this to control the canvas size

        const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
        scene.add(ambientLight);

        const spotLight = new THREE.SpotLight(0xffffff, 1);
        spotLight.position.set(0, 64, 32);
        scene.add(spotLight);

        const boxGeometry = new THREE.BoxGeometry(16, 16, 16);
        const boxMaterial = new THREE.MeshNormalMaterial();
        const boxMesh = new THREE.Mesh(boxGeometry, boxMaterial);
        scene.add(boxMesh);

        const animate = () => {
            boxMesh.rotation.x += 0.01;
            boxMesh.rotation.y += 0.01;
            renderer.render(scene, camera);
            requestAnimationFrame(animate);
        };
        animate();
    }, []);

    return (
        <div className="ThreeJS">
            <canvas ref={canvasRef}></canvas>
        </div>
    );
    
}

export default Simulation;