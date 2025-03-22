package Shalev_Aviv.TRAFFI;

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