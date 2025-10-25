package org.cpts422.carrentalapp.service.error;
public class DriversLicenseTakenException extends RuntimeException {
    public DriversLicenseTakenException(String dl) {
        super("Driver's license number already in use: " + dl);
    }
}
