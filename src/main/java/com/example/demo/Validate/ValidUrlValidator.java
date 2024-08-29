package com.example.demo.Validate;

import java.net.URI;
import java.net.URISyntaxException;

import com.example.demo.Validate.Anotations.ValidUrl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidUrlValidator implements ConstraintValidator<ValidUrl, String> {

    @Override
    public boolean isValid(String url, ConstraintValidatorContext context) {
        if(url == null || url.isEmpty()) {
            return true;
        }
        
        try{
            new URI(url).parseServerAuthority();
            return true;
        }catch(URISyntaxException e){
            return false;
        }
    }

}
