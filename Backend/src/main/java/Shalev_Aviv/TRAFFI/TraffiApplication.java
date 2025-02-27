package Shalev_Aviv.TRAFFI;

import java.io.IOException;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import Shalev_Aviv.TRAFFI.service.JsonConverter;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "http://localhost:3000") // Adjust the port if needed
public class TraffiApplication {
    private int[][] trafficLightsMatrix;
    private Map<Integer, int[]> lightsToLanesMap;
    private Map<Integer, int[]> lanesToLanesMap;

    public static void main(String[] args) {
        SpringApplication.run(TraffiApplication.class, args);
    }

    @PostMapping("/api/json")
    public String postMethodName(@RequestBody Map<String, String> entity) {
        try {
            String trafficLightsGraphJson = entity.get("trafficLightsGraph");
            String lanesToLightsGraphJson = entity.get("lanesToLightsGraph");
            String lanesDictJson = entity.get("lanesDict");
            
            // Log the received JSON for debugging
            System.out.println("Received trafficLightsGraph: " + trafficLightsGraphJson);
            System.out.println("Received lanesToLightsGraph: " + lanesToLightsGraphJson);
            System.out.println("Received lanesDict: " + lanesDictJson);
            
            // Convert the JSON strings to Java objects
            trafficLightsMatrix = JsonConverter.convertJsonToMatrix(trafficLightsGraphJson);
            lightsToLanesMap = JsonConverter.convertLightsToLanesMap(lanesToLightsGraphJson);
            lanesToLanesMap = JsonConverter.convertlanesToLanesMap(lanesDictJson);
            
            // Log converted objects to verify
            System.out.println("Successfully converted trafficLightsMatrix");
            System.out.println("Successfully converted lightsToLanesMap with " + lightsToLanesMap.size() + " entries");
            System.out.println("Successfully converted lanesToLanesMap with " + lanesToLanesMap.size() + " entries");
            
            return "Received and processed JSON successfully!";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error processing JSON: " + e.getMessage();
        }
    }

    @GetMapping("/api/lights-matrix")
    public int[][] getMatrix() {
        return trafficLightsMatrix;
    }
    @GetMapping("/api/lights-map")
    public Map<Integer, int[]> getLightsToLanesMap() {
        return lightsToLanesMap;
    }
    @GetMapping("/api/lanes-map")
    public Map<Integer, int[]> getlanesToLanesMap() {
        return lanesToLanesMap;
    }

    @GetMapping("/")
    public String index() {
        return "Connected to the server!";
    }
}