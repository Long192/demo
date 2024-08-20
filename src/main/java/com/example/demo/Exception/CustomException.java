package com.example.demo.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomException extends Exception {
    private int errorCode;

    public CustomException(Integer errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
    }
}
