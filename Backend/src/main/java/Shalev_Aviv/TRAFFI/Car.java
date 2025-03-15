package Shalev_Aviv.TRAFFI;

// Car class - representing a single vehicle in the simulation
public class Car {
    public enum CarType { PRIVATE, MOTORCYCLE, POLICE, AMBULANCE }

    private CarType type;
    private boolean emergency;

    // Default constructor
    public Car() {
        this(CarType.PRIVATE);
    }
    // Constructor with type
    public Car(CarType type) {
        this.type = type;
        setEmergency();
    }

    private void setEmergency() {
        emergency = switch (type) {
            case POLICE, AMBULANCE -> true;
            case PRIVATE, MOTORCYCLE -> false;
        };
        // Add default case to avoid compilation error
    }

    // Getters
    public CarType getType() { return type; }
    public boolean getEmergency() { return emergency; }

    // ToString
    @Override
    public String toString() {
        return "Car type: " + type + ", Emergency: " + emergency;
    }
}