package Shalev_Aviv.TRAFFI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import Shalev_Aviv.TRAFFI.WebSocket.CarWebSocketHandler;
import Shalev_Aviv.TRAFFI.WebSocket.TrafficLightWebSocketHandler;

// Junction class - representing the entire junction
public class Junction {
    private Map<Lane, TrafficLight> laneToTrafficLightMap; // Map of lanes to traffic lights

    private int[][] trafficLightGraph; // Matrix representing the connections between traffic lights
    private TrafficLight[] trafficLightsArray; // Traffic lights at the junction
    private Map<Integer, Integer[]> lanesMap; // Map of lanes to destinations
    private Lane[] lanes; // Lanes at the junction
    private Set<Integer> destinationLanes; // Set of destination lanes
    private List<Integer> enteringLanes; // List of entering lanes

    /** Constructor*/
    public Junction(int[][] trafficLightGraph, TrafficLight[] trafficLightsArray, Map<Integer, Integer[]> lanesMap, Lane[] lanes) {
        this.trafficLightGraph = trafficLightGraph;
        this.trafficLightsArray = trafficLightsArray;
        this.lanesMap = lanesMap;
        this.lanes = lanes;

        // Build reverse mapping of lanes to traffic lights
        this.laneToTrafficLightMap = new HashMap<>();
        for (TrafficLight light : trafficLightsArray) {
            for (Lane lane : light.getLanes()) {
                laneToTrafficLightMap.put(lane, light);
            }
        }

        // Build destination lanes
        this.destinationLanes = new HashSet<>();
        for (Integer[] targets : lanesMap.values()) {
            if (targets != null) {
                Collections.addAll(destinationLanes, targets);
            }
        }
        // Build entering lanes
        this.enteringLanes = new ArrayList<>();
        for (Map.Entry<Integer, Integer[]> entry : lanesMap.entrySet()) {
            if (entry.getValue() != null && !destinationLanes.contains(entry.getKey())) {
                enteringLanes.add(entry.getKey());
            }
        }
    }

    /** return the index of the traffic light with the largest weight<p>
     * <STRONG>O(n)</STRONG><p>
     * n -> length of <CODE>trafficLightsArray</CODE>
     * */
    public int maxWeightIndex() {
        return maxRegularWeight(maxEmergencyWeight());
    }
    /** return the maximum emergency weight among all traffic lights<p>
     * <STRONG>O(n)</STRONG><p>
     * n -> length of <CODE>trafficLightsArray</CODE>
     * */
    private int maxEmergencyWeight() {
        int maxWeight = 0;
        for (int i = 0; i < trafficLightsArray.length; i++) {
            if (trafficLightsArray[i].getEmergencyWeight() > maxWeight) {
                maxWeight = trafficLightsArray[i].getEmergencyWeight();
            }
        }
        return maxWeight;
    }
    /** return the index of the traffic light with the largest regular weight among the lights with the same maximum emergency weight<p>
     * <STRONG>O(n)</STRONG><p>
     * n -> length of <CODE>trafficLightsArray</CODE>
     * */
    private int maxRegularWeight(int maxEmergencyWeight) {
        List<Integer> candidates = new ArrayList<>();
        int maxWeight = 0;
        // First find all traffic lights with the maximum emergency weight
        for (int i = 0; i < trafficLightsArray.length; i++) {
            if (trafficLightsArray[i].getEmergencyWeight() == maxEmergencyWeight) {
                if (trafficLightsArray[i].getRegularWeight() > maxWeight) {
                    maxWeight = trafficLightsArray[i].getRegularWeight();
                    candidates.clear();
                    candidates.add(i);
                }
                else if (trafficLightsArray[i].getRegularWeight() == maxWeight) {
                    candidates.add(i);
                }
            }
        }
    
        Random rand = new Random();
        return candidates.get(rand.nextInt(candidates.size()));
    }

    @Autowired
    private CarWebSocketHandler webSocketHandler;
    // Setter for manual injection
    public void setCarsWebSocketHandler(CarWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }
    /** Async function that adds cars to the lanes
     * <STRONG>O(n)</STRONG><p>
     * n -> <CODE>while(true)</CODE>
    */
    @Async
    public void addCarsAsync(int delay) {
        System.out.println("Starting to add cars asynchronously");

        Car.CarType[] carType = {
            Car.CarType.PRIVATE, Car.CarType.PRIVATE, Car.CarType.PRIVATE,
            Car.CarType.MOTORCYCLE, Car.CarType.MOTORCYCLE, Car.CarType.MOTORCYCLE,
            Car.CarType.POLICE, Car.CarType.POLICE,
            Car.CarType.AMBULANCE, Car.CarType.AMBULANCE
        };
        Random rand = new Random();
        while (true) {
            int randomIndex = rand.nextInt(carType.length);
            Car newCar = new Car(carType[randomIndex]);
            // Pick a random lane from the entering lanes (1-indexed)
            int laneId = this.enteringLanes.get(rand.nextInt(this.enteringLanes.size()));
            // Use (laneId - 1) when accessing the lanes array (which is 0-indexed)
            lanes[laneId - 1].addCar(newCar);
            System.out.println("Added car to lane " + laneId + ", traffic light " + (laneToTrafficLightMap.get(lanes[laneId - 1]).getId()));
            // Send the update with the correct laneId
            webSocketHandler.sendCarUpdate(newCar.getId(), laneId, newCar.getType().toString());
        
            try {
                Thread.sleep(delay);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Error adding cars: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    /** Async function that finds the maximum-weighted-traffic-light (MWTL) and finds the largest clique (based on weight) that the MWTL appears in, and make it green<p>
     * <STRONG>O(n^3)</STRONG><p>
     * n -> length of <CODE>trafficLightsArray</CODE><p>
     * m -> size of clique
    */
    @Async
    public void manageTrafficLights() {
        while(true) {
            int maxWeightIndex = maxWeightIndex();
            System.out.println("Maximum-weighted-traffic-light: " + (maxWeightIndex + 1));
            Set<Integer> clique = findLargestClique(maxWeightIndex);
            
            // Print the clique
            System.out.println("Clique:");
            for (Integer i : clique) {
                System.out.print(i+1+", ");
            } System.out.println("\n");

            changeLights(clique);
            System.out.println("Iteration complete\n");

            // Delay before next iteration
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Error managing traffic lights: " + e.getMessage());
                System.exit(1);
            }
        }
    }
    /** finds the traffic light with the maximum weight in the set and returns it<p>
     * <STRONG>O(n)</STRONG><p>
     * n -> size of set
    */
    private int maxWeightIndexFromSet(Set<Integer> set) {
        return maxRegularWeightIndexFromSet(set, maxEmergencyWeightFromSet(set));
    }
    /** finds the maximum emergency weight in the set<p>
     * <STRONG>O(n)</STRONG><p>
     * n -> size of set
    */
    private int maxEmergencyWeightFromSet(Set<Integer> set) {
        List<Integer> setList = new ArrayList<>(set);
        int maxWeight = trafficLightsArray[setList.get(0)].getEmergencyWeight();
        for (int i = 0; i < setList.size(); i++) {
            int current = setList.get(i);
            if (trafficLightsArray[current].getEmergencyWeight() > maxWeight) {
                maxWeight = trafficLightsArray[current].getEmergencyWeight();
            }
        }
        return maxWeight;
    }
    /** finds the traffic light with the maximum regular weight in the set, given the maximum emergency weight<p>
     * <STRONG>O(n)</STRONG><p>
     * n -> size of set
    */
    private int maxRegularWeightIndexFromSet(Set<Integer> set, int maxEmergencyWeight) {
        List<Integer> candidates = new ArrayList<>();
        int maxRegularWeight = 0;
    
        for (Integer index : set) {
            TrafficLight light = trafficLightsArray[index];
            if (light.getEmergencyWeight() == maxEmergencyWeight) {
                if (light.getRegularWeight() > maxRegularWeight) {
                    maxRegularWeight = light.getRegularWeight();
                    candidates.clear();
                    candidates.add(index);
                }
                else if (light.getRegularWeight() == maxRegularWeight) {
                    candidates.add(index);
                }
            }
        }
        // Randomize selection among candidates with the same weight
        Random rand = new Random();
        return candidates.get(rand.nextInt(candidates.size()));
    }
    /** finds the largest clique in the graph that contains the maximum weighted traffic light<p>
     * O(n^2 * m)<p>
     * n -> size of the clique<p>
     * m -> length of the <CODE>trafficLightsArray</CODE>
    */
    private Set<Integer> findLargestClique(int maxWeightIndex) {
        HashSet<Integer> clique = new HashSet<>();
        clique.add(maxWeightIndex);
        boolean stop = false;
        do {
           findStrongConnection(clique, maxWeightIndex);
           // Add all possible traffic lights to temp set
           HashSet<Integer> temp = new HashSet<>();
           for(int i = 0; i < trafficLightsArray.length; i++) {
                if(!clique.contains(i)) {
                    if(canWeAddThis(clique, i)) {
                        temp.add(i);
                    }    
                }
            }
            // Find the traffic light with the maximum weight in the temp set and add it to the clique
            if(temp.isEmpty()) {
                stop = true;
            }
            else {
                int candidate = maxWeightIndexFromSet(temp);
                if(clique.contains(candidate)) {
                    stop = true;
                }
                else {
                    clique.add(candidate);
                }
            }
        } while(canWeAdd(clique) && !stop);

        return clique;
    }
    /** return false if we found the largest clique, return true otherwise<p>
     * O(n^2)<p>
     * n -> the size of the clique*/
    private boolean canWeAdd(Set<Integer> clique) {
        boolean canWeAdd = false;
        for(int i = 0; i < trafficLightsArray.length && !canWeAdd; i++) {
            if(!clique.contains(i)) {
                if(canWeAddThis(clique, i)) {
                    canWeAdd = true;
                }
            }
        }
        return canWeAdd;
    }
    /** return true if we can add a trafficLightsArray[index] to the clique<p>
     * O(n)<p>
     * n -> the size of the clique
     * */
    private boolean canWeAddThis(Set<Integer> clique, int index) {
        boolean canAdd = true;
        for(Integer i : clique) {
            canAdd = canAdd && (trafficLightGraph[index][i] != 0);
        }
        return canAdd;
    }
    /** finds if a traffic light has a strong connection with another traffic light, and if so - add it to the clique<p>
     * <STRONG>O(n*m)</STRONG><p>
     * n -> length of the matrix<p>
     * m -> size of the clique
     * */
    private void findStrongConnection(Set<Integer> clique, int index) {
        for(int i = 0; i < trafficLightGraph.length; i++) {
            if(trafficLightGraph[index][i] == 2 && !clique.contains(i)) {
                if(canWeAddThis(clique, i)) {
                    clique.add(i);
                }
            }
        }
    }
    
    @Autowired
    private TrafficLightWebSocketHandler trafficLightWebSocketHandler;
    // Setter for manual injection
    public void setTrafficLightWebSocketHandler(TrafficLightWebSocketHandler trafficLightWebSocketHandler) {
        this.trafficLightWebSocketHandler = trafficLightWebSocketHandler;
    }
    /** changes the lights of the traffic lights in the clique<p>
     * O(n)
     * n -> the amount of traffic lights in the junction
     * */
    private void changeLights(Set<Integer> clique) {
        for (int i = 0; i < trafficLightsArray.length; i++) {
            System.out.println("Traffic light " + (i+1) + " weights: " + trafficLightsArray[i].getEmergencyWeight() + ", " + trafficLightsArray[i].getRegularWeight());
            if (clique.contains(i)) {
                trafficLightsArray[i].setOn(true);
                trafficLightWebSocketHandler.sendTrafficLightUpdate(i+1, true);
                trafficLightsArray[i].startDequeue(lanesMap, lanes);
            }
            else {
                trafficLightsArray[i].setOn(false);
                trafficLightWebSocketHandler.sendTrafficLightUpdate(i+1, false);
                trafficLightsArray[i].stopDequeue();
            }
        }
    }

    // Getters
    public Map<Lane, TrafficLight> getLaneToTrafficLightMap() { return this.laneToTrafficLightMap; }
    public int[][] getTrafficLightGraph() { return this.trafficLightGraph; }
    public TrafficLight[] getTrafficLightsArray() { return this.trafficLightsArray; }
    public Map<Integer, Integer[]> getLanesMap() { return this.lanesMap; }
    public Lane[] getLanes() { return this.lanes; }
    public Set<Integer> getDestinationLanes() { return this.destinationLanes; }
    public List<Integer> getEnteringLanes() { return this.enteringLanes; }

    // ToString
    @Override
    public String toString() {
        String str = "";

        // Traffic light Matrix
        str += "Traffic light matrix:\n";
        for (int i = 0; i < trafficLightGraph.length; i++) {
            for (int j = 0; j < trafficLightGraph[i].length; j++) {
                str += trafficLightGraph[i][j] + " ";
            }
            str += "\n";
        }
        str += "\n";
        // Traffic lights array
        str += "Traffic lights array:\n";
        for (TrafficLight light : trafficLightsArray) {
            str += light.toString() + "\n";
        }
        str += "\n";
        // Lanes map
        str += "Lanes map:\n";
        for(Map.Entry<Integer, Integer[]> entry : lanesMap.entrySet()) {
            Integer[] value = entry.getValue();
            str += "Lane " + entry.getKey() + " : ";
            if (value == null) {
                str += "null\n";
            }
            else {
                str += Arrays.toString(value) + "\n";
            }
        }
        str += "\n";
        // Lanes array
        str += "Lanes array:\n";
        for (Lane lane : lanes) {
            str += lane.toString() + "\n";
        }

        return str;
    }
}