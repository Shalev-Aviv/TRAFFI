package Shalev_Aviv.TRAFFI;

import java.util.Map;
import java.util.Random;

import org.springframework.scheduling.annotation.Async;

// TrafficLight class - representing a specific traffic light at a junction
class TrafficLight {
    public enum Color{ RED, GREEN }
    
    private volatile boolean isDequeuing = false;
    private final Object weightLock = new Object();

    private Color color; // The color of the light (RED, GREEN)
    private Lane[] lanes; // Array of lanes that are controlled by this traffic light
    private int emergencyWeight; // Represents the emergency weight of this light
    private int regularWeight; // Represents the regular weight of this light
    private int id; // light's id
	
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
     * n -> length of <CODE>lanes</CODE><p>
    */
    @Async
    public void startDequeue(Map<Integer, Integer[]> lanesMap, Lane[] lanes) {
        if(!isDequeuing && this.color == Color.GREEN) {
            isDequeuing = true;
            new Thread(() -> {
                while(isDequeuing) {
                    Random rand = new Random();
                    for(Lane lane : this.lanes) {
                        if(this.color == Color.GREEN) {
                            Car car = lane.removeCar();
                            if(car != null) {
                                Integer[] destIds = lanesMap.get(lane.getId());
                                if(destIds != null && destIds.length > 0) {
                                    int destId = destIds[rand.nextInt(destIds.length)];
                                    if(destId > 0 && destId <= lanes.length) {
                                        lanes[destId - 1].addCar(car);
                                    }
                                }
                            }
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        isDequeuing = false;
                    }
                }
            }).start();
        }
    }

    /** stop dequeuing cars from every lane in the lanes array<p>
     * <STRONG>O(1)</STRONG
    */
    public void stopDequeue() {
        isDequeuing = false;
    }

    /** Turns the traffic light on (true) or off (false)*/
    public void setColor(boolean set) { this.color = set ? Color.GREEN : Color.RED; }
    public void setRegularWeight(int weight) { this.regularWeight = weight; }
    public void setEmergencyWeight(int weight) { this.emergencyWeight = weight; }
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