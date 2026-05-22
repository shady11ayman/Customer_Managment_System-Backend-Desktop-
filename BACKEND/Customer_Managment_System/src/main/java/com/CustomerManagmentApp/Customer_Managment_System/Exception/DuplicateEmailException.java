package com.CustomerManagmentApp.Customer_Managment_System.Exception;


public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("A customer with email '" + email + "' already exists");
    }
}
