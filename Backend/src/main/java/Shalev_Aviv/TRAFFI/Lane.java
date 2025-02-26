package Shalev_Aviv.TRAFFI;
import java.util.*;

// Lane class - representing a queue of cars in a specific lane
public class Lane {
    private Queue<Car> cars; // Queue of cars in the lane
    private int amount; // Amount of cars in the lane
    private int regularCarsCounter; // Amount of regular cars in the lane
    private int emergencyCarsCounter; // Amount of emergency cars in the lane

    public Lane() {
        this.cars = new LinkedList<>();
        this.amount = 0;
        this.regularCarsCounter = 0;
        this.emergencyCarsCounter = 0;
    }

    /**
     * Add a car to the lane (Maximun cars per lane is 6)
     * @param car
     * @return void
     */
    public void addCar(Car car) {
        if(amount >= 6) return;
        cars.add(car);
        amount++;
        if(car.getEmergency()) {
            emergencyCarsCounter++;
        } else {
            regularCarsCounter++;
        }
    }

    /**
     * Remove the first car from the lane (if any)
     * @return void
     */
    public void removeCar() {
        if (!cars.isEmpty()) {
            amount--;
            if(cars.remove().getEmergency()) {
                emergencyCarsCounter--;
            } else {
                regularCarsCounter--;
            }
        }
    }

    public Queue<Car> getCars() { return cars; }
    public int getAmount() { return amount; }
    public int getRegularCarsCounter() { return regularCarsCounter; }
    public int getEmergencyCarsCounter() { return emergencyCarsCounter; }
}