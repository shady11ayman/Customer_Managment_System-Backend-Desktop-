package com.CustomerManagmentApp.Customer_Managment_System.Controller;

import com.CustomerManagmentApp.Customer_Managment_System.DTOs.CustomerDTO;
import com.CustomerManagmentApp.Customer_Managment_System.DTOs.PagedResponse;
import com.CustomerManagmentApp.Customer_Managment_System.Service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;


    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers(
            @RequestParam(required = false) String search) {

        if (search != null && !search.isBlank()) {
            log.debug("GET /customers?search={}", search);
            return ResponseEntity.ok(customerService.searchCustomers(search));
        }

        log.debug("GET /customers");
        return ResponseEntity.ok(customerService.getAllCustomers());
    }


    @GetMapping("/paged")
    public ResponseEntity<PagedResponse<CustomerDTO>> getCustomersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        if (size > 100) {
            throw new IllegalArgumentException("Page size must not exceed 100");
        }

        if (search != null && !search.isBlank()) {
            log.debug("GET /customers/paged?page={}&size={}&search={}", page, size, search);
            return ResponseEntity.ok(customerService.searchCustomersPaged(search, page, size));
        }

        log.debug("GET /customers/paged?page={}&size={}", page, size);
        return ResponseEntity.ok(customerService.getCustomersPaged(page, size));
    }


    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        log.debug("GET /customers/{}", id);
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }


    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CustomerDTO dto) {
        log.debug("POST /customers - name: {}", dto.getName());
        CustomerDTO created = customerService.createCustomer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerDTO dto) {
        log.debug("PUT /customers/{}", id);
        CustomerDTO updated = customerService.updateCustomer(id, dto);
        return ResponseEntity.ok(updated);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        log.debug("DELETE /customers/{}", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
