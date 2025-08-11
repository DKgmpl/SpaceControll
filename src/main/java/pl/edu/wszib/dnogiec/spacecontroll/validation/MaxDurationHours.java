package pl.edu.wszib.dnogiec.spacecontroll.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaxDurationHoursValidator.class)
public @interface MaxDurationHours {
    String message() default "Czas trwania rezerwacji przekracza dozwolony limit.";
    long value(); // liczba godzin
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
