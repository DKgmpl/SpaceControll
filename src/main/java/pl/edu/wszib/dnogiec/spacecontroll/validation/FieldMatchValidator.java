package pl.edu.wszib.dnogiec.spacecontroll.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {
    private String first;
    private String second;

    @Override
    public void initialize(FieldMatch anno) {
        this.first = anno.first();
        this.second = anno.second();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Object v1 = new BeanWrapperImpl(value).getPropertyValue(first);
        Object v2 = new BeanWrapperImpl(value).getPropertyValue(second);
        if (v1 == null && v2 == null) return true;
        return v1 != null && v1.equals(v2);
    }
}
