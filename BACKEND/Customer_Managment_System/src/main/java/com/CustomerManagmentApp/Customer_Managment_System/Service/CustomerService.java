package com.CustomerManagmentApp.Customer_Managment_System.Service;

import com.CustomerManagmentApp.Customer_Managment_System.DTOs.CustomerDTO;
import com.CustomerManagmentApp.Customer_Managment_System.DTOs.PagedResponse;
import com.CustomerManagmentApp.Customer_Managment_System.Entity.Customer;
import com.CustomerManagmentApp.Customer_Managment_System.Exception.CustomerNotFoundException;
import com.CustomerManagmentApp.Customer_Managment_System.Exception.DuplicateEmailException;
import com.CustomerManagmentApp.Customer_Managment_System.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers() {
        log.debug("Fetching all customers");
        return customerRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagedResponse<CustomerDTO> getCustomersPaged(int page, int size) {
        log.debug("Fetching customers - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Customer> customerPage = customerRepository.findAll(pageable);
        return toPagedResponse(customerPage);
    }

    @Transactional(readOnly = true)
    public List<CustomerDTO> searchCustomers(String query) {
        log.debug("Searching customers with query: {}", query);
        return customerRepository.searchCustomersAll(query)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagedResponse<CustomerDTO> searchCustomersPaged(String query, int page, int size) {
        log.debug("Searching customers with query: {}, page: {}, size: {}", query, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Customer> customerPage = customerRepository.searchCustomers(query, pageable);
        return toPagedResponse(customerPage);
    }

    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long id) {
        log.debug("Fetching customer with id: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return toDTO(customer);
    }

    @Transactional
    public CustomerDTO createCustomer(CustomerDTO dto) {
        log.debug("Creating customer with email: {}", dto.getEmail());
        if (customerRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException(dto.getEmail());
        }
        Customer customer = toEntity(dto);
        Customer saved = customerRepository.save(customer);
        log.info("Created customer with id: {}", saved.getId());
        return toDTO(saved);
    }

    @Transactional
    public CustomerDTO updateCustomer(Long id, CustomerDTO dto) {
        log.debug("Updating customer with id: {}", id);
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        if (customerRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new DuplicateEmailException(dto.getEmail());
        }

        existing.setName(dto.getName());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());

        Customer updated = customerRepository.save(existing);
        log.info("Updated customer with id: {}", updated.getId());
        return toDTO(updated);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        log.debug("Deleting customer with id: {}", id);
        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }
        customerRepository.deleteById(id);
        log.info("Deleted customer with id: {}", id);
    }

    private CustomerDTO toDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setCreatedAt(customer.getCreatedAt());
        return dto;
    }

    private Customer toEntity(CustomerDTO dto) {
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        return customer;
    }

    private PagedResponse<CustomerDTO> toPagedResponse(Page<Customer> page) {
        List<CustomerDTO> content = page.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
