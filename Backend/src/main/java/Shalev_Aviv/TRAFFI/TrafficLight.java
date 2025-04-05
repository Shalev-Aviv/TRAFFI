package Shalev_Aviv.TRAFFI;

import java.util.Map;
import java.util.Random;

import org.springframework.scheduling.annotation.Async;

// TrafficLight class - representing a specific traffic light at a junction
class TrafficLight {
    private volatile boolean isDequeuing = false;
    public enum Color{ RED, GREEN }

    private final Object weightLock = new Object();

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
        synchronized(weightLock) {
            this.emergencyWeight += delta;
            if(this.emergencyWeight < 0) this.emergencyWeight = 0;
        }
    }
    /** Increment the regular weight of the traffic light*/
    public void incrementRegularWeight(int delta) {
        synchronized(weightLock) {
            this.regularWeight += delta;
            if(this.regularWeight < 0) this.regularWeight = 0;
        }
    }

    /** Start dequeuing cars from every lane in the lanes array<p>
     * <STRONG>O(n)</STRONG<p>
     * n -> <CODE>lanes.length</CODE><p>
     */
    @Async
    public void startDequeue(Map<Integer, Integer[]> lanesMap, Lane[] lanes) {
        if (isDequeuing || this.color != Color.GREEN) {
            return;
        }
        isDequeuing = true;
    
        new Thread(() -> {
            while (isDequeuing) {
                Random rand = new Random();
                // Only dequeue from this traffic light's lanes, not all lanes
                for (Lane lane : this.lanes) {  // Changed from 'lanes' to 'this.lanes'
                    if (this.color != Color.GREEN) {
                        break;
                    }
    
                    Car car = lane.removeCar(); // This decrements the weight
                    if (car == null) {
                        continue;
                    }
    
                    Integer[] destIds = lanesMap.get(lane.getId());
                    if (destIds == null || destIds.length == 0) {
                        continue;
                    }
    
                    int destId = destIds[rand.nextInt(destIds.length)];
                    if (destId > 0 && destId <= lanes.length) {
                        lanes[destId - 1].addCar(car); // This increments the weight in the new lane
                    }
                }
                try {
                    Thread.sleep(1000);
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
    public int getEmergencyWeight() {
        synchronized(weightLock) {
            return this.emergencyWeight;
        }
    }
    public int getRegularWeight() {
        synchronized(weightLock) {
            return this.regularWeight;
        }
    }
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