package Shalev_Aviv.TRAFFI;

import java.util.Map;

// Junction class - representing the entire junction
class Junction {
    private int[][] trafficLightGraph; // Graph representing the junction
    private TrafficLight[] trafficLightsArray; // Traffic lights at the junction
    private Map<Integer, Integer[]> lanesMap; // Map of lanes to destinations
    private Lane[] exitLanes; // Lanes that exit the intersection

    public Junction(int[][] trafficLightGraph, TrafficLight[] trafficLightsArray, Map<Integer, Integer[]> lanesMap, Lane[] exitLanes) {
        this.trafficLightGraph = trafficLightGraph;
        this.trafficLightsArray = trafficLightsArray;
        this.lanesMap = lanesMap;
        this.exitLanes = exitLanes;
    }

    public void updateJunction() {
        /*
         * This function updates the junction by:
         * 1. finding the traffic light with the maximun weight
         * 2. turn off all the lights that can't work simultaneously with the light with the maximum weight
         * 3. turn on the light with the maximum weight
         * 4. find the largest clique of lights that can work simultaneously with the light with the maximum weight
         * 5. turn on all the lights in the clique
         * 6. dequeue cars from the lanes of the lights that are turned off and enqueue them to the langes they go to (according to the lanes map)
         * 7. update the weights of the lights
         */
    }

    /**
     * @return the index of the traffic light with the largest weight
     */ 
    public int MaxWeight() {
        int maxEmergencyWeight = MaximumEmergencyWeight();

        return MaximumRegularWeight(maxEmergencyWeight);
    }

    /**
     * @return an array of size 2, where the first element is the weight of the traffic light with the largest emergency weight, and the second element is the index of that light
     */
    private int MaximumEmergencyWeight() {
        int maxWeight = 0;
        for (int i = 0; i < trafficLightsArray.length; i++) {
            if (trafficLightsArray[i].getEmergencyWeight() > maxWeight) {
                maxWeight = trafficLightsArray[i].getEmergencyWeight();
            }
        }
        return maxWeight;
    }
    /**
     * @return the index of the traffic light with the largest regular weight among the lights with the same maximum emergency weight
     */
    private int MaximumRegularWeight(int maxEmergencyWeight) {
        int maxWeight = 0;
        int index = 0;
        for (int i = 0; i < trafficLightsArray.length; i++) {
            if(trafficLightsArray[i].getEmergencyWeight() != maxEmergencyWeight) continue;

            // this happens only if the current light has the same emergency weight as the max
            if (trafficLightsArray[i].getRegularWeight() > maxWeight) {
                maxWeight = trafficLightsArray[i].getRegularWeight();
                index = i;
            }
        }
        return index;
    }

    // Getters
    public TrafficLight[] getTrafficLightsArray() { return trafficLightsArray; }
    public Lane[] getExitLanes() { return exitLanes; }
}