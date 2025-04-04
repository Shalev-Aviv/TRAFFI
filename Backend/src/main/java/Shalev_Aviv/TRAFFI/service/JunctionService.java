package Shalev_Aviv.TRAFFI.Service;
import Shalev_Aviv.TRAFFI.WebSocket.CarWebSocketHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import Shalev_Aviv.TRAFFI.Junction;

@Service
public class JunctionService {
    @Autowired
    private CarWebSocketHandler carWebSocketHandler;

    private Junction junction;

    // Set the Junction instance after creation.
    public void setJunction(Junction junction) {
        this.junction = junction;
        // Manually inject the dependency into the junction
        this.junction.setWebSocketHandler(carWebSocketHandler);
    }

    @Async
    public void addCarsAsync(int delay) {
        junction.addCarsAsync(delay);
    }

    @Async
    public void manageTrafficLights() {
        junction.manageTrafficLights();
    }
}