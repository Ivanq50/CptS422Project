package org.cpts422.carrentalapp.model;

import jakarta.persistence.*;

@Entity
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String make;
    private String model;
    private int year;

    @Enumerated(EnumType.STRING)
    private VehicleClass vehicleClass; // ECONOMY, SEDAN, SUV, LUXURY

    private boolean available = true;
    private double dailyRate; // based on vehicleClass
    private int mileage;
}