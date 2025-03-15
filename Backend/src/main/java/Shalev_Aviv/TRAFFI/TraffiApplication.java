package Shalev_Aviv.TRAFFI;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private Map<Integer, Integer[]> lightsToLanesMap;
    private Map<Integer, Integer[]> lanesToLanesMap;

    public static void main(String[] args) {
        SpringApplication.run(TraffiApplication.class, args);
    }

    @PostMapping("/parsing")
    public ResponseEntity<Map<String, String>> postMethodName(@RequestBody Map<String, String> entity) {
        try {
            String trafficLightsMatrixJson = entity.get("trafficLightsMatrix");
            String lanesToLightsMapJson = entity.get("lightsToLanesMap");
            String lanesMapJson = entity.get("lanesToLanesMap");

            // Convert the JSON strings to Java objects
            trafficLightsMatrix = JsonConverter.convertJsonToMatrix(trafficLightsMatrixJson);
            lightsToLanesMap = JsonConverter.convertLightsToLanesMap(lanesToLightsMapJson);
            lanesToLanesMap = JsonConverter.convertlanesToLanesMap(lanesMapJson);
            


            // Creating the lanes, 1-based indexing
            Lane[] lanes = new Lane[lanesToLanesMap.size()];
            for (int i = 0; i < lanesToLanesMap.size(); i++) {
                lanes[i] = new Lane(i+1);
            }

            // Creating the traffic lights
            TrafficLight[] trafficLights = new TrafficLight[lightsToLanesMap.size()];
            int lightIndex = 0;
            for (int i = 0; i < lightsToLanesMap.size(); i++) {
                Integer lightId = i + 1; // Traffic light IDs are 1-based
                Integer[] laneIds = lightsToLanesMap.get(lightId);
                if (laneIds != null) {
                    int length = laneIds.length;
                    Lane[] lightLanes = new Lane[length];
                    for (int j = 0; j < length; j++) {
                        int laneId = laneIds[j];
                        if (laneId >= 1 && laneId <= lanes.length) {
                            lightLanes[j] = lanes[laneId - 1];
                        }
                        else {
                            System.err.println("Warning: Lane ID " + laneId + " out of bounds for lanes array for traffic light " + lightId);
                        }
                    }
                    trafficLights[lightIndex++] = new TrafficLight(lightLanes, lightId);
                }
                else {
                    System.err.println("Warning: No lanes found for traffic light " + lightId);
                }
            }

            // Creating the junction
            Junction junction = new Junction(trafficLightsMatrix, trafficLights, lanesToLanesMap, lanes);
            System.out.println(junction.toString());


            Map<String, String> response = new HashMap<>();
            response.put("message", "Received and processed JSON successfully!");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (IOException e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error processing JSON: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/lights-matrix")
    public int[][] getMatrix() {
        return trafficLightsMatrix;
    }
    @GetMapping("/lights-map")
    public Map<Integer, Integer[]> getLightsToLanesMap() {
        return lightsToLanesMap;
    }
    @GetMapping("/lanes-map")
    public Map<Integer, Integer[]> getlanesToLanesMap() {
        return lanesToLanesMap;
    }

    @GetMapping("/")
    public String index() {
        return "Connected to the server!";
    }
}