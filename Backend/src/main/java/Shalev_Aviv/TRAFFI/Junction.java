package Shalev_Aviv.TRAFFI;

import Shalev_Aviv.TRAFFI.Service.TrafficLightComparator;
import Shalev_Aviv.TRAFFI.Service.TrafficLightMaxHeap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.BitSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import Shalev_Aviv.TRAFFI.WebSocket.CarWebSocketHandler;
import Shalev_Aviv.TRAFFI.WebSocket.TrafficLightWebSocketHandler;

// Junction class - representing the entire junction
public class Junction {
    private BitSet[] trafficLightsConnections; // Array of bits representing the connections between traffic lights
    private BitSet[] trafficLightsStrongConnections; // Array of bits representing the strong connections between traffic lights
    private TrafficLight[] trafficLightsArray; // Traffic lights at the junction
    private Map<Integer, Integer[]> lanesMap; // Map of lanes to destinations
    private Lane[] lanes; // Lanes at the junction
    private Set<Integer> destinationLanes; // Set of destination lanes
    private List<Integer> enteringLanes; // List of entering lanes
    private static final int MAX_GREEN_DURATION = 5; // Maximum green durations for each traffic light
    private volatile boolean isPaused = false; // Pause flag
    private TrafficLightMaxHeap trafficLightHeap; // Max-heap

    /** Constructor*/
    public Junction(BitSet[] trafficLightsConnections, BitSet[] trafficLightsStrongConnections, TrafficLight[] trafficLightsArray, Map<Integer, Integer[]> lanesMap, Lane[] lanes) {
        this.trafficLightsConnections = trafficLightsConnections;
        this.trafficLightsStrongConnections = trafficLightsStrongConnections;
        this.trafficLightsArray = trafficLightsArray;
        this.lanesMap = lanesMap;
        this.lanes = lanes;

        // Set the junction reference in each lane
        for (Lane lane : lanes) {
            lane.setJunction(this);
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
        
        // Initialize the max-heap
        this.trafficLightHeap = new TrafficLightMaxHeap(trafficLightsArray);
    }

    /** return the index of the traffic light with the largest weight<p>
     * <STRONG>O(1)</STRONG>
    */
    public int maxWeightIndex() {
        TrafficLight max = trafficLightHeap.peek();
        return max.getId();
    }

    /** Update the locations of the max-heap based on the changed weights<p>
     * <STRONG>O(log n)</STRONG><p>
     * n -> length of <CODE>trafficLightsArray</CODE>
    */ 
    public void updateTrafficLightWeight(TrafficLight tl) {
        trafficLightHeap.updateWeight(tl);
    }

    @Autowired
    private CarWebSocketHandler webSocketHandler;
    // Setter for manual injection
    public void setCarsWebSocketHandler(CarWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    /** Async function that works infinitly <CODE>while(true)</CODE> with a <CODE>delay</CODE> seconds delay between each iteration<p>
     * Add 1 car to a random lane from the entering lanes<p>
     * <STRONG>O(1)</STRONG><p>
    */
    @Async
    public void addCarsAsync(int delay) {
        System.out.println("Starting to add cars asynchronously");

        Car.CarType[] carType = {
            Car.CarType.PRIVATE, Car.CarType.PRIVATE, Car.CarType.PRIVATE, Car.CarType.PRIVATE,
            Car.CarType.MOTORCYCLE, Car.CarType.MOTORCYCLE, Car.CarType.MOTORCYCLE, Car.CarType.MOTORCYCLE,
            Car.CarType.POLICE,
            Car.CarType.AMBULANCE
        };
        boolean running = true;
        while (running) {
            if (!isPaused) {  // Only add cars when not paused
                int randomIndex = TraffiApplication.rand.nextInt(carType.length);
                Car newCar = new Car(carType[randomIndex]);
                // Pick a random lane from the entering lanes (1-indexed)
                int laneId = this.enteringLanes.get(TraffiApplication.rand.nextInt(this.enteringLanes.size()));
                // Use (laneId - 1) when accessing the lanes array (which is 0-indexed)
                lanes[laneId - 1].addCar(newCar);
                // Send the update with the correct laneId
                webSocketHandler.sendCarUpdate(newCar.getId(), laneId, newCar.getType().toString());
            }
        
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

    /** Async function that works infinitly <CODE>while(true)</CODE> with a 5 seconds delay between each iteration<p>
    * Finds the maximum-weighted-traffic-light (MWTL), and finds the largest clique (based on weight) that the MWTL appears in, and set all traffic lights in the clique to green, and all the others to red<p>
     * <STRONG>O(n^3)</STRONG><p>
     * n -> length of <CODE>trafficLightsArray</CODE>
    */
    @Async
    public void manageTrafficLights() {
        boolean running = true;
        while (running) {
            if (!isPaused) {  // Only manage lights when not paused
                int maxWeightIndex = maxWeightIndex();
                System.out.println("Maximum-weighted-traffic-light: " + (maxWeightIndex + 1));
                BitSet clique = findLargestClique(maxWeightIndex);
                
                // Print the clique
                System.out.println("Clique:");
                for (int i = clique.nextSetBit(0); i >= 0; i = clique.nextSetBit(i+1)) {
                    System.out.print(i+1+", ");
                } System.out.println("\n");

                changeLights(clique);
                System.out.println("Iteration complete\n");
            }

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
     * n -> number of set bits in <CODE>set</CODE>
    */
    private int maxWeightIndexFromSet(BitSet set) {
        int maxIndex = 0;
        TrafficLight max = null;
        for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i + 1)) {
            TrafficLight tl = trafficLightsArray[i];
            if (max == null || new TrafficLightComparator().compare(tl, max) < 0) {
                max = tl;
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    /** finds the largest clique in the graph that contains the maximum weighted traffic light<p>
     * <STRONG>O(n^3)</STRONG><p>
     * n -> length of <CODE>trafficLightsArray</CODE>
    */
    private BitSet findLargestClique(int maxWeightIndex) {
        BitSet clique = new BitSet(trafficLightsArray.length);
        boolean stop = false;
        
        // Validate no light is suffering from starvation
        int greedy = findGreedyIfStarvation(maxWeightIndex);

        // Add the MWTL to the clique
        if(maxWeightIndex != greedy && canWeAddThis(clique, maxWeightIndex)) {
            clique.set(maxWeightIndex);
            // Find strong connecions with the MWTL (if any)
            findStrongConnection(clique, maxWeightIndex, greedy);
        }

        // Add all possible traffic lights to temp set
        BitSet temp = AddToTempSet(clique, greedy);

        do {
            if(temp.isEmpty()) {
                stop = true;
            }
            else {
                int candidate = maxWeightIndexFromSet(temp);
                if(clique.get(candidate)) {
                    stop = true;
                }
                else {
                    clique.set(candidate);
                    temp.clear(candidate);
                    findStrongConnection(clique, candidate, greedy);
                    removeFromTemp(clique, temp);
                }
            }

        } while(!stop);

        return clique;
    }

    /** Checks if the MWTL is greedy, and it causing a different traffic light to starve<p>
     * if so - return the index of the MWTL, else - return -1 (ignore)<p>
     * <STRONG>O(n)</STRONG><p>
     * n -> length of <CODE>trafficLightsArray</CODE>
    */
    private int findGreedyIfStarvation(int maxWeightIndex) {
        int res = -1;
        if(trafficLightsArray[maxWeightIndex].getGreenDuration() > MAX_GREEN_DURATION) {
            if(starving(maxWeightIndex)) res = maxWeightIndex;
        }
        return res;
    }

    /** Check if the greedy traffic light is starving other traffic lights<p>
     * <STRONG>O(n)</STRONG><p>
     * n -> length of <CODE>trafficLightsArray</CODE>
    */
    private boolean starving(int maxWeightIndex) {
        boolean flag = false;
        for(int i = 0; i < trafficLightsArray.length; i++) {
            if(i != maxWeightIndex && !trafficLightsConnections[maxWeightIndex].get(i)) {
                if(trafficLightsArray[i].getEmergencyWeight() > 0 || trafficLightsArray[i].getRegularWeight() > 0) {
                    flag = true;
                }
            }
        }
        return flag;
    }

    /**
     * Remove all the lights that can no longer work with the clique<p>
     * <STRONG>O(n^2)</STRONG><p>
     * n -> number of bits in <CODE>clique</CODE>
    */
    private void removeFromTemp(BitSet clique, BitSet temp) {
        for(int i = temp.nextSetBit(0); i >= 0; i = temp.nextSetBit(i+1)) {
            if(!canWeAddThis(clique, i)) {
                temp.clear(i);
            }
        }
    }

    /** Returns a set contains all the traffic light that can be added to the clique<p>
     * <STRONG>O(n^2)</STRONG><p>
     * n -> number of bits in <CODE>clique</CODE>
    */
    private BitSet AddToTempSet(BitSet clique, int greedy) {
        int n = trafficLightsArray.length;
        BitSet temp = new BitSet(n);
        for(int i = clique.nextClearBit(0); i >= 0 && i < n; i = clique.nextClearBit(i+1)) {
            if(i != greedy && canWeAddThis(clique, i)) {
                temp.set(i);
            }
        }
        return temp;
    }

    /** return true if we can add a trafficLightsArray[index] to the clique<p>
     * <STRONG>O(n)</STRONG><p>
     * n -> number of bits in <CODE>clique</CODE>
    */
    private boolean canWeAddThis(BitSet clique, int index) {
        boolean canWeAdd = true;
        for(int i = clique.nextSetBit(0); i >= 0 && canWeAdd; i = clique.nextSetBit(i+1)) {
            if(!trafficLightsConnections[index].get(i)) {
                canWeAdd = false;
            }
        }
        return canWeAdd;
    }
  
    /** finds if a traffic light has a strong connection with another traffic light, and if so - add it to the clique<p>
     * <STRONG>O(n^2)</STRONG><p>
     * n -> number of bits in <CODE>clique</CODE>
    */
    private void findStrongConnection(BitSet clique, int index, int greedy) {
        for(int i = trafficLightsStrongConnections[index].nextSetBit(0); i >= 0; i = trafficLightsStrongConnections[index].nextSetBit(i+1)) {
            if(!clique.get(i) && i != greedy && canWeAddThis(clique, i)) {
                clique.set(i);
            }
        }
    }
    
    @Autowired
    private TrafficLightWebSocketHandler trafficLightWebSocketHandler;
    // Setter for manual injection
    public void setTrafficLightWebSocketHandler(TrafficLightWebSocketHandler trafficLightWebSocketHandler) {
        this.trafficLightWebSocketHandler = trafficLightWebSocketHandler;
    }

    /** changes the lights of the traffic lights in the junction<p>
     * <STRONG>O(n)</STRONG><p>
     * n -> length of <CODE>trafficLightArray</CODE>
    */
    private void changeLights(BitSet clique) {
        for (int i = 0; i < trafficLightsArray.length; i++) {
            System.out.println("Traffic light " + (i+1) + " weights: " + trafficLightsArray[i].getEmergencyWeight() + ", " + trafficLightsArray[i].getRegularWeight());
            boolean flag = clique.get(i);
            
            trafficLightsArray[i].setColor(flag);
            trafficLightWebSocketHandler.sendTrafficLightUpdate(i+1, flag);
            
            if(flag) {
                trafficLightsArray[i].setGreenDuration(trafficLightsArray[i].getGreenDuration()+1);
                trafficLightsArray[i].startDequeue(lanesMap, lanes);
            }
            else {
                trafficLightsArray[i].setGreenDuration(0);
                trafficLightsArray[i].stopDequeue();
            }
        }
    }

    public void setIsPaused(boolean paused) { this.isPaused = paused; }
    
    // Getters
    public BitSet[] getTrafficLightsConnections() { return this.trafficLightsConnections;}
    public BitSet[] getTrafficLightsStrongConnections() { return this.trafficLightsStrongConnections; }
    public TrafficLight[] getTrafficLightsArray() { return this.trafficLightsArray; }
    public Map<Integer, Integer[]> getLanesMap() { return this.lanesMap; }
    public Lane[] getLanes() { return this.lanes; }
    public Set<Integer> getDestinationLanes() { return this.destinationLanes; }
    public List<Integer> getEnteringLanes() { return this.enteringLanes; }
    public boolean getIsPaused() { return this.isPaused; }

    // ToString
    @Override
    public String toString() {
        String str = "";

        // Traffic light Matrix
        str += "Traffic light matrix:\n";
        for (int i = 0; i < trafficLightsConnections.length; i++) {
            str += trafficLightsConnections[i].toString() + "\n";
        } str += "\n";

        // Traffic light strong connections matrix
        str += "Traffic light strong connections matrix:\n";
        for (int i = 0; i < trafficLightsStrongConnections.length; i++) {
            str += trafficLightsStrongConnections[i].toString() + "\n";
        } str += "\n";

        // Traffic lights array
        str += "Traffic lights array:\n";
        for (TrafficLight light : trafficLightsArray) {
            str += light.toString() + "\n";
        } str += "\n";
        
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
        } str += "\n";
        
        // Lanes array
        str += "Lanes array:\n";
        for (Lane lane : lanes) {
            str += lane.toString() + "\n";
        }

        return str;
    }
}
