package com.example.demo.Validate;

import org.springframework.beans.BeanWrapperImpl;

import com.example.demo.Interface.AtLeastOneFieldNotNull;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AtLeastOneFieldNotNullValidator
        implements ConstraintValidator<AtLeastOneFieldNotNull, Object> {
    private String[] fields;

    @Override
    public void initialize(AtLeastOneFieldNotNull annotation) {
        ConstraintValidator.super.initialize(annotation);
        this.fields = annotation.fields();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        BeanWrapperImpl beanWrapperImpl = new BeanWrapperImpl(value);

        for (String field: fields){
            Object property = beanWrapperImpl.getPropertyValue(field);
            return property != null && !property.toString().trim().isEmpty();
        }

        return false;
    }
}
