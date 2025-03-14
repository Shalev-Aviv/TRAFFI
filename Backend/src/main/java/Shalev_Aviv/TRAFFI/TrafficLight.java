package Shalev_Aviv.TRAFFI;

// TrafficLight class - representing a specific traffic light at a junction
class TrafficLight {
    public enum Color{ RED, GREEN }

    private Color color;
    private Lane[] lanes;
    private int emergencyWeight;
    private int regularWeight;
    private int id;
	
    public TrafficLight(Lane[] lanes, int id) {
        this.color = Color.RED;
        this.lanes = lanes;
        this.emergencyWeight = 0;
        this.regularWeight = 0;
        this.id = id;
    }
    


    public void setWeight() {
        for (Lane lane : lanes) {
            this.emergencyWeight += lane.getEmergencyCarsCounter();
            this.regularWeight += lane.getRegularCarsCounter();
        }
    }

    public void turnOff() { this.color = Color.RED; }
    public void turnOn() { this.color = Color.GREEN; }


    // Getters
    public Color getColor() { return this.color; }
    public Lane[] getLanes() { return this.lanes; }
    public int getEmergencyWeight() { return this.emergencyWeight; }
    public int getRegularWeight() { return this.regularWeight; }
    public int getId() { return this.id; }
}