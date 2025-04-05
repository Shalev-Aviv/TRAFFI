package Shalev_Aviv.TRAFFI;

import java.util.Map;
import java.util.Random;

import org.springframework.scheduling.annotation.Async;

// TrafficLight class - representing a specific traffic light at a junction
class TrafficLight {
    private volatile boolean isDequeuing = false;
    public enum Color{ RED, GREEN }

    private Color color;
    private Lane[] lanes;
    private int emergencyWeight;
    private int regularWeight;
    private int id;
	
    /** Constructor*/
    public TrafficLight(Lane[] lanes, int id) {
        this.color = Color.RED;
        this.lanes = lanes;
        this.emergencyWeight = 0;
        this.regularWeight = 0;
        this.id = id;
    }
    
    /** Increment the emergency weight of the traffic light*/
    public void incrementEmergencyWeight(int delta) {
        this.emergencyWeight += delta;
        if(this.emergencyWeight < 0) this.emergencyWeight = 0; // Prevent negative weights
    }
    /** Increment the regular weight of the traffic light*/
    public void incrementRegularWeight(int delta) {
        this.regularWeight += delta;
        if(this.regularWeight < 0) this.regularWeight = 0; // Prevent negative weights
    }

    /** Start dequeuing cars from every lane in the lanes array<p>
     * <STRONG>O(n)</STRONG<p>
     * n -> <CODE>lanes.length</CODE><p>
     */
    @Async
    public void startDequeue(Map<Integer, Integer[]> lanesMap, Lane[] lanes) {
        if (isDequeuing || this.color != Color.GREEN) {
            return; // Prevent starting multiple dequeue processes or dequeuing when the light is red
        }
        isDequeuing = true;
    
        new Thread(() -> {
            while (isDequeuing) {
                Random rand = new Random();
                for (Lane lane : lanes) {
                    if (this.color != Color.GREEN) {
                        break; // Stop dequeuing if the light turns red
                    }
    
                    Car car = lane.removeCar();
                    if (car == null) {
                        continue;
                    }
    
                    Integer[] destIds = lanesMap.get(lane.getId());
                    if (destIds == null || destIds.length == 0) {
                        continue;
                    }
    
                    int destId = rand.nextInt(destIds.length);
                    if (destId > 0 && destId <= lanes.length) {
                        lanes[destId - 1].addCar(car);
                    }
                }
                try {
                    Thread.sleep(1000); // Adjust the delay as needed
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    isDequeuing = false;
                    return;
                }
            }
        }).start();
    }

    /** stop dequeuing cars from every lane in the lanes array<p>
     * <STRONG>O(1)</STRONG
     */
    public void stopDequeue() {
        //System.out.println("stop dequeuing cars asynchronously from traffic light " + this.id);
        isDequeuing = false;
    }

    /** Create a function that turns the traffic light on or off*/
    public void setOn(boolean on) { this.color = on ? Color.GREEN : Color.RED; }

    // Getters
    public Color getColor() { return this.color; }
    public Lane[] getLanes() { return this.lanes; }
    public int getEmergencyWeight() { return this.emergencyWeight; }
    public int getRegularWeight() { return this.regularWeight; }
    public int getId() { return this.id; }

    // ToString
    @Override
    public String toString() {
        String str = "Traffic Light " + this.id + ": ";
        for (Lane lane : lanes) {
            str += lane.getId() + ", ";
        }
        str += this.color + " emergency weight: " + this.emergencyWeight + " regular weight: " + this.regularWeight;
        return str;
    }
}