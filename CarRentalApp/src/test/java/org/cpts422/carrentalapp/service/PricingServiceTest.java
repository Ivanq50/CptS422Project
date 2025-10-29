package org.cpts422.carrentalapp.service;

import org.cpts422.carrentalapp.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;


public class PricingServiceTest {

    private PricingService pricing;

    @BeforeEach
    void setUp() {
        pricing = new PricingService();
    }

    @Test
    void rentalTotalPremiumUnder25() {
        AppUser user = new AppUser();
        user.setMembershipType(MembershipType.PREMIUM);
        user.setAge(22);

        Vehicle vehicle = new Vehicle();
        vehicle.setDailyRate(100);

        double total = pricing.rentalTotal(user, vehicle, 2);
        assertEquals(183.6, total);
    }

    @Test
    void rentalTotalPremium25Over() {
        AppUser user = new AppUser();
        user.setMembershipType(MembershipType.PREMIUM);
        user.setAge(30);

        Vehicle vehicle = new Vehicle();
        vehicle.setDailyRate(50);

        double total = pricing.rentalTotal(user, vehicle, 3);
        assertEquals(135.0, total);
    }

    @Test
    void rentalTotalStandardUnder25() {
        AppUser user = new AppUser();
        user.setMembershipType(MembershipType.STANDARD);
        user.setAge(20);

        Vehicle vehicle = new Vehicle();
        vehicle.setDailyRate(40);

        double total = pricing.rentalTotal(user, vehicle, 2);
        assertEquals(81.6, total);
    }

    @Test
    void rentalTotalStandard25Over() {
        AppUser user = new AppUser();
        user.setMembershipType(MembershipType.STANDARD);
        user.setAge(30);

        Vehicle vehicle = new Vehicle();
        vehicle.setDailyRate(60);

        double total = pricing.rentalTotal(user, vehicle, 1);
        assertEquals(60.0, total);
    }

    @Test
    void penaltyForReturnRentedOnTimeStandard() {
        AppUser user = new AppUser();
        user.setMembershipType(MembershipType.STANDARD);

        Vehicle vehicle = new Vehicle();
        vehicle.setDailyRate(100);

        Rental rental = new Rental();
        rental.setRentedAt(LocalDate.now());
        rental.setDaysPaid(1);
        rental.setUser(user);
        rental.setVehicle(vehicle);

        LocalDateTime returningNow = LocalDateTime.now();

        double penalty = pricing.penaltyForReturn(rental, returningNow);
        assertEquals(0.0, penalty);
    }

    @Test
    void penaltyForReturnRentedLateStandard() {
        AppUser user = new AppUser();
        user.setMembershipType(MembershipType.STANDARD);

        Vehicle vehicle = new Vehicle();
        vehicle.setDailyRate(50);

        Rental rental = new Rental();
        rental.setRentedAtDateTime(LocalDateTime.now().minusDays(3));
        rental.setDaysPaid(1);
        rental.setUser(user);
        rental.setVehicle(vehicle);

        LocalDateTime returningNow = LocalDateTime.now();

        double penalty = pricing.penaltyForReturn(rental, returningNow);
        assertEquals(50.0, penalty);
    }

    @Test
    void computeLatePenaltyForReturn() {
        AppUser user = new AppUser();
        user.setMembershipType(MembershipType.STANDARD);

        Vehicle vehicle = new Vehicle();
        vehicle.setDailyRate(100);

        Rental rental = new Rental();
        rental.setRentedAtDateTime(LocalDateTime.now().minusDays(2));
        rental.setDaysPaid(1);
        rental.setUser(user);
        rental.setVehicle(vehicle);

        double penalty = pricing.computeLatePenalty(rental, user, LocalDateTime.now());
        assertTrue(penalty >= 0.0);
    }

    @Test
    void todayAndNow() {
        assertNotNull(pricing.today());
        assertNotNull(pricing.now());
    }

    @Test
    void penaltyForReturnPremiumUserGraceDayApplied() {
        AppUser user = new AppUser();
        user.setMembershipType(MembershipType.PREMIUM);

        Vehicle vehicle = new Vehicle();
        vehicle.setDailyRate(100);

        Rental rental = new Rental();
        rental.setRentedAtDateTime(LocalDateTime.now().minusDays(4));
        rental.setDaysPaid(2);
        rental.setUser(user);
        rental.setVehicle(vehicle);

        LocalDateTime returningNow = LocalDateTime.now();

        double penalty = pricing.penaltyForReturn(rental, returningNow);

        assertEquals(50.0, penalty);
    }

    @Test
    void penaltyForReturnDaysPaidIsNull() {
        AppUser user = new AppUser();
        user.setMembershipType(MembershipType.STANDARD);

        Vehicle vehicle = new Vehicle();
        vehicle.setDailyRate(100);

        Rental rental = new Rental();
        rental.setRentedAtDateTime(LocalDateTime.now().minusDays(3));
        rental.setDaysPaid(null); // <-- trigger the null branch
        rental.setUser(user);
        rental.setVehicle(vehicle);

        LocalDateTime returningNow = LocalDateTime.now();

        double penalty = pricing.penaltyForReturn(rental, returningNow);
        assertEquals(150.0, penalty);
    }


}
