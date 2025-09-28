package org.cpts422.carrentalapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false)
    private LocalDate rentedAt;

    @Column(nullable = false)
    private LocalDate expectedReturnAt;

    private LocalDate returnedAt;

    @Column(nullable = false)
    private double totalCharge;

    private Integer daysPaid;
    private Double penaltyCharge;

    private LocalDateTime rentedAtDateTime;   // when the rental actually started
    private LocalDateTime returnedAtDateTime; // when the user actually returned

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public LocalDate getRentedAt() { return rentedAt; }
    public void setRentedAt(LocalDate rentedAt) { this.rentedAt = rentedAt; }
    public LocalDate getExpectedReturnAt() { return expectedReturnAt; }
    public void setExpectedReturnAt(LocalDate expectedReturnAt) { this.expectedReturnAt = expectedReturnAt; }
    public LocalDate getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDate returnedAt) { this.returnedAt = returnedAt; }
    public double getTotalCharge() { return totalCharge; }
    public void setTotalCharge(double totalCharge) { this.totalCharge = totalCharge; }
    public Integer getDaysPaid() { return daysPaid; }
    public void setDaysPaid(Integer daysPaid) { this.daysPaid = daysPaid; }
    public Double getPenaltyCharge() { return penaltyCharge; }
    public void setPenaltyCharge(Double penaltyCharge) { this.penaltyCharge = penaltyCharge; }
    public LocalDateTime getRentedAtDateTime() { return rentedAtDateTime; }
    public void setRentedAtDateTime(LocalDateTime dt) { this.rentedAtDateTime = dt; }
    public LocalDateTime getReturnedAtDateTime() { return returnedAtDateTime; }
    public void setReturnedAtDateTime(LocalDateTime dt) { this.returnedAtDateTime = dt; }
}
