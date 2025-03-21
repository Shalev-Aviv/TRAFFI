package Shalev_Aviv.TRAFFI;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import Shalev_Aviv.TRAFFI.service.JsonConverter;
import Shalev_Aviv.TRAFFI.service.TrafficService;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@EnableAsync
public class TraffiApplication {
    private int[][] trafficLightsMatrix;
    private Map<Integer, Integer[]> lightsToLanesMap;
    private Map<Integer, Integer[]> lanesToLanesMap;
    private Junction junction;

    @Autowired
    private TrafficService trafficService;

    public static void main(String[] args) {
        SpringApplication.run(TraffiApplication.class, args);
    }

    @PostMapping("/parsing")
    public ResponseEntity<Map<String, String>> postMethodName(@RequestBody Map<String, String> entity) {
        try {
            parseJsonData(entity);
            Lane[] lanes = createLanes(); // Create lanes
            TrafficLight[] trafficLights = createTrafficLights(lanes); // Create traffic lights
            junction = new Junction(trafficLightsMatrix, trafficLights, lanesToLanesMap, lanes); // Create junction
            System.out.println(junction.toString()); // Print junction

            trafficService.setJunction(junction); // Set junction in traffic service
            trafficService.addCarsAsync(10, 1000); // Add cars to junction asynchronously

            System.out.println(junction.toString());
            System.out.println(junction.MaxWeight());
            try {
                Thread.sleep(1000*15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(junction.toString());
            System.out.println(junction.MaxWeight()+1);

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

    private void parseJsonData(Map<String, String> entity) throws IOException {
        String trafficLightsMatrixJson = entity.get("trafficLightsMatrix");
        String lanesToLightsMapJson = entity.get("lightsToLanesMap");
        String lanesMapJson = entity.get("lanesToLanesMap");

        trafficLightsMatrix = JsonConverter.convertJsonToMatrix(trafficLightsMatrixJson);
        lightsToLanesMap = JsonConverter.convertLightsToLanesMap(lanesToLightsMapJson);
        lanesToLanesMap = JsonConverter.convertlanesToLanesMap(lanesMapJson);
    }

    /**
     * Create lanes based on the lanes map
     * @return Lane[]
     */
    private Lane[] createLanes() {
        Lane[] lanes = new Lane[lanesToLanesMap.size()];
        for (int i = 0; i < lanesToLanesMap.size(); i++) {
            lanes[i] = new Lane(i + 1);
        }
        return lanes;
    }

    /**
     * Create traffic lights based on the lanes they control
     * @param lanes
     * @return TrafficLight[]
     */
    private TrafficLight[] createTrafficLights(Lane[] lanes) {
        TrafficLight[] trafficLights = new TrafficLight[lightsToLanesMap.size()];
        int lightIndex = 0;
        for (int i = 0; i < lightsToLanesMap.size(); i++) {
            Integer lightId = i + 1;
            Integer[] laneIds = lightsToLanesMap.get(lightId);
            if (laneIds != null) {
                int length = laneIds.length;
                Lane[] lightLanes = new Lane[length];
                for (int j = 0; j < length; j++) {
                    int laneId = laneIds[j];
                    if (laneId >= 1 && laneId <= lanes.length) {
                        lightLanes[j] = lanes[laneId - 1];
                    } else {
                        System.err.println("Warning: Lane ID " + laneId + " out of bounds for lanes array for traffic light " + lightId);
                    }
                }
                TrafficLight trafficLight = new TrafficLight(lightLanes, lightId);
                trafficLights[lightIndex++] = trafficLight;
                
                // Set parent traffic light for each lane
                for (Lane lane : lightLanes) {
                    if (lane != null) {
                        lane.setParentTrafficLight(trafficLight);
                    }
                }
            } else {
                System.err.println("Warning: No lanes found for traffic light " + lightId);
            }
        }
        return trafficLights;
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
