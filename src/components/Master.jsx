import React, { useEffect, useRef } from "react";
import LocomotiveScroll from "locomotive-scroll";
import { Github, House, Gamepad2, FileSpreadsheet } from "lucide-react";

const Master = () => {
  const scrollRef = useRef(null);
  // Flag to indicate if the scroll-to-top animation is active
  const animatingRef = useRef(false);
  // Reference to the manual-interruption handler (so we can remove it later)
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
      // Set the flag indicating an animated scroll is active
      animatingRef.current = true;
      
      // Define an interruption handler that cancels the animation if user scrolls manually
      const handleUserScroll = (e) => {
        if (animatingRef.current && scrollRef.current) {
          // Cancel the current animation if user intervenes
          scrollRef.current.stop();
          animatingRef.current = false;
          // Remove this handler once we cancel the animation
          container.removeEventListener("wheel", handleUserScroll, { passive: true });
        }
      };

      // Store the handler reference so we can remove it if needed
      interruptionHandlerRef.current = handleUserScroll;
      // Listen for manual scroll events (non-blocking)
      container.addEventListener("wheel", handleUserScroll, { passive: true });

      // Stop any current scroll animations and start scrolling to the top
      scrollRef.current.stop();
      scrollRef.current.scrollTo(0, {
        duration: 1000,
        easing: [0.25, 0.0, 0.25, 0.5],
        callback: () => {
          // Once the animation is finished, clear the flag and remove the temporary event listener
          animatingRef.current = false;
          container.removeEventListener("wheel", handleUserScroll, { passive: true });
          // Restart locomotive scroll so that future scrollTo calls animate smoothly
          scrollRef.current.start();
        },
      });
    }
  };

  return (
    <nav className="Master">
      <div className="IconsContainer">
        <a href="https://github.com/Shalev-Aviv/TRAFFI" target="_blank" rel="noopener noreferrer">
          <Github className="Icons" id="Github" />
        </a>
        <a onClick={scrollToTop} style={{ cursor: "pointer" }}>
          <House className="Icons" id="House" />
        </a>
        <a>
          <Gamepad2 className="Icons" id="Controller" />
        </a>
        <a>
          <FileSpreadsheet className="Icons" id="Read" />
        </a>
      </div>
      <nav className="Line"></nav>
    </nav>
  );
};

export default Master;
