package Shalev_Aviv.TRAFFI;

// Junction class representing the entire intersection
class Junction {
    private TrafficLight[] trafficLights; // Traffic lights at the intersection (junction)
    private Lane[] exitLanes; // Lanes that exit the intersection

    public Junction(TrafficLight[] trafficLights, Lane[] exitLanes) {
        this.trafficLights = trafficLights;
        this.exitLanes = exitLanes;
    }

    // Create a graph out of the json object

    // Create a dictionary of lanes

    public TrafficLight[] getTrafficLights() { return trafficLights; }
}