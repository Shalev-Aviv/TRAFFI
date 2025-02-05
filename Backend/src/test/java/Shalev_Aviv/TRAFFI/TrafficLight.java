package Shalev_Aviv.TRAFFI;
// TrafficLight class representing the traffic light at a lane
class TrafficLight {
    enum LightState { RED, GREEN }
    private LightState state;
    private Lane[] lanes;
    private int weight;

    public TrafficLight(Lane[] lanes) {
        this.state = LightState.RED;
        this.lanes = lanes;
        this.weight = 0;
    }

    public int getWeight() {
        for (Lane lane : lanes) {

        }
        return weight;
    }

    public void switchLight() {
        this.state = (state == LightState.RED) ? LightState.GREEN : LightState.RED;
    }

    public LightState getState() { return state; }
}
