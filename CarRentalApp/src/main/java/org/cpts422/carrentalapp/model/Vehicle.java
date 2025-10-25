package org.cpts422.carrentalapp.model;

import jakarta.persistence.*;

@Entity
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;

    @Column(nullable = false)
    private String make;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private double dailyRate;

    @Column(nullable = false)
    private boolean available = true;

    public Vehicle() {}

    public Vehicle(String make, String model, VehicleType vehicleType, double dailyRate, boolean available) {
        this.make = make;
        this.model = model;
        this.vehicleType = vehicleType;
        this.dailyRate = dailyRate;
        this.available = available;
    }

    // Getter and setter methods
    public boolean isAvailable() {
        return Boolean.TRUE.equals(this.available);
    }
    public boolean getAvailable() {
        return isAvailable();
    }

    public Long getId() { return id;}
    public void setId(Long id) { this.id = id; }
    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public double getDailyRate() { return dailyRate; }
    public void setDailyRate(double dailyRate) { this.dailyRate = dailyRate; }

    public void setAvailable(boolean available) { this.available = available; }
}