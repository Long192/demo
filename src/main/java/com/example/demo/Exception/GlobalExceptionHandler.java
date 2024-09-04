package com.example.demo.Exception;

import java.net.MalformedURLException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.demo.Dto.Response.CustomResponse;
import com.uploadcare.upload.UploadFailureException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;

@ControllerAdvice
public class GlobalExceptionHandler<T> {
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<CustomResponse<T>> handlingException(Exception exception) {
        CustomResponse<T> response = new CustomResponse<>();
        response.setStatus(400);
        response.setMessage(exception.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    ResponseEntity<CustomResponse<T>> handlingNoResourceFoundException() {
        CustomResponse<T> response = new CustomResponse<>();
        response.setStatus(404);
        response.setMessage("not found");
        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(value = ExpiredJwtException.class)
    ResponseEntity<CustomResponse<T>> handlingExpiredToken() {
        CustomResponse<T> response = new CustomResponse<>();
        response.setStatus(403);
        response.setMessage("token expired");
        return ResponseEntity.status(403).body(response);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<CustomResponse<T>> handlingMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        CustomResponse<T> response = new CustomResponse<>();
        response.setStatus(400);
        response.setMessage(exception.getAllErrors().getFirst().getDefaultMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = HandlerMethodValidationException.class)
    ResponseEntity<CustomResponse<T>> handlingHandlerMethodValidationException(
        HandlerMethodValidationException exception) {
        CustomResponse<T> response = new CustomResponse<>();
        response.setStatus(400);
        response.setMessage(exception.getAllErrors().getFirst().getDefaultMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = UploadFailureException.class)
    ResponseEntity<CustomResponse<T>> handlingUploadFailureException() {
        CustomResponse<T> response = new CustomResponse<>();
        response.setStatus(400);
        response.setMessage("failed to upload image");
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<CustomResponse<T>> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        CustomResponse<T> response = new CustomResponse<>();
        response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());
        response.setMessage("image to large");
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = MalformedURLException.class)
    ResponseEntity<CustomResponse<T>> handlingMalformedURLException(MalformedURLException exception) {
        CustomResponse<T> response = new CustomResponse<>();
        response.setStatus(400);
        response.setMessage(exception.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = SignatureException.class)
    ResponseEntity<CustomResponse<T>> handlingSignatureException() {
        CustomResponse<T> response = new CustomResponse<>();
        response.setStatus(400);
        response.setMessage("signature error");
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = NumberFormatException.class)
    ResponseEntity<CustomResponse<T>> handlingNumberFormatException() {
        CustomResponse<T> response = new CustomResponse<>();
        response.setStatus(400);
        response.setMessage("number format error");
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = CustomException.class)
    ResponseEntity<CustomResponse<T>> handlingCustomException(CustomException exception) {
        CustomResponse<T> response = new CustomResponse<>();
        response.setStatus(exception.getErrorCode());
        response.setMessage(exception.getMessage());
        return ResponseEntity.status(exception.getErrorCode()).body(response);
    }
}
