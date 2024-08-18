package com.example.demo.Validate;

import com.example.demo.Validate.Anotations.AtLeastOneFieldNotNull;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class AtLeastOneFieldNotNullValidator implements ConstraintValidator<AtLeastOneFieldNotNull, Object> {
    private String[] fields;

    @Override
    public void initialize(AtLeastOneFieldNotNull annotation) {
        ConstraintValidator.super.initialize(annotation);
        this.fields = annotation.fields();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        BeanWrapperImpl beanWrapperImpl = new BeanWrapperImpl(value);
        for (String field : fields) {
            Object property = beanWrapperImpl.getPropertyValue(field);
            return property != null && !property.toString().trim().isEmpty();
        }
        return false;
    }
}
