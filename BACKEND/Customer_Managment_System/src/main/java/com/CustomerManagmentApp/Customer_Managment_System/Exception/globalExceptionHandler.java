package com.CustomerManagmentApp.Customer_Managment_System.Exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import com.CustomerManagmentApp.Customer_Managment_System.DTOs.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;




    @RestControllerAdvice
    public class globalExceptionHandler {

        @ExceptionHandler(CustomerNotFoundException.class)
        public ResponseEntity<ApiError> handleCustomerNotFound(CustomerNotFoundException ex) {
            ApiError error = new ApiError(
                    HttpStatus.NOT_FOUND.value(),
                    "Not Found",
                    ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(DuplicateEmailException.class)
        public ResponseEntity<ApiError> handleDuplicateEmail(DuplicateEmailException ex) {
            ApiError error = new ApiError(
                    HttpStatus.CONFLICT.value(),
                    "Conflict",
                    ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex) {
            List<String> details = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());

            ApiError error = new ApiError(
                    HttpStatus.BAD_REQUEST.value(),
                    "Validation Failed",
                    "Input validation failed. Please check the provided fields.",
                    details
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
            ApiError error = new ApiError(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleGenericException(Exception ex) {
            ApiError error = new ApiError(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal Server Error",
                    "An unexpected error occurred. Please try again later."
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


