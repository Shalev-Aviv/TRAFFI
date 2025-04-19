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
        }
        else {
            this.regularCarsCounter++;
            if (parentTrafficLight != null) {
                parentTrafficLight.incrementRegularWeight(1);
            }
        }
    }

    /** Remove and return the first car from the lane and update the weights of the parent traffic light accordingly<p>
     * return null if <CODE>cars.isEmpty()</CODE>
    */
    public Car removeCar() {
        Car removedCar = null;
        
        if (!cars.isEmpty()) {
            removedCar = cars.poll();
            updateCountersAndWeights(removedCar);
        }
        
        return removedCar;
    }
    /** Update the counters and weights of the parent traffic light based on the car's type*/
    private void updateCountersAndWeights(Car car) {
        if (car != null) {
            boolean isEmergency = car.getEmergency();
            updateCarCounter(isEmergency);
            
            if (parentTrafficLight != null) {
                updateTrafficLightWeights(isEmergency);
            }
        }
    }
    /** Update the car counter based on whether the car is an emergency vehicle or not*/
    private void updateCarCounter(boolean isEmergency) {
        if (isEmergency) {
            this.emergencyCarsCounter--;
            this.emergencyCarsCounter = this.emergencyCarsCounter > 0 ? this.emergencyCarsCounter : 0; // Ensure non-negative count
        }
        else {
            this.regularCarsCounter--;
            this.regularCarsCounter = this.regularCarsCounter > 0 ? this.regularCarsCounter : 0; // Ensure non-negative count
        }
    }
    /** Update the weights of the parent traffic light based on whether the car is an emergency vehicle or not*/
    private void updateTrafficLightWeights(boolean isEmergency) {
        if (isEmergency) {
            parentTrafficLight.incrementEmergencyWeight(-1);
            int newWeight = Math.max(0, parentTrafficLight.getEmergencyWeight());
            parentTrafficLight.setEmergencyWeight(newWeight);
        }
        else {
            parentTrafficLight.incrementRegularWeight(-1);
            int newWeight = Math.max(0, parentTrafficLight.getRegularWeight());
            parentTrafficLight.setRegularWeight(newWeight);
        }
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