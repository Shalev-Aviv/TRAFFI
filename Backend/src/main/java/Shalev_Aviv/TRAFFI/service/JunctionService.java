// 1. Create a service to wrap Junction’s async methods.
package Shalev_Aviv.TRAFFI.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import Shalev_Aviv.TRAFFI.Junction;

@Service
public class JunctionService {
    private Junction junction;

    // Set the Junction instance after creation.
    public void setJunction(Junction junction) {
        this.junction = junction;
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