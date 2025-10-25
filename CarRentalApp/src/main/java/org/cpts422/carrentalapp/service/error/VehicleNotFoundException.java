package org.cpts422.carrentalapp.service.error;
public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(Object id) { super("Vehicle not found: " + id); }
}
