import { useEffect, useRef } from "react";
import LocomotiveScroll from "locomotive-scroll";
import { Github, House, Gamepad2, FileSpreadsheet } from "lucide-react";

function Master() {
  const scrollRef = useRef(null);

  useEffect(() => {
    scrollRef.current = new LocomotiveScroll({
      el: document.querySelector("[data-scroll-container]"),
      smooth: true,
      smoothMobile: true,
      inertia: 0.3,
      multiplier: 3.0,
    });

    return () => {
      if (scrollRef.current) scrollRef.current.destroy();
    };
  }, []);

  const scrollToTop = () => {
    if (scrollRef.current) {
      scrollRef.current.stop(); // Temporarily stop Locomotive Scroll
      scrollRef.current.scrollTo(0, {
        duration: 1000, 
        easing: [0.25, 0.0, 0.35, 1.0],
        callback: () => {
          scrollRef.current.start(); // Resume Locomotive Scroll after scrolling is done
        },
      });
    }
  };
  

  return (
     <nav className="Master">
       <div className="IconsContainer">
         <a href="https://github.com/Shalev-Aviv/TRAFFI" target="_blank" rel="noopener noreferrer">
           <Github className="Icons" id="Github"/>
         </a>
         <a onClick={scrollToTop} style={{ cursor: "pointer" }}>
           <House className="Icons" id="House"/>
         </a>
         <a>
           <Gamepad2 className="Icons" id="Controller"  />
         </a>
         <a>
           <FileSpreadsheet className="Icons" id="Read"/>
         </a>
       </div>
         <nav className="Line"></nav>
     </nav>
  );
}

export default Master;
