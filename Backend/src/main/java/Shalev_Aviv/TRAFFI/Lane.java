package Shalev_Aviv.TRAFFI;
import java.util.*;

// Lane class - representing a queue of cars in a specific lane
public class Lane {
    private Queue<Car> cars; // Queue of cars in the lane
    private int amount; // Amount of cars in the lane
    private int regularCarsCounter; // Amount of regular cars in the lane
    private int emergencyCarsCounter; // Amount of emergency cars in the lane
    private int id; // Lane id

    public Lane(int id) {
        this.cars = new LinkedList<>();
        this.amount = 0;
        this.regularCarsCounter = 0;
        this.emergencyCarsCounter = 0;
        this.id = id;
    }

    /**
     * Add a car to the lane
     * @param car
     * @return void
     */
    public void addCar(Car car) {
        cars.add(car);
        this.amount++;
        if(car.getEmergency()) {
            this.emergencyCarsCounter++;
        } else {
            this.regularCarsCounter++;
        }
    }

    /**
     * Remove the first car from the lane (if any)
     * @return void
     */
    public void removeCar() {
        if (cars.isEmpty()) return;
        
        this.amount--;
        if(cars.remove().getEmergency()) {
            this.emergencyCarsCounter--;
        } else {
            this.regularCarsCounter--;
        }
    }

    public Queue<Car> getCars() { return this.cars; }
    public int getAmount() { return this.amount; }
    public int getRegularCarsCounter() { return this.regularCarsCounter; }
    public int getEmergencyCarsCounter() { return this.emergencyCarsCounter; }
    public int getId() { return this.id; }
}