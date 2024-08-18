package com.example.demo.Validate.Anotations;

import com.example.demo.Validate.AtLeastOneFieldNotNullValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneFieldNotNullValidator.class)
@Documented
public @interface AtLeastOneFieldNotNull {
    String message() default "At least one of the fields must not be null";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] fields();
}
