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

import org.springframework.scheduling.annotation.Async;

// Junction class - representing the entire junction
public class Junction {
    private Map<Lane, TrafficLight> laneToTrafficLightMap; // Map of lanes to traffic lights

    private int[][] trafficLightGraph; // Graph representing the junction
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
        int maxWeight = 0;
        int index = 0;
        for (int i = 0; i < trafficLightsArray.length; i++) {
            if(trafficLightsArray[i].getEmergencyWeight() != maxEmergencyWeight) {
                continue;
            }

            // this happens only if the current light has the same emergency weight as the max
            if (trafficLightsArray[i].getRegularWeight() > maxWeight) {
                maxWeight = trafficLightsArray[i].getRegularWeight();
                index = i;
            }
        }
        return index;
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

            int laneIndex = this.enteringLanes.get(rand.nextInt(this.enteringLanes.size()));
            lanes[laneIndex].addCar(newCar);
            System.out.println("Added car to lane " + laneIndex + ", traffic light " + (laneToTrafficLightMap.get(lanes[laneIndex-1]).getId()));

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
        List<Integer> setList = new ArrayList<>(set);
        int maxWeight = trafficLightsArray[setList.get(0)].getRegularWeight();
        int index = setList.get(0);
        for (int i = 0; i < setList.size(); i++) {
            int current = setList.get(i);
            if (trafficLightsArray[current].getEmergencyWeight() == maxEmergencyWeight && trafficLightsArray[current].getRegularWeight() > maxWeight) {
                maxWeight = trafficLightsArray[current].getRegularWeight();
                index = current;
            }
        }
        return index;
    }
    /** finds the largest clique in the graph that contains the maximum weighted traffic light<p>
     * O(n^2 * m)<p>
     * n -> size of the clique<p>
     * m -> length of the <CODE>trafficLightsArray</CODE>
    */
    private Set<Integer> findLargestClique(int maxWeightIndex) {
        HashSet<Integer> clique = new HashSet<>();
        clique.add(maxWeightIndex);
        while(canWeAdd(clique)) {
            findStrongConnection(clique, maxWeightIndex);
            HashSet<Integer> temp = new HashSet<>();
            for(int i = 0; i < trafficLightsArray.length; i++) {
                if(clique.contains(i)) {
                    continue;
                }
                if(canWeAddThis(clique, i)) {
                    temp.add(i);
                }
            }
            if(temp.isEmpty()) {
                break;
            }
            else {
                int candidate = maxWeightIndexFromSet(temp);
                if(clique.contains(candidate)) {
                    break;
                }
                clique.add(candidate);
            }
        }
        return clique;
    }
    /** return false if we found the largest clique, return true otherwise<p>
     * O(n^2)<p>
     * n -> the size of the clique*/
    private boolean canWeAdd(Set<Integer> clique) {
        for(int i = 0; i < trafficLightsArray.length; i++) {
            if(clique.contains(i)) {
                continue;
            }
            if(canWeAddThis(clique, i)) {
                return true;
            }
        }
        return false;
    }
    /** return true if we can add a trafficLightsArray[index] to the clique<p>
     * O(n)<p>
     * n -> the size of the clique
     * */
    private boolean canWeAddThis(Set<Integer> clique, int index) {
        for(Integer i : clique) {
            if(trafficLightGraph[index][i] == 0) {
                return false;
            }
        }
        return true;
    }
    /** finds if a traffic light has a strong connection with another traffic light, and if so - add it to the clique<p>
     * <STRONG>O(n*m)</STRONG><p>
     * n -> length of the matrix<p>
     * m -> size of the clique
     * */
    private void findStrongConnection(Set<Integer> clique, int index) {
        for(int i = 0; i < trafficLightGraph.length; i++) {
            if(trafficLightGraph[index][i] == 2) {
                if(canWeAddThis(clique, i)) {
                    clique.add(i);
                }
            }
        }
    }
    /** changes the lights of the traffic lights in the clique<p>
     * O(n)
     * n -> the amount of traffic lights in the junction
     * */
    private void changeLights(Set<Integer> clique) {
        for (int i = 0; i < trafficLightsArray.length; i++) {
            if (clique.contains(i)) {
                trafficLightsArray[i].setOn(true);
                trafficLightsArray[i].startDequeue(lanesMap, lanes);
            }
            else {
                trafficLightsArray[i].setOn(false);
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