// Created by : Yevin
// Created on : Sep 22

// Last Updated by : Yevin
// Last Updated on : Sep 22

package org.cpts422.carrentalapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(
        name = "app_user",
        uniqueConstraints = {
                @UniqueConstraint(name = "user_username", columnNames = {"username"}),
                @UniqueConstraint(name = "user_dl", columnNames = {"drivers_license_number"})
        }
)
public class AppUser {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 32)
    private String username;

    // Store Hashed Password
    @Column(name = "password_hash", nullable = false, length = 72)
    private String passwordHash;

    @Column(name = "age", nullable = false)
    private int age;

    @Column(name = "drivers_license_number", nullable = false)
    private String driversLicenseNumber;

    @Column(name = "drivers_license_expiry", nullable = false)
    private LocalDate driversLicenseExpiry;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_type", nullable = false)
    private MembershipType membershipType = MembershipType.STANDARD;

    // getters/setters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getDriversLicenseNumber() { return driversLicenseNumber; }
    public void setDriversLicenseNumber(String driversLicenseNumber) { this.driversLicenseNumber = driversLicenseNumber; }
    public LocalDate getDriversLicenseExpiry() { return driversLicenseExpiry; }
    public void setDriversLicenseExpiry(LocalDate driversLicenseExpiry) { this.driversLicenseExpiry = driversLicenseExpiry; }
    public MembershipType getMembershipType() { return membershipType; }
    public void setMembershipType(MembershipType membershipType) { this.membershipType = membershipType; }
}
