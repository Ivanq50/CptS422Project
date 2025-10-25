package org.cpts422.carrentalapp.service.error;
public class VehicleUnavailableException extends RuntimeException {
    public VehicleUnavailableException(Long id) { super("Vehicle is not available: " + id); }
}
