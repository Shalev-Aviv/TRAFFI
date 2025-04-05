package Shalev_Aviv.TRAFFI.Service;
import Shalev_Aviv.TRAFFI.WebSocket.CarWebSocketHandler;
import Shalev_Aviv.TRAFFI.WebSocket.TrafficLightWebSocketHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import Shalev_Aviv.TRAFFI.Junction;

@Service
public class JunctionService {
    @Autowired
    private CarWebSocketHandler carWebSocketHandler;
    @Autowired
    private TrafficLightWebSocketHandler trafficLightWebSocketHandler;

    private Junction junction;

    // Set the Junction instance after creation.
    public void setJunction(Junction junction) {
        this.junction = junction;
        // Manually inject the dependency into the junction
        this.junction.setCarsWebSocketHandler(carWebSocketHandler);
        this.junction.setTrafficLightWebSocketHandler(trafficLightWebSocketHandler);
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