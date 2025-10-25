package org.cpts422.carrentalapp.service.error;
public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(Long id) { super("Vehicle not found: " + id); }
}
