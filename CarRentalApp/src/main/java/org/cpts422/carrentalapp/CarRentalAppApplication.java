package org.cpts422.carrentalapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.util.List;

import org.cpts422.carrentalapp.model.Vehicle;
import org.cpts422.carrentalapp.model.VehicleType;
import org.cpts422.carrentalapp.repo.VehicleRepository;



@SpringBootApplication
public class CarRentalAppApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(CarRentalAppApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadData(VehicleRepository vehicleRepository) {
        return args -> {
            vehicleRepository.save(new Vehicle("Toyota", "Corolla", VehicleType.ECONOMY, 50.0, true));
            vehicleRepository.save(new Vehicle("Honda", "Civic", VehicleType.SEDAN, 70.0, true));
            vehicleRepository.save(new Vehicle("BMW", "X5", VehicleType.LUXURY, 150.0, true));
            vehicleRepository.save(new Vehicle("Ford", "Explorer", VehicleType.SUV, 90.0, true));
        };
    }
}
