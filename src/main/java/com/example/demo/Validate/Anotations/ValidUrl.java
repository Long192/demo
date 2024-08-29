package com.example.demo.Validate.Anotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.example.demo.Validate.ValidUrlValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidUrlValidator.class)
@Documented
public @interface ValidUrl {
    String message() default "url invalid";

    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
