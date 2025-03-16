package Shalev_Aviv.TRAFFI.service;

import Shalev_Aviv.TRAFFI.Junction;
import Shalev_Aviv.TRAFFI.Car;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TrafficService {
    private Junction junction;

    public void setJunction(Junction junction) {
        this.junction = junction;
    }

    @Async
    public void addCarsAsync(int numberOfCars, int delay) {
        System.out.println("Starting to add " + numberOfCars + " cars asynchronously");
        for (int i = 0; i < numberOfCars; i++) {
            try {
                Thread.sleep(delay);
                Car.CarType[] carType = {
                    Car.CarType.PRIVATE, Car.CarType.PRIVATE, Car.CarType.PRIVATE,
                    Car.CarType.MOTORCYCLE, Car.CarType.MOTORCYCLE, Car.CarType.MOTORCYCLE,
                    Car.CarType.POLICE, Car.CarType.POLICE,
                    Car.CarType.AMBULANCE, Car.CarType.AMBULANCE
                };
                int random = (int) (Math.random() * 10);
                Car newCar = new Car(carType[random]);

                if (junction != null && junction.getLanes() != null) {
                    random = (int) (Math.random() * junction.getLanes().length);
                    junction.getLanes()[random].addCar(newCar);
                } else {
                    System.err.println("Error: Lanes array is not initialized in Junction.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Error adding car: " + e.getMessage());
            }
        }
        System.out.println("Finished adding cars asynchronously.");
    }
}
