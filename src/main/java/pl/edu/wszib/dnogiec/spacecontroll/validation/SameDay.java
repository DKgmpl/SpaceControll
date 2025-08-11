package pl.edu.wszib.dnogiec.spacecontroll.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SameDayValidator.class)
public @interface SameDay {
    String message() default "Rezerwacja musi mieścić się w obrębie jednego dnia.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
