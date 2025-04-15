package Shalev_Aviv.TRAFFI;

import java.io.IOException;
import java.util.Map;
import java.util.BitSet;
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

import Shalev_Aviv.TRAFFI.Service.JsonConverter;
import Shalev_Aviv.TRAFFI.Service.JunctionService;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@EnableAsync
public class TraffiApplication {
    private int[][] trafficLightsMatrix; // Traffic lights matrix
    private Map<Integer, Integer[]> lightsToLanesMap; // Map of lights to lanes
    private Map<Integer, Integer[]> lanesToLanesMap; // Map of lanes to lanes
    private Junction junction; // junction

    public Map<Integer, Integer[]> getLanesToLanesMap() { return this.lanesToLanesMap; }

    @Autowired
    private JunctionService junctionService;

    public static void main(String[] args) {
        SpringApplication.run(TraffiApplication.class, args);
    }

    /** Parse JSON data from the frontend to Java DS*/
    private void parseJsonData(Map<String, String> entity) throws IOException {
        String trafficLightsMatrixJson = entity.get("trafficLightsMatrix");
        String lanesToLightsMapJson = entity.get("lightsToLanesMap");
        String lanesMapJson = entity.get("lanesToLanesMap");

        trafficLightsMatrix = JsonConverter.convertJsonToMatrix(trafficLightsMatrixJson);
        lightsToLanesMap = JsonConverter.convertLightsToLanesMap(lanesToLightsMapJson);
        lanesToLanesMap = JsonConverter.convertlanesToLanesMap(lanesMapJson);
    }
    /** Create lanes based on the lanes map*/
    private Lane[] createLanes() {
        Lane[] lanes = new Lane[lanesToLanesMap.size()];
        for (int i = 0; i < lanesToLanesMap.size(); i++) {
            lanes[i] = new Lane(i + 1);
        }
        return lanes;
    }
    /** Create traffic lights based on the lanes they control*/
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

    private void initializeGraphBitSets(int[][] trafficLightMatrix, BitSet[] trafficLightsConnections, BitSet[] trafficLightsStrongConnections) {
        if (trafficLightMatrix == null) {
            throw new IllegalArgumentException("Invalid traffic light graph: null");
        }
        int n = trafficLightMatrix.length;

        if (trafficLightsConnections == null || trafficLightsConnections.length != n ||
            trafficLightsStrongConnections == null || trafficLightsStrongConnections.length != n) {
             throw new IllegalArgumentException("Provided BitSet arrays have incorrect size or are null.");
        }
        
        for (int i = 0; i < n; i++) {
            trafficLightsConnections[i] = new BitSet(n);
            trafficLightsStrongConnections[i] = new BitSet(n);

            // Populate the BitSets based on the matrix
            for (int j = 0; j < n; j++) {
                if (trafficLightMatrix[i][j] != 0) {
                    trafficLightsConnections[i].set(j);
                }
                if (trafficLightMatrix[i][j] == 2) {
                    trafficLightsStrongConnections[i].set(j);
                }
            }
        }
    }

    @PostMapping("/parsing")
    public ResponseEntity<Map<String, String>> postMethodName(@RequestBody Map<String, String> entity) {
        try {
            parseJsonData(entity);
            Lane[] lanes = createLanes(); // Create lanes
            TrafficLight[] trafficLights = createTrafficLights(lanes); // Create traffic lights
            BitSet[] trafficLightsConnections = new BitSet[trafficLightsMatrix.length];
            BitSet[] trafficLightsStrongConnections = new BitSet[trafficLightsMatrix.length];
            initializeGraphBitSets(trafficLightsMatrix, trafficLightsConnections, trafficLightsStrongConnections); // Changing from matrix to BitSet(s)
            junction = new Junction(trafficLightsConnections, trafficLightsStrongConnections, trafficLights, lanesToLanesMap, lanes); // Create junction
            
            // Print junction (DEBUG)
            System.out.println(junction.toString());

            // Start simulation
            junctionService.setJunction(junction);
            junctionService.addCarsAsync(1000); // Add cars asynchronously to the junction
            junctionService.manageTrafficLights(); // Controls the traffic lights

            // Return response
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