package pl.edu.wszib.dnogiec.spacecontroll.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EndAfterStartValidator.class)
public @interface EndAfterStart {
String message() default "Czas zakończenia musi być po czasie rozpoczęcia.";
Class<?>[] groups() default {};
Class<? extends Payload>[] payload() default {};
}
