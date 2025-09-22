/*
Used to carry and validate the user for the registration form
*/

// Created by : Yevin
// Created on : Sep 22

// Last Updated by : Yevin
// Last Updated on : Sep 22

package org.cpts422.carrentalapp.web.datatransfers;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.cpts422.carrentalapp.model.MembershipType;

import java.time.LocalDate;

public class RegistrationForm
{

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 32, message = "Username must be 3–32 characters")
    private String username;

    @NotNull(message = "Age is required")
    @Min(value = 21, message = "You must be 21 or older")
    private Integer age;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Driver’s license is required")
    private String driversLicenseNumber;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry must be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate driversLicenseExpiry;

    @NotNull(message = "Membership type is required")
    private MembershipType membershipType;

    // Getters & setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDriversLicenseNumber() { return driversLicenseNumber; }
    public void setDriversLicenseNumber(String driversLicenseNumber) { this.driversLicenseNumber = driversLicenseNumber; }

    public LocalDate getDriversLicenseExpiry() { return driversLicenseExpiry; }
    public void setDriversLicenseExpiry(LocalDate driversLicenseExpiry) { this.driversLicenseExpiry = driversLicenseExpiry; }

    public MembershipType getMembershipType() { return membershipType; }
    public void setMembershipType(MembershipType membershipType) { this.membershipType = membershipType; }
}