package pl.edu.wszib.dnogiec.spacecontroll.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;

import java.time.LocalDateTime;

public class EndAfterStartValidator implements ConstraintValidator<EndAfterStart, Reservation> {
    @Override
    public boolean isValid(Reservation value, ConstraintValidatorContext context) {
        if (value == null) return true;
        LocalDateTime start = value.getStartTime();
        LocalDateTime end = value.getEndTime();
        if (start == null || end == null) return true;
        return end.isAfter(start);
    }
}
