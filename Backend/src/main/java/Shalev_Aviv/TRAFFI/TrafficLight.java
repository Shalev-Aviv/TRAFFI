package Shalev_Aviv.TRAFFI;
// TrafficLight class representing the traffic light at a lane
class TrafficLight {
<<<<<<< HEAD
    enum Color{ RED, GREEN }
    private Color color;
    private Lane[] lanes;
    private int weight;
    private int id;

    public TrafficLight(Lane[] lanes) {
        this.color = Color.RED;
=======
    enum LightState { RED, GREEN }
    private LightState state;
    private Lane[] lanes;
    private int weight;

    public TrafficLight(Lane[] lanes) {
        this.state = LightState.RED;
>>>>>>> 769d958298c125492e3f93b6ff107001c147b078
        this.lanes = lanes;
        this.weight = 0;
    }

    public int getWeight() {
        for (Lane lane : lanes) {

        }
        return weight;
    }

    public void switchLight() {
<<<<<<< HEAD
        this.color = (color == Color.RED) ? Color.GREEN : Color.RED;
    }

    public Color getColor() { return color; }
=======
        this.state = (state == LightState.RED) ? LightState.GREEN : LightState.RED;
    }

    public LightState getState() { return state; }
>>>>>>> 769d958298c125492e3f93b6ff107001c147b078
}
