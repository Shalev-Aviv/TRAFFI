package Shalev_Aviv.TRAFFI;
// TrafficLight class representing a specific traffic light at a junction
class TrafficLight {
    enum Color{ RED, GREEN }
    private Color color;
    private Lane[] lanes;
    private int weight;
    private int id;
	
    public TrafficLight(Lane[] lanes) {
        this.color = Color.RED;
        this.lanes = lanes;
        this.weight = 0;
        this.id = 0;
    }
    
    public Color getColor() { return color; }

    public int getWeight() { 
        weight = 0;
        for (Lane lane : lanes) {
            weight += lane.getLaneWeight();
        }
        return weight;
    }

    public void updateWeight() {

    }

    public void switchLight() {
        this.color = (color == Color.RED) ? Color.GREEN : Color.RED;
    }
}
