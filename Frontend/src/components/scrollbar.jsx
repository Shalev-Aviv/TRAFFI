import React, { useEffect, useRef } from "react";
import LocomotiveScroll from "locomotive-scroll";
import "locomotive-scroll/dist/locomotive-scroll.css";

const Scrollbar = ({ children }) => {
  const containerRef = useRef(null);

  useEffect(() => {
    // Initialize LocomotiveScroll
    const scroll = new LocomotiveScroll({
      el: containerRef.current,
      smooth: true,
      smoothMobile: true,
      inertia: 0.3, /* Scroll's speed */
      multiplier: 3.0, /* How much the scroll scrolls */
    });

    // Cleanup on unmount
    return () => {
      if (scroll) scroll.destroy();
    };
  }, []);

  return (
    <div data-scroll-container ref={containerRef}>
      {children}
    </div>
  );
};

export default Scrollbar;
