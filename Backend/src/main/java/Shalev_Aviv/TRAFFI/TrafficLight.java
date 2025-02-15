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
		this.state = LightState.RED;
        this.lanes = lanes;
        this.weight = 0;
    }

    public int getWeight() {
        for (Lane lane : lanes) {
			// Do something
        }
        return weight;
    }

    public void switchLight() {
        this.color = (color == Color.RED) ? Color.GREEN : Color.RED;
    }

    public Color getColor() { return color; }
        this.state = (state == LightState.RED) ? LightState.GREEN : LightState.RED;
    }

    public LightState getState() { return state; }
}
