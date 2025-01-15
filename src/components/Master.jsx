import { Github, House, Gamepad2, FileSpreadsheet } from "lucide-react";

function Master() {
  const scrollToTop = () => {
    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
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
    </nav>
  );
}

export default Master;
