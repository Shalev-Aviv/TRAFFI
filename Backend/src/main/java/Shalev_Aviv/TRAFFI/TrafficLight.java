package Shalev_Aviv.TRAFFI;

import java.util.Map;

import org.springframework.scheduling.annotation.Async;

// TrafficLight class - representing a specific traffic light at a junction
class TrafficLight {
    public enum Color{ RED, GREEN }

    private Color color;
    private Lane[] lanes;
    private int emergencyWeight;
    private int regularWeight;
    private int id;
	
    // Constructor
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
    }
    /** Increment the regular weight of the traffic light*/
    public void incrementRegularWeight(int delta) {
        this.regularWeight += delta;
    }

    /** Start dequeuing cars from every lane in the lanes array<p>
     * <STRONG>O(n*m)</STRONG<p>
     * n -> <CODE>lanes.length</CODE><p>
     * m -> <CODE>while(true)</CODE>
     */
    @Async
    public void startDequeue(Map<Integer, Integer[]> lanesMap, Lane[] lanes) {
        for (Lane lane : lanes) {
            Car car = lane.getCars().poll();
            if (car == null) {
                continue;
            }
            Integer[] destIds = lanesMap.get(lane.getId());
            if (destIds != null && destIds.length > 0) {
                // Choose the first destination lane (assuming lane IDs are 1-indexed)
                int destId = destIds[0];
                Lane destLane = lanes[destId - 1];
                destLane.getCars().add(car);
            }
        }
    }
    /** stop dequeuing cars from every lane in the lanes array<p>
     * <STRONG>O(1)</STRONG
     */
    public void stopDequeue() {
        // Somehow terminate `startDequeue` operation
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