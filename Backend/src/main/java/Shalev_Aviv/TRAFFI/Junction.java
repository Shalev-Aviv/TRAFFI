package Shalev_Aviv.TRAFFI;

// Junction class - representing the entire junction (intersection)
class Junction {
    private TrafficLight[] trafficLights; // Traffic lights at the intersection (junction)
    private Lane[] exitLanes; // Lanes that exit the intersection

    public Junction(TrafficLight[] trafficLights, Lane[] exitLanes) {
        this.trafficLights = trafficLights;
        this.exitLanes = exitLanes;
    }

    // Create a graph out of the json object

    // Create a dictionary of lanes


    /**
     * @return the index of the traffic light with the largest weight
     */ 
    public int FindMaxWeightLight() {
        // [0] - weight, [1] - index
        int[] maxWeight = MaximumEmergencyWeight();

        if(MultipleMaxWeights(maxWeight[0]) == 1) {
            return maxWeight[1];
        }

        return MaximumRegularWeight(maxWeight[0]);
    }

    /**
     * @return an array of size 2, where the first element is the weight of the traffic light with the largest emergency weight, and the second element is the index of that light
     */
    private int[] MaximumEmergencyWeight() {
        int[] maxWeight = {0, 0};
        for (int i = 0; i < trafficLights.length; i++) {
            if (trafficLights[i].getEmergencyWeight() > maxWeight[0]) {
                maxWeight[0] = trafficLights[i].getEmergencyWeight();
                maxWeight[1] = i;
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
        for (int i = 0; i < trafficLights.length; i++) {
            if(trafficLights[i].getEmergencyWeight() != maxEmergencyWeight) continue;

            // this happens only if the current light has the same emergency weight as the max
            if (trafficLights[i].getRegularWeight() > maxWeight) {
                maxWeight = trafficLights[i].getRegularWeight();
                index = i;
            }
        }
        return index;
    }
    /**
     * @param maxWeight
     * @return the amount of traffic lights with the same maximum emergency weight
     */
    private int MultipleMaxWeights(int maxWeight) {
        int counter = 0;
        for (TrafficLight trafficLight : trafficLights) {
            if (trafficLight.getEmergencyWeight() == maxWeight) {
                counter++;
            }
        }
        return counter;
    }

    // Getters
    public TrafficLight[] getTrafficLights() { return trafficLights; }
    public Lane[] getExitLanes() { return exitLanes; }
}