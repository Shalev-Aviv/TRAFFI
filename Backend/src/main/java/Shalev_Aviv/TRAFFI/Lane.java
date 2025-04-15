package Shalev_Aviv.TRAFFI;
import java.util.*;

// Lane class - representing a queue of cars in a specific lane
public class Lane {
    private TrafficLight parentTrafficLight; // Reference to the parent traffic light

    private Queue<Car> cars; // Queue of cars in the lane
    private int regularCarsCounter; // Amount of regular cars in the lane
    private int emergencyCarsCounter; // Amount of emergency cars in the lane
    private int id; // Lane's id

    /** Constructor*/
    public Lane(int id) {
        this.cars = new LinkedList<>();
        this.regularCarsCounter = 0;
        this.emergencyCarsCounter = 0;
        this.id = id;
    }

    // Set the parent traffic light
    public void setParentTrafficLight(TrafficLight light) {
        this.parentTrafficLight = light;
    }

    /** Add a car to the lane and update the weights of the parent traffic light*/
    public void addCar(Car car) {
        cars.add(car);
        if(car.getEmergency()) {
            this.emergencyCarsCounter++;
            if (parentTrafficLight != null) {
                parentTrafficLight.incrementEmergencyWeight(1);
            }
        } else {
            this.regularCarsCounter++;
            if (parentTrafficLight != null) {
                parentTrafficLight.incrementRegularWeight(1);
            }
        }
    }
    /** Remove and return the first car from the lane, or null if the lane is empty, and update the weights of the parent traffic light accordingly*/
    public Car removeCar() {
        if (cars.isEmpty()) return null;
        
        Car removedCar = cars.poll();
        if(removedCar == null) {
            return null;
        }
        
        if(removedCar.getEmergency()) {
            this.emergencyCarsCounter--;
            if(this.emergencyCarsCounter < 0) this.emergencyCarsCounter = 0;
        }
        else {
            this.regularCarsCounter--;
            if(this.regularCarsCounter < 0) this.regularCarsCounter = 0;
        }

        if (parentTrafficLight != null) {
            if (removedCar.getEmergency()) {
                this.emergencyCarsCounter--;
                if (this.emergencyCarsCounter < 0) this.emergencyCarsCounter = 0;
                parentTrafficLight.incrementEmergencyWeight(-1);
                if(parentTrafficLight.getEmergencyWeight() < 0) parentTrafficLight.setEmergencyWeight(0);
            }
            else {
                this.regularCarsCounter--;
                if (this.regularCarsCounter < 0) this.regularCarsCounter = 0;
                parentTrafficLight.incrementRegularWeight(-1);
                if(parentTrafficLight.getRegularWeight() < 0) parentTrafficLight.setRegularWeight(0);
            }
        }
        return removedCar;
    }

    // Getters
    public TrafficLight getParentTrafficLight() { return this.parentTrafficLight; }
    public Queue<Car> getCars() { return this.cars; }
    public int getRegularCarsCounter() { return this.regularCarsCounter; }
    public int getEmergencyCarsCounter() { return this.emergencyCarsCounter; }
    public int getId() { return this.id; }

    // ToString
    @Override
    public String toString() {
        return "Lane " + this.id + ": " + 
        " regular cars: " + this.regularCarsCounter + 
        " emergency cars: " + this.emergencyCarsCounter;
    }
}