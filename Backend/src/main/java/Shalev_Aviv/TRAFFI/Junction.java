package Shalev_Aviv.TRAFFI;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.springframework.scheduling.annotation.Async;

import java.util.HashMap;
import java.util.HashSet;

// Junction class - representing the entire junction
public class Junction {
    private Map<Lane, TrafficLight> laneToTrafficLightMap; // Map of lanes to traffic lights

    private int[][] trafficLightGraph; // Graph representing the junction
    private TrafficLight[] trafficLightsArray; // Traffic lights at the junction
    private Map<Integer, Integer[]> lanesMap; // Map of lanes to destinations
    private Lane[] lanes; // Lanes at the junction

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
    }

    /** return the index of the traffic light with the largest weight*/ 
    public int maxWeightIndex() {
        return maximumRegularWeight(maximumEmergencyWeight());
    }
    /** return the maximum emergency weight among all traffic lights*/
    private int maximumEmergencyWeight() {
        int maxWeight = 0;
        for (int i = 0; i < trafficLightsArray.length; i++) {
            if (trafficLightsArray[i].getEmergencyWeight() > maxWeight) {
                maxWeight = trafficLightsArray[i].getEmergencyWeight();
            }
        }
        return maxWeight;
    }
    /** return the index of the traffic light with the largest regular weight among the lights with the same maximum emergency weight*/
    private int maximumRegularWeight(int maxEmergencyWeight) {
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

    /** Async function that adds cars to the lanes*/
    @Async
    public void addCarsAsync(int numberOfCars, int delay) {
        System.out.println("Starting to add " + numberOfCars + " cars asynchronously");
        for (int i = 0; i < numberOfCars; i++) {
            Car.CarType[] carType = {
                Car.CarType.PRIVATE, Car.CarType.PRIVATE, Car.CarType.PRIVATE,
                Car.CarType.MOTORCYCLE, Car.CarType.MOTORCYCLE, Car.CarType.MOTORCYCLE,
                Car.CarType.POLICE, Car.CarType.POLICE,
                Car.CarType.AMBULANCE, Car.CarType.AMBULANCE
            };
            int random = (int) (Math.random() * 10);
            Car newCar = new Car(carType[random]);
            random = (int) (Math.random() * lanes.length);
            lanes[random].addCar(newCar);
            /**/System.out.println("Added car to lane " + random);
            
            // Add delay
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Error adding cars: " + e.getMessage());
            }
        }
        /**/System.out.println("Finished adding cars asynchronously.");
    }

    /** Async function that gets the maximum-weighted-traffic-light (MWTL) and finds the largest clique (based on weight) that the MWTL appears in*/
    @Async
    public void manageTrafficLights() {
        Set<Integer> clique = new HashSet<>();
        largestClique(maxWeightIndex(), clique);
        
        // Print the clique
        System.out.println("Clique:\n");
        for (Integer i : clique) {
            System.out.print(i+1+", ");
        }
        changeLights(clique);
        /**/System.out.println("Changed lights to clique");
    }
    /** changes the lights of the traffic lights in the clique*/
    private void changeLights(Set<Integer> clique) {
        for (int i = 0; i < trafficLightsArray.length; i++) {
            if (clique.contains(i)) {
                trafficLightsArray[i].setOn(true);
            }
            else {
                trafficLightsArray[i].setOn(false);
            }
        }
    }
    /** finds the largest clique in the graph that contains the maximum weighted traffic light*/
    private void largestClique(int maxWeightIndex, Set<Integer> clique) {
        clique.add(maxWeightIndex);
        findStrongConnection(clique, maxWeightIndex);
        while(canWeAdd(clique)) {
            for(int i = 0; i < trafficLightsArray.length; i++) {
                if(clique.contains(i)) {
                    continue;
                }
                boolean flag = true;
                for(Integer j : clique) {
                    if(trafficLightGraph[i][j] == 0) {
                        flag = false;
                        break;
                    }
                }
                if(flag) {
                    clique.add(i);
                    findStrongConnection(clique, i);
                }
            }
        }
    }
    /** checks if we found the largest clique*/
    private boolean canWeAdd(Set<Integer> clique) {
        for(int i = 0; i < trafficLightsArray.length; i++) {
            if(clique.contains(i)) {
                continue;
            }
            boolean flag = canWeAdd(clique, i);
            if(flag) {
                return true;
            }
        }
        return false;
    }
    /** checks if we can add a traffic light to the clique*/
    private boolean canWeAdd(Set<Integer> clique, int index) {
        for(Integer i : clique) {
            if(trafficLightGraph[index][i] == 0) {
                return false;
            }
        }
        return true;
    }
    /** finds if a traffic light has a strong connection with another traffic light, and if so - add it to the clique*/
    private void findStrongConnection(Set<Integer> clique, int index) {
        for(int i = 0; i < trafficLightGraph.length; i++) {
            if(trafficLightGraph[index][i] == 2) {
                if(canWeAdd(clique, i)) {
                    clique.add(i);
                }
            }
        }
    }

    // Getters
    public Map<Lane, TrafficLight> getLaneToTrafficLightMap() { return laneToTrafficLightMap; }
    public int[][] getTrafficLightGraph() { return trafficLightGraph; }
    public TrafficLight[] getTrafficLightsArray() { return trafficLightsArray; }
    public Map<Integer, Integer[]> getLanesMap() { return lanesMap; }
    public Lane[] getLanes() { return lanes; }

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