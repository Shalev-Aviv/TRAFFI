package Shalev_Aviv.TRAFFI;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "http://localhost:3000") // Adjust the port if needed
public class TraffiApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(TraffiApplication.class, args);
	}

    @PostMapping("/api/json")
    public String postMethodName(@RequestBody Map<String, String> entity) {
        System.out.println("Received trafficLightsGraph: " + entity.get("trafficLightsGraph"));
		System.out.println("Received lanesDict: " + entity.get("lanesDict"));
		return "Received JSON successfully!";
    }

	@GetMapping("/")
	public String index() {
		return "Connected to the server!";
	}
}