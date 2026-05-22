package com.CustomerManagmentApp.Customer_Managment_System.Exception;
public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(Long id) {
        super("Customer with ID " + id + " was not found");
    }
}
