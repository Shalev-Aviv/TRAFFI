package Shalev_Aviv.TRAFFI;
import java.util.concurrent.atomic.AtomicInteger;

// Car class - representing a single vehicle in the simulation
public class Car {
    public enum CarType { PRIVATE, MOTORCYCLE, POLICE, AMBULANCE }

    private static final AtomicInteger idCounter = new AtomicInteger(0);
    private final int id; // Unique ID for each car

    private CarType type;
    private boolean emergency;

    /** Default constructor*/
    public Car() {
        this(CarType.PRIVATE);
    }
    /** Constructor with type*/
    public Car(CarType type) {
        this.id = idCounter.incrementAndGet(); // Assign unique id
        this.type = type;
        setEmergency();
    }

    private void setEmergency() {
        this.emergency = switch (type) {
            case POLICE, AMBULANCE -> true;
            case PRIVATE, MOTORCYCLE -> false;
            default -> false; // Default case for safety
        };
    }

    // Getters
    public int getId() { return this.id; }
    public CarType getType() { return this.type; }
    public boolean getEmergency() { return this.emergency; }

    // ToString
    @Override
    public String toString() {
        return "Car id: " + id + ", type: " + type + ", emergency: " + emergency;
    }
}