package Shalev_Aviv.TRAFFI;
import java.util.*;
// Lane class representing a queue of cars in a specific lane
public class Lane {
    private Queue<Car> cars; // Queue of cars in the lane
    private int amount; // Amount of cars in the lane
    private int regulareCarsCounter; // Amount of regular cars in the lane
    private int emergencyCarsCounter; // Amount of emergency cars in the lane

    public Lane() {
        this.cars = new LinkedList<>();
        this.amount = 0;
        this.regulareCarsCounter = 0;
        this.emergencyCarsCounter = 0;
    }

    public void addCar(Car car) {
        cars.add(car);
        amount++;
        if(car.getEmergency()) {
            emergencyCarsCounter++;
        } else {
            regulareCarsCounter++;
        }
    }

    public void removeCar() {
        if (!cars.isEmpty()) {
            if(cars.remove().getEmergency()) {
                emergencyCarsCounter--;
            } else {
                regulareCarsCounter--;
            }
            amount--;
        }
    }

    public Queue<Car> getCars() { return cars; }
    public int getAmount() { return amount; }
    public int getRegularCarsCounter() { return regulareCarsCounter; }
    public int getEmergencyCarsCounter() { return emergencyCarsCounter; }
}