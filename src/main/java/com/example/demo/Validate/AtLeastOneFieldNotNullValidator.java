package com.example.demo.Validate;

import com.example.demo.Validate.Anotations.AtLeastOneFieldNotNull;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

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
        var inValid = true;
        for (String field : fields) {
            var property = beanWrapperImpl.getPropertyValue(field);
            if (property != null) {
                break;
            }

            if (property instanceof String) {
                if (!((String) property).isEmpty()) {
                    inValid = false;
                    break;
                }
            }

            if (property instanceof List) {
                if (!((List<?>) property).isEmpty()) {
                    inValid = false;
                    break;
                }
            }
        }
        return inValid;
    }
}
