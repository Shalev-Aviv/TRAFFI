package Shalev_Aviv.TRAFFI;

// TrafficLight class - representing a specific traffic light at a junction
class TrafficLight {
    public enum Color{ RED, GREEN }

    private Color color;
    private Lane[] lanes;
    private int emergencyWeight;
    private int regularWeight;
	
    public TrafficLight(Lane[] lanes) {
        this.color = Color.RED;
        this.lanes = lanes;
        setWeight();
    }
    
    public void setWeight() {
        this.emergencyWeight = 0;
        this.regularWeight = 0;
        for (Lane lane : lanes) {
            emergencyWeight += lane.getEmergencyCarsCounter();
            regularWeight += lane.getRegularCarsCounter();
        }
    }

    public void switchLight() {
        this.color = (color == Color.RED) ? Color.GREEN : Color.RED;
    }

    // Getters
    public Color getColor() { return this.color; }
    public int getEmergencyWeight() { return this.emergencyWeight; }
    public int getRegularWeight() { return this.regularWeight; }
}