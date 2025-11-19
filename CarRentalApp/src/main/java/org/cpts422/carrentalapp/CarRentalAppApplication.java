package org.cpts422.carrentalapp;

import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.model.MembershipType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.time.LocalDate;

import org.cpts422.carrentalapp.model.Vehicle;
import org.cpts422.carrentalapp.model.VehicleType;
import org.cpts422.carrentalapp.repo.VehicleRepository;
import org.cpts422.carrentalapp.repo.AppUserRepository;




@SpringBootApplication
public class CarRentalAppApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(CarRentalAppApplication.class, args);
    }


    @Bean
    public CommandLineRunner loadData(VehicleRepository vehicleRepository, AppUserRepository appUserRepository, PasswordEncoder passwordEncoder ) {
        return args -> {
            vehicleRepository.save(new Vehicle("Toyota", "Corolla", VehicleType.ECONOMY, 50.0, true));
            vehicleRepository.save(new Vehicle("Honda", "Civic", VehicleType.SEDAN, 70.0, true));
            vehicleRepository.save(new Vehicle("BMW", "X5", VehicleType.LUXURY, 150.0, true));
            vehicleRepository.save(new Vehicle("Ford", "Explorer", VehicleType.SUV, 90.0, true));

            if (!appUserRepository.existsByUsername("testuser")) {
                AppUser testUser = new AppUser();
                testUser.setUsername("testuser");
                testUser.setPasswordHash(passwordEncoder.encode("password123"));                testUser.setDriversLicenseNumber("D12345678");
                testUser.setDriversLicenseExpiry(LocalDate.now().plusYears(5));
                testUser.setMembershipType(MembershipType.STANDARD);
                testUser.setWalletBalance(100.0);
                appUserRepository.save(testUser);
            }
        };
    }
}
