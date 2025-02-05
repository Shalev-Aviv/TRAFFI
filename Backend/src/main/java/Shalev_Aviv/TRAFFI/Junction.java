package Shalev_Aviv.TRAFFI;
// Junction class representing the entire intersection
class Junction {
    private TrafficLight[] trafficLights;

    public Junction(TrafficLight[] trafficLights) {
        this.trafficLights = trafficLights;
    }

    public void switchLights() {
        for (TrafficLight light : trafficLights) {
            trafficLights.switchLight();
        }
    }

    public TrafficLight[] getTrafficLights() { return trafficLights; }
}
