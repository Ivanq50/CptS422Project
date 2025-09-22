/*
Finds all users in the database by username and driver's license
*/

// Created by : Yevin
// Created on : Sep 22

// Last Updated by : Yevin
// Last Updated on : Sep 22

package org.cpts422.carrentalapp.repo;

import org.cpts422.carrentalapp.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long>
{
    boolean existsByUsername(String username);
    boolean existsByDriversLicenseNumber(String driversLicenseNumber);
}

