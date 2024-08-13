package com.example.demo.Exception;

import com.uploadcare.upload.UploadFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.demo.Dto.Response.ApiResponse;

import io.jsonwebtoken.ExpiredJwtException;

import java.net.MalformedURLException;

@ControllerAdvice
public class GlobalExceptionHandler<T> {
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<T>> handlingException(Exception exception) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(400);
        response.setMessage(exception.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    ResponseEntity<ApiResponse<T>> handlingNoResourceFoundException() {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(404);
        response.setMessage("not found");
        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(value = ExpiredJwtException.class)
    ResponseEntity<ApiResponse<T>> handlingExpiredToken() {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(403);
        response.setMessage("token expired");
        return ResponseEntity.status(403).body(response);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<T>> handlingMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(400);
        response.setMessage(exception.getAllErrors().getFirst().getDefaultMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = HandlerMethodValidationException.class)
    ResponseEntity<ApiResponse<T>> handlingHandlerMethodValidationException(
            HandlerMethodValidationException exception) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(400);
        response.setMessage(exception.getAllErrors().getFirst().getDefaultMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = UploadFailureException.class)
    ResponseEntity<ApiResponse<T>> handlingUploadFailureException() {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(401);
        response.setMessage("failed to upload image");
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = MalformedURLException.class)
    ResponseEntity<ApiResponse<T>> handlingMalformedURLException(
            MalformedURLException exception) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(400);
        response.setMessage(exception.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    ResponseEntity<ApiResponse<T>> handlingDataIntegrityViolationException() {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(400);
        response.setMessage("entity already exists");
        return ResponseEntity.badRequest().body(response);
    }
}
