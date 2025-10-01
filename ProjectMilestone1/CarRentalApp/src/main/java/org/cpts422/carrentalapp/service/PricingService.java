// Created by : Yevin
// Created on : Sep 28

// Last Updated by : Yevin
// Last Updated on : Sep 28

package org.cpts422.carrentalapp.service;

import org.cpts422.carrentalapp.model.*;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;

@Service
public class PricingService
{

    public double rentalTotal(AppUser user, Vehicle vehicle, int days)
    {
        double base = vehicle.getDailyRate() * days;

        // 10% off for premium
        if (user.getMembershipType() == MembershipType.PREMIUM) base *= 0.90;

        // +2% young renter fee (<25)
        if (user.getAge() < 25) base *= 1.02;

        return round2(base);
    }

    public double penaltyForReturn(Rental rental, LocalDateTime returningNow)
    {
        LocalDateTime rentedDT = rental.getRentedAtDateTime();
        if (rentedDT == null)
        {
            rentedDT = rental.getRentedAt().atStartOfDay();
        }

        long realDays = ChronoUnit.DAYS.between(rentedDT.toLocalDate(), returningNow.toLocalDate());

        LocalTime rentedTime = rentedDT.toLocalTime();
        LocalTime threshold = rentedTime.plusHours(2);
        if (returningNow.toLocalTime().isAfter(threshold))
        {
            realDays += 1;
        }

        long overdueDays = realDays - (rental.getDaysPaid() == null ? 0 : rental.getDaysPaid());

        if (rental.getUser().getMembershipType() == MembershipType.PREMIUM)
        {
            overdueDays = Math.max(0, overdueDays - 1);
        }

        if (overdueDays <= 0) return 0.0;

        double perDayPenalty = rental.getVehicle().getDailyRate() * 0.50;
        return round2(perDayPenalty * overdueDays);
    }

    public LocalDate today() { return LocalDate.now(); }
    public LocalDateTime now() { return LocalDateTime.now(); }

    private double round2(double x) { return Math.round(x * 100.0) / 100.0; }
}