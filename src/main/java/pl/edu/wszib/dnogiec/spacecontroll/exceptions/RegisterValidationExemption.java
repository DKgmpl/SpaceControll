package pl.edu.wszib.dnogiec.spacecontroll.exceptions;

public class RegisterValidationExemption extends RuntimeException {
    public RegisterValidationExemption(String message) {
        super(message);
    }
}