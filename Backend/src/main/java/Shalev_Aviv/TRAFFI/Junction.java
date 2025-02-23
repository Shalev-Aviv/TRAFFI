package Shalev_Aviv.TRAFFI;

// Junction class representing the entire intersection
class Junction {
    private TrafficLight[] trafficLights;

    public Junction(TrafficLight[] trafficLights) {
        this.trafficLights = trafficLights;
    }

    // Create a graph out of the json object

    public TrafficLight[] getTrafficLights() { return trafficLights; }
}