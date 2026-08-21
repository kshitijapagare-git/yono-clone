package com.yono.bank.service;

import com.yono.bank.entity.Customer;
import com.yono.bank.exception.ResourceNotFoundException;
import com.yono.bank.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Customer create(Customer customer) {
        return customerRepository.save(customer);
    }

    public Customer get(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    public List<Customer> list() {
        return customerRepository.findAll();
    }

    public Customer update(Long id, Customer update) {
        Customer existing = get(id);
        existing.setFirstName(update.getFirstName());
        existing.setLastName(update.getLastName());
        existing.setEmail(update.getEmail());
        existing.setPhone(update.getPhone());
        existing.setStatus(update.getStatus());
        return customerRepository.save(existing);
    }

    public void delete(Long id) {
        Customer existing = get(id);
        customerRepository.delete(existing);
    }
}
