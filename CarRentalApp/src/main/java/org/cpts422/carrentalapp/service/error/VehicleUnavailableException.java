package org.cpts422.carrentalapp.service.error;
public class VehicleUnavailableException extends RuntimeException {
    public VehicleUnavailableException(Object id) { super("Vehicle not available: " + id); }
}
