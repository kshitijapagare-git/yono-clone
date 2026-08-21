package com.yono.bank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yono.bank.entity.Account;
import com.yono.bank.entity.AccountType;
import com.yono.bank.entity.Customer;
import com.yono.bank.entity.Status;
import com.yono.bank.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        Customer c = new Customer();
        c.setFirstName("John");
        c.setLastName("Smith");
        c.setEmail("john.smith@example.com");
        c.setPhone("1234567890");
        c.setStatus(Status.ACTIVE);
        customer = customerRepository.save(c);
    }

    private Account newAccount() {
        Account account = new Account();
        account.setCustomer(customer);
        account.setAccountNumber("ACC-1001");
        account.setType(AccountType.SAVINGS);
        account.setBalance(new BigDecimal("500.00"));
        account.setStatus(Status.ACTIVE);
        return account;
    }

    @Test
    void createsGetsListsUpdatesAndDeletesAccount() throws Exception {
        String response = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAccount())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.accountNumber").value("ACC-1001"))
                .andExpect(jsonPath("$.customer.id").value(customer.getId()))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/accounts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("SAVINGS"));

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        Account update = newAccount();
        update.setType(AccountType.CURRENT);
        update.setBalance(new BigDecimal("1000.00"));

        mockMvc.perform(put("/api/accounts/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("CURRENT"))
                .andExpect(jsonPath("$.balance").value(1000.00));

        mockMvc.perform(delete("/api/accounts/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/accounts/{id}", id))
                .andExpect(status().isNotFound());
    }
}
