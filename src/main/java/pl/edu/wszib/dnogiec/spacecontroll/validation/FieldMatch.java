package pl.edu.wszib.dnogiec.spacecontroll.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FieldMatchValidator.class)
public @interface FieldMatch {
    String first();
    String second();
    String message() default "Pola nie są takie same.";
    Class<?>[]groups() default {};
    Class<? extends Payload>[] payload() default {};
}
