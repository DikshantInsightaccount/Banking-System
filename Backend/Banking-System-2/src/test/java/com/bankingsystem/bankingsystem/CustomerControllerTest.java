package com.bankingsystem.bankingsystem;

import com.bankingsystem.bankingsystem.entity.Customer;
import com.bankingsystem.bankingsystem.entity.CustomerStatus;
import com.bankingsystem.bankingsystem.entity.KycStatus;
import com.bankingsystem.bankingsystem.services.CustomerServiceImp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CustomerControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    CustomerServiceImp customerService;

    // -------------------- Customer CRUD --------------------

    @Test
    void testRegisterCustomer_success() throws Exception {
        Customer saved = new Customer();
        saved.setCustomerid(1L);

        when(customerService.registerCustomer(any(Customer.class))).thenReturn(saved);

        mvc.perform(post("/api/customers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullname\":\"A\",\"email\":\"a@b.com\",\"phone\":\"999\",\"address\":\"HYD\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerid").value(1));

        verify(customerService).registerCustomer(any(Customer.class));
    }

    @Test
    void testGetCustomerById_success() throws Exception {
        Customer customer = new Customer();
        customer.setCustomerid(1L);

        when(customerService.getCustomerById(1L)).thenReturn(customer);

        mvc.perform(get("/api/customers/{customerId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerid").value(1));

        verify(customerService).getCustomerById(1L);
    }

    @Test
    void testGetCustomerByUserId_success() throws Exception {
        Customer customer = new Customer();
        customer.setCustomerid(2L);

        when(customerService.getCustomerByUserId(101L)).thenReturn(customer);

        mvc.perform(get("/api/customers/by-user/{userId}", 101))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerid").value(2));

        verify(customerService).getCustomerByUserId(101L);
    }

    @Test
    void testGetCustomerByEmail_success() throws Exception {
        Customer customer = new Customer();
        customer.setCustomerid(3L);

        when(customerService.getCustomerByEmail("x@y.com")).thenReturn(customer);

        mvc.perform(get("/api/customers/by-email")
                        .param("email", "x@y.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerid").value(3));

        verify(customerService).getCustomerByEmail("x@y.com");
    }

    @Test
    void testGetCustomerByPhone_success() throws Exception {
        Customer customer = new Customer();
        customer.setCustomerid(4L);

        when(customerService.getCustomerByPhone("9999999999")).thenReturn(customer);

        mvc.perform(get("/api/customers/by-phone")
                        .param("phone", "9999999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerid").value(4));

        verify(customerService).getCustomerByPhone("9999999999");
    }

    @Test
    void testGetAllCustomers_empty() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of());

        mvc.perform(get("/api/customers/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(customerService).getAllCustomers();
    }

    @Test
    void testGetAllCustomers_nonEmpty() throws Exception {
        Customer c = new Customer();
        c.setCustomerid(10L);

        when(customerService.getAllCustomers()).thenReturn(List.of(c));

        mvc.perform(get("/api/customers/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].customerid").value(10));

        verify(customerService).getAllCustomers();
    }

    @Test
    void testUpdateCustomer_success() throws Exception {
        Customer updatedResult = new Customer();
        updatedResult.setCustomerid(10L);

        when(customerService.updateCustomer(eq(10L), any(Customer.class))).thenReturn(updatedResult);

        mvc.perform(put("/api/customers/update/{customerId}", 10)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullname\":\"New Name\",\"address\":\"New Addr\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerid").value(10));

        verify(customerService).updateCustomer(eq(10L), any(Customer.class));
    }

    @Test
    void testDeleteCustomer_success() throws Exception {
        doNothing().when(customerService).deleteCustomer(10L);

        mvc.perform(delete("/api/customers/delete/{customerId}", 10))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Customer deleted"));

        verify(customerService).deleteCustomer(10L);
    }

    // -------------------- Customer Status Management --------------------

    @Test
    void testGetCustomerStatus_success() throws Exception {
        CustomerStatus statusObj = new CustomerStatus();
        statusObj.setAccountactive(true);
        statusObj.setRemarks("Active");

        when(customerService.getCustomerStatus(1L)).thenReturn(statusObj);

        mvc.perform(get("/api/customers/{customerId}/status", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountactive").value(true))
                .andExpect(jsonPath("$.remarks").value("Active"));

        verify(customerService).getCustomerStatus(1L);
    }

    @Test
    void testActivateCustomer_success_nullRemarks() throws Exception {

        when(customerService.activateCustomer(1L, null))
                .thenReturn(new CustomerStatus());

        mvc.perform(put("/api/customers/{customerId}/status/activate", 1))
                .andExpect(status().isOk());

        verify(customerService).activateCustomer(1L, null);
    }

    @Test
    void testActivateCustomer_success_withRemarks() throws Exception {
        when(customerService.activateCustomer(1L, "ok"))
                .thenReturn(new CustomerStatus());

        mvc.perform(put("/api/customers/1/status/activate")
                        .param("remarks", "ok"))
                .andExpect(status().isOk());

        verify(customerService).activateCustomer(1L, "ok");
    }

    @Test
    void testDeactivateCustomer_success_nullRemarks() throws Exception {

        when(customerService.deactivateCustomer(1L, null))
                .thenReturn(new CustomerStatus());

        mvc.perform(put("/api/customers/{customerId}/status/deactivate", 1))
                .andExpect(status().isOk());

        verify(customerService).deactivateCustomer(1L, null);
    }

    @Test
    void testDeactivateCustomer_success_withRemarks() throws Exception {
        when(customerService.deactivateCustomer(1L, "not ok"))
                .thenReturn(new CustomerStatus());

        mvc.perform(put("/api/customers/{customerId}/status/deactivate", 1)
                        .param("remarks", "not ok"))
                .andExpect(status().isOk());

        verify(customerService).deactivateCustomer(1L, "not ok");
    }

    // -------------------- KYC Management --------------------

    @Test
    void testUpdateKycStatus_success() throws Exception {
        when(customerService.updateKycStatus(1L, KycStatus.VERIFIED))
                .thenReturn(new Customer());

        mvc.perform(put("/api/customers/{customerId}/kyc", 1)
                        .param("status", KycStatus.VERIFIED.name()))
                .andExpect(status().isOk());

        verify(customerService).updateKycStatus(1L, KycStatus.VERIFIED);
    }

    @Test
    void testSubmitKycProof_success() throws Exception {
        Customer customer = new Customer();
        customer.setCustomerid(1L);

        when(customerService.submitKycProof(1L, "AADHAR", "1234"))
                .thenReturn(customer);

        mvc.perform(put("/api/customers/{customerId}/kyc/proof", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idprooftype\":\"AADHAR\",\"idproofnumber\":\"1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerid").value(1));

        verify(customerService).submitKycProof(1L, "AADHAR", "1234");
    }

    // -------------------- Convenience Checks --------------------

    @Test
    void testExistsByEmail_true() throws Exception {
        when(customerService.existsByEmail("a@b.com")).thenReturn(true);

        mvc.perform(get("/api/customers/exists/email")
                        .param("email", "a@b.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exists").value(true));

        verify(customerService).existsByEmail("a@b.com");
    }

    @Test
    void testExistsByPhone_false() throws Exception {
        when(customerService.existsByPhone("999")).thenReturn(false);

        mvc.perform(get("/api/customers/exists/phone")
                        .param("phone", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(false));

        verify(customerService).existsByPhone("999");
    }

    @Test
    void testExistsByUserId_true() throws Exception {
        when(customerService.existsByUserId(101L)).thenReturn(true);

        mvc.perform(get("/api/customers/exists/user")
                        .param("userId", "101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));

        verify(customerService).existsByUserId(101L);
    }
}