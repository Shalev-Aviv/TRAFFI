package Shalev_Aviv.TRAFFI;
import java.util.Arrays;
import java.util.Map;


// Junction class - representing the entire junction
class Junction {
    private int[][] trafficLightGraph; // Graph representing the junction
    private TrafficLight[] trafficLightsArray; // Traffic lights at the junction
    private Map<Integer, Integer[]> lanesMap; // Map of lanes to destinations

    public Junction(int[][] trafficLightGraph, TrafficLight[] trafficLightsArray, Map<Integer, Integer[]> lanesMap) {
        this.trafficLightGraph = trafficLightGraph;
        this.trafficLightsArray = trafficLightsArray;
        this.lanesMap = lanesMap;
    }

    /**
     * @return the index of the traffic light with the largest weight
     */ 
    public int MaxWeight() {
        return MaximumRegularWeight(MaximumEmergencyWeight());
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
    public int[][] getTrafficLightGraph() { return trafficLightGraph; }
    public TrafficLight[] getTrafficLightsArray() { return trafficLightsArray; }
    public Map<Integer, Integer[]> getLanesMap() { return lanesMap; }

    // ToString
    @Override
    public String toString() {
        String str = "";
        // Traffic light Matrix
        for (int i = 0; i < trafficLightGraph.length; i++) {
            for (int j = 0; j < trafficLightGraph[i].length; j++) {
                str += trafficLightGraph[i][j] + " ";
            }
            str += "\n";
        }
        // Traffic lights array
        for (TrafficLight light : trafficLightsArray) {
            str += "Traffic light " + light.getId() + " is " + light.getColor() + " with emergency weight " + light.getEmergencyWeight() + " and regular weight " + light.getRegularWeight() + "\n";
        }
        // Lanes map
        for(Map.Entry<Integer, Integer[]> entry : lanesMap.entrySet()) {
            Integer[] value = entry.getValue();
            str += "Lane " + entry.getKey() + " : ";
            if (value == null) {
                str += "null\n";
            } else {
                // Use Arrays.toString to get the content of the array
                str += Arrays.toString(value) + "\n";
            }
        }
        return str;
    }
}