import './Navbar.css'
import React, { useEffect, useRef } from "react";
import LocomotiveScroll from "locomotive-scroll";
import { Github, House, Gamepad2, FileSpreadsheet } from "lucide-react";

const Navbar = () => {
  const scrollRef = useRef(null);
  const animatingRef = useRef(false);
  const interruptionHandlerRef = useRef(null);

  useEffect(() => {
    // Initialize Locomotive Scroll on mount
    scrollRef.current = new LocomotiveScroll({
      el: document.querySelector("[data-scroll-container]"),
      smooth: true,
      smoothMobile: true,
      inertia: 0.3,
      multiplier: 3.0,
    });

    return () => {
      if (scrollRef.current) {
        scrollRef.current.destroy();
      }
    };
  }, []);

  const scrollToTop = () => {
    if (scrollRef.current) {
      const container = document.querySelector("[data-scroll-container]");
      animatingRef.current = true;
      
      const handleUserScroll = (e) => {
        if (animatingRef.current && scrollRef.current) {
          scrollRef.current.stop();
          animatingRef.current = false;
          container.removeEventListener("wheel", handleUserScroll, { passive: true });
        }
      };

      interruptionHandlerRef.current = handleUserScroll;
      container.addEventListener("wheel", handleUserScroll, { passive: true });

      scrollRef.current.stop();
      scrollRef.current.scrollTo(0, {
        duration: 1000,
        easing: [0.25, 0.0, 0.25, 0.5],
        callback: () => {
          animatingRef.current = false;
          container.removeEventListener("wheel", handleUserScroll, { passive: true });
          scrollRef.current.start();
        },
      });
    }
  };

  const scrollToSimulation = () => {
    if (scrollRef.current) {
      const startButton = document.querySelector('.Start-container');
      if (startButton) {
        const container = document.querySelector("[data-scroll-container]");
        animatingRef.current = true;
        
        const handleUserScroll = (e) => {
          if (animatingRef.current && scrollRef.current) {
            scrollRef.current.stop();
            animatingRef.current = false;
            container.removeEventListener("wheel", handleUserScroll, { passive: true });
          }
        };

        interruptionHandlerRef.current = handleUserScroll;
        container.addEventListener("wheel", handleUserScroll, { passive: true });

        scrollRef.current.stop();
        scrollRef.current.scrollTo(startButton, {
          duration: 1000,
          easing: [0.25, 0.0, 0.25, 0.5],
          offset: -100, // Scroll to 100px above the button
          callback: () => {
            animatingRef.current = false;
            container.removeEventListener("wheel", handleUserScroll, { passive: true });
            scrollRef.current.start();
          },
        });
      }
    }
  };

  return (
    <nav className="Master">
      <div className="IconsContainer">
        <a href="https://github.com/Shalev-Aviv/TRAFFI" target="_blank" rel="noopener noreferrer">
          <Github className="Icons" id="Github" />
        </a>
        <a onClick={scrollToTop} style={{ cursor: "pointer" }} href="#">
          <House className="Icons" id="House" />
        </a>
        <a onClick={scrollToSimulation} style={{ cursor: "pointer" }} href="#">
          <Gamepad2 className="Icons" id="Controller" />
        </a>
        <a style={{ cursor: "pointer"}} href='#'>
          <FileSpreadsheet className="Icons" id="Read" />
        </a>
      </div>
      <nav className="Line"></nav>
    </nav>
  );
};

export default Navbar;