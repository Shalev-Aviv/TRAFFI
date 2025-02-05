package Shalev_Aviv.TRAFFI.Controllers;
import org.springframework.web.bind.annotation.*;
import Shalev_Aviv.TRAFFI.Car;

@RestController
@RequestMapping("/cars")
@CrossOrigin(origins = "http://localhost:3000") // Allow frontend to call backend
public class CarController {

    @GetMapping("/test")
    public String testEndpoint() {
        return "Car controller is working!";
    }

    @PostMapping("/create")
    public String createCar(@RequestParam Car.CarType type) {
        Car newCar = new Car(type);
        System.out.println("Car created: " + newCar.getType());
        return "Car of type " + type + " created!";
    }
}
