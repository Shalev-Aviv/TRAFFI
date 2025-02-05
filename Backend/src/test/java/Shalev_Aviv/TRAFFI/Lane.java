package Shalev_Aviv.TRAFFI;
import java.util.*;
// Lane class representing a queue of cars in a specific lane
public class Lane {
    private Queue<Car> cars;
    private int amount;
    private int sumWeight;

    public Lane() {
        this.cars = new LinkedList<>();
        this.amount = 0;
        this.sumWeight = 0;
    }

    public void addCar(Car car) {
        cars.add(car);
        amount++;
        sumWeight += car.getWeight();
    }
    public void removeCar() {
        if (!cars.isEmpty()) {
            amount--;
            sumWeight -= cars.poll().getWeight();
        }
    }

    public int getSumWeight() { return sumWeight; }
    public Queue<Car> getCars() { return cars; }
}