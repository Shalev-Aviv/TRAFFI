package Shalev_Aviv.TRAFFI;
import java.util.*;
// Car class representing a vehicle in the simulation
public class Car {
    enum CarType { PRIVATE, MOTORCYCLE, POLICE, AMBULANCE }

    private CarType type;
    private int weight;

    public Car(CarType type) {
        this.type = type;
        setWeight();
    }

    private void setWeight() {
        this.weight = switch (type) {
            case POLICE, AMBULANCE -> Integer.MAX_VALUE;
            case PRIVATE, MOTORCYCLE -> 1;
        };
    }

    public CarType getType() { return type; }
    public int getWeight() { return weight; }
}