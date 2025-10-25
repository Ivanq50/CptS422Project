package org.cpts422.carrentalapp.service.error;
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(double needed, double available) {
        super("Insufficient funds. Needed $" + needed + ", available $" + available);
    }
}
