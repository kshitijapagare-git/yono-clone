package com.yono.bank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yono.bank.entity.Customer;
import com.yono.bank.entity.Customer.IdType;
import com.yono.bank.entity.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Customer newCustomer() {
        Customer customer = new Customer();
        customer.setFirstName("Jane");
        customer.setLastName("Doe");
        customer.setEmail("jane.doe@example.com");
        customer.setPhone("9998887777");
        customer.setStatus(Status.ACTIVE);
        customer.setIdNumber("ID123456789");
        customer.setIdType(IdType.NATIONAL_ID);
        customer.setDateOfBirth(LocalDate.now().minusDays(30));
        return customer;
    }

    @Test
    void createsGetsListsUpdatesAndDeletesCustomer() throws Exception {
        String response = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCustomer())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("jane.doe@example.com"))
                .andExpect(jsonPath("$.idNumber").value("ID123456789"))
                .andExpect(jsonPath("$.idType").value("NATIONAL_ID"))
                .andExpect(jsonPath("$.dateOfBirth").value(LocalDate.now().minusDays(30).toString()))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.idNumber").value("ID123456789"))
                .andExpect(jsonPath("$.idType").value("NATIONAL_ID"))
                .andExpect(jsonPath("$.dateOfBirth").value(LocalDate.now().minusDays(30).toString()));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        Customer update = newCustomer();
        update.setFirstName("Janet");
        update.setStatus(Status.INACTIVE);
        update.setIdNumber("ID987654321");
        update.setIdType(IdType.PASSPORT);
        update.setDateOfBirth(LocalDate.now().minusDays(10));

        mockMvc.perform(put("/api/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Janet"))
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.idNumber").value("ID987654321"))
                .andExpect(jsonPath("$.idType").value("PASSPORT"))
                .andExpect(jsonPath("$.dateOfBirth").value(LocalDate.now().minusDays(10).toString()));

        mockMvc.perform(delete("/api/customers/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/customers/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void returns400ForMissingAndInvalidKycFieldsOnCreate() throws Exception {
        Customer valid = newCustomer();

        Customer missingIdNumber = newCustomer();
        missingIdNumber.setIdNumber(null);
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingIdNumber)))
                .andExpect(status().isBadRequest());

        Customer blankIdNumber = newCustomer();
        blankIdNumber.setIdNumber("   ");
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blankIdNumber)))
                .andExpect(status().isBadRequest());

        Customer tooLongIdNumber = newCustomer();
        tooLongIdNumber.setIdNumber("123456789012345678901");
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tooLongIdNumber)))
                .andExpect(status().isBadRequest());

        Customer missingIdType = newCustomer();
        missingIdType.setIdType(null);
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingIdType)))
                .andExpect(status().isBadRequest());

        Customer missingDateOfBirth = newCustomer();
        missingDateOfBirth.setDateOfBirth(null);
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingDateOfBirth)))
                .andExpect(status().isBadRequest());

        Customer futureDateOfBirth = newCustomer();
        futureDateOfBirth.setDateOfBirth(LocalDate.now().plusDays(1));
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(futureDateOfBirth)))
                .andExpect(status().isBadRequest());

        // sanity: valid payload should still work
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valid)))
                .andExpect(status().isCreated());
    }

    @Test
    void returns400ForMissingAndInvalidKycFieldsOnUpdate() throws Exception {
        String response = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCustomer())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        Customer updateMissingIdNumber = newCustomer();
        updateMissingIdNumber.setIdNumber(null);
        mockMvc.perform(put("/api/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMissingIdNumber)))
                .andExpect(status().isBadRequest());

        Customer updateMissingIdType = newCustomer();
        updateMissingIdType.setIdType(null);
        mockMvc.perform(put("/api/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMissingIdType)))
                .andExpect(status().isBadRequest());

        Customer updateMissingDateOfBirth = newCustomer();
        updateMissingDateOfBirth.setDateOfBirth(null);
        mockMvc.perform(put("/api/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMissingDateOfBirth)))
                .andExpect(status().isBadRequest());

        Customer updateFutureDateOfBirth = newCustomer();
        updateFutureDateOfBirth.setDateOfBirth(LocalDate.now().plusDays(2));
        mockMvc.perform(put("/api/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateFutureDateOfBirth)))
                .andExpect(status().isBadRequest());
    }
}
