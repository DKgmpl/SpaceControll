package pl.edu.wszib.dnogiec.spacecontroll.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;

import java.time.Duration;
import java.time.LocalDateTime;

public class MaxDurationHoursValidator implements ConstraintValidator<MaxDurationHours, Reservation> {
    private long maxHours;

    @Override
    public void initialize(MaxDurationHours constraintAnnotation) {
        this.maxHours = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Reservation value, ConstraintValidatorContext context) {
        if (value == null) return true;
        LocalDateTime start = value.getStartTime();
        LocalDateTime end = value.getEndTime();
        if (start == null || end == null) return true;
        long hours = Duration.between(start, end).toHours();
        return hours <= maxHours;
    }
}
