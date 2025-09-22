/*
Used to carry and validate the user for the registration form
*/

// Created by : Yevin
// Created on : Sep 22

// Last Updated by : Yevin
// Last Updated on : Sep 22

package org.cpts422.carrentalapp.web.datatransfers;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import org.cpts422.carrentalapp.model.MembershipType;

public class RegistrationForm {
    @NotBlank @Size(min=3, max=32)
    public String username;

    @NotBlank @Size(min=6, max=72)
    public String password;

    @Min(0)
    public int age;

    @NotBlank
    public String driversLicenseNumber;

    @NotNull
    public LocalDate driversLicenseExpiry;

    @NotNull
    public MembershipType membershipType = MembershipType.STANDARD;
}
