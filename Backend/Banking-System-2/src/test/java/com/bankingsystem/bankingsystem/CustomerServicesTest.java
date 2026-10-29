package com.bankingsystem.bankingsystem;

import com.bankingsystem.bankingsystem.entity.Customer;
import com.bankingsystem.bankingsystem.entity.CustomerStatus;
import com.bankingsystem.bankingsystem.entity.KycStatus;
import com.bankingsystem.bankingsystem.entity.User;
import com.bankingsystem.bankingsystem.exceptions.ResourceNotFoundException;
import com.bankingsystem.bankingsystem.repositories.CustomerRepository;
import com.bankingsystem.bankingsystem.repositories.CustomerStatusRepository;
import com.bankingsystem.bankingsystem.services.CustomerServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServicesTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerStatusRepository customerStatusRepository;

    @InjectMocks
    private CustomerServices customerServices;

    private Customer validCustomer;

    @BeforeEach
    void setUp() {
        validCustomer = buildValidCustomer();
    }

    private Customer buildValidCustomer() {
        User user = new User();
        user.setUserid(101L);

        Customer c = new Customer();
        c.setUser(user);
        c.setFullname("Sree Miraa");
        c.setEmail("sree@example.com");
        c.setPhone("9999999999");
        c.setAddress("Hyderabad");
        // id proof not set by default
        return c;
    }

    // -------------------- registerCustomer --------------------

    @Test
    @DisplayName("registerCustomer: throws if user is null")
    void registerCustomer_userNull_throws() {
        validCustomer.setUser(null);
        assertThrows(IllegalArgumentException.class,
                () -> customerServices.registerCustomer(validCustomer));
        verifyNoInteractions(customerRepository, customerStatusRepository);
    }

    @Test
    @DisplayName("registerCustomer: throws if userId is null")
    void registerCustomer_userIdNull_throws() {
        validCustomer.getUser().setUserid(null);
        assertThrows(IllegalArgumentException.class,
                () -> customerServices.registerCustomer(validCustomer));
        verifyNoInteractions(customerRepository, customerStatusRepository);
    }

    @Test
    @DisplayName("registerCustomer: throws if fullname empty")
    void registerCustomer_fullnameEmpty_throws() {
        validCustomer.setFullname("   ");
        assertThrows(IllegalArgumentException.class,
                () -> customerServices.registerCustomer(validCustomer));
        verifyNoInteractions(customerRepository, customerStatusRepository);
    }

    @Test
    @DisplayName("registerCustomer: throws if email empty")
    void registerCustomer_emailEmpty_throws() {
        validCustomer.setEmail("");
        assertThrows(IllegalArgumentException.class,
                () -> customerServices.registerCustomer(validCustomer));
        verifyNoInteractions(customerRepository, customerStatusRepository);
    }

    @Test
    @DisplayName("registerCustomer: throws if phone empty")
    void registerCustomer_phoneEmpty_throws() {
        validCustomer.setPhone("   ");
        assertThrows(IllegalArgumentException.class,
                () -> customerServices.registerCustomer(validCustomer));
        verifyNoInteractions(customerRepository, customerStatusRepository);
    }

    @Test
    @DisplayName("registerCustomer: if only idprooftype present -> throws")
    void registerCustomer_onlyIdProofType_throws() {
        validCustomer.setIdprooftype("AADHAR");
        validCustomer.setIdproofnumber(null);

        assertThrows(IllegalArgumentException.class,
                () -> customerServices.registerCustomer(validCustomer));
        verifyNoInteractions(customerRepository, customerStatusRepository);
    }

    @Test
    @DisplayName("registerCustomer: if only idproofnumber present -> throws")
    void registerCustomer_onlyIdProofNumber_throws() {
        validCustomer.setIdprooftype(null);
        validCustomer.setIdproofnumber("1234");

        assertThrows(IllegalArgumentException.class,
                () -> customerServices.registerCustomer(validCustomer));
        verifyNoInteractions(customerRepository, customerStatusRepository);
    }

    @Test
    @DisplayName("registerCustomer: no id proof -> sets KYC PENDING, saves customer, creates default status")
    void registerCustomer_noIdProof_success_createsStatus() {
        Customer saved = buildValidCustomer();
        saved.setCustomerid(1L);

        when(customerRepository.save(any(Customer.class))).thenReturn(saved);
        when(customerStatusRepository.findByCustomer_Customerid(1L)).thenReturn(Optional.empty());
        when(customerStatusRepository.save(any(CustomerStatus.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Customer result = customerServices.registerCustomer(validCustomer);

        assertNotNull(result);
        assertEquals(KycStatus.PENDING, result.getKycstatus());

        // verify order: save customer -> find status -> save status
        InOrder inOrder = inOrder(customerRepository, customerStatusRepository);
        inOrder.verify(customerRepository).save(any(Customer.class));
        inOrder.verify(customerStatusRepository).findByCustomer_Customerid(1L);
        inOrder.verify(customerStatusRepository).save(any(CustomerStatus.class));

        verifyNoMoreInteractions(customerRepository, customerStatusRepository);
    }

    @Test
    @DisplayName("registerCustomer: id proof present -> trims fields and sets KYC PENDING")
    void registerCustomer_idProofPresent_success_trimsAndPending() {
        validCustomer.setIdprooftype("  AADHAR ");
        validCustomer.setIdproofnumber("  9999-8888 ");

        Customer saved = buildValidCustomer();
        saved.setCustomerid(10L);

        when(customerRepository.save(any(Customer.class))).thenReturn(saved);
        when(customerStatusRepository.findByCustomer_Customerid(10L)).thenReturn(Optional.empty());
        when(customerStatusRepository.save(any(CustomerStatus.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Customer result = customerServices.registerCustomer(validCustomer);

        // The service trims fields before saving, so verify what was passed to save()
        verify(customerRepository).save(argThat(c ->
                "AADHAR".equals(c.getIdprooftype()) &&
                        "9999-8888".equals(c.getIdproofnumber()) &&
                        c.getKycstatus() == KycStatus.PENDING
        ));

        assertEquals(KycStatus.PENDING, result.getKycstatus());
    }

    @Test
    @DisplayName("registerCustomer: if status already exists -> updates remarks and saves it")
    void registerCustomer_statusExists_updatesRemarks() {
        Customer saved = buildValidCustomer();
        saved.setCustomerid(5L);

        CustomerStatus existingStatus = new CustomerStatus(saved);
        existingStatus.setRemarks("OLD");

        when(customerRepository.save(any(Customer.class))).thenReturn(saved);
        when(customerStatusRepository.findByCustomer_Customerid(5L))
                .thenReturn(Optional.of(existingStatus));
        when(customerStatusRepository.save(any(CustomerStatus.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Customer result = customerServices.registerCustomer(validCustomer);
        assertNotNull(result);

        verify(customerStatusRepository).save(argThat(st ->
                "Registered - pending activation".equals(st.getRemarks())
        ));
    }

    // -------------------- getCustomerBy... --------------------

    @Test
    @DisplayName("getCustomerById: returns customer if exists")
    void getCustomerById_found() {
        Customer c = buildValidCustomer();
        c.setCustomerid(2L);
        when(customerRepository.findById(2L)).thenReturn(Optional.of(c));

        Customer result = customerServices.getCustomerById(2L);
        assertEquals(2L, result.getCustomerid());
    }

    @Test
    @DisplayName("getCustomerById: throws ResourceNotFoundException if not found")
    void getCustomerById_notFound_throws() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> customerServices.getCustomerById(99L));
    }

    @Test
    @DisplayName("getCustomerByUserId: throws if not found")
    void getCustomerByUserId_notFound_throws() {
        when(customerRepository.findByUser_Userid(101L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> customerServices.getCustomerByUserId(101L));
    }

    @Test
    @DisplayName("getCustomerByEmail: throws if not found")
    void getCustomerByEmail_notFound_throws() {
        when(customerRepository.findByEmail("x@y.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> customerServices.getCustomerByEmail("x@y.com"));
    }

    @Test
    @DisplayName("getCustomerByPhone: throws if not found")
    void getCustomerByPhone_notFound_throws() {
        when(customerRepository.findByPhone("123")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> customerServices.getCustomerByPhone("123"));
    }

    @Test
    @DisplayName("getAllCustomers: returns list")
    void getAllCustomers_returnsList() {
        when(customerRepository.findAll()).thenReturn(List.of(buildValidCustomer()));
        assertEquals(1, customerServices.getAllCustomers().size());
        verify(customerRepository).findAll();
    }

    // -------------------- updateCustomer --------------------

    @Test
    @DisplayName("updateCustomer: updates only non-null fields")
    void updateCustomer_updatesNonNullFields() {
        Customer existing = buildValidCustomer();
        existing.setCustomerid(7L);

        Customer updated = new Customer();
        updated.setFullname("New Name");
        updated.setEmail(null); // should not override
        updated.setPhone("8888888888");
        updated.setAddress("New Address");

        when(customerRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Customer result = customerServices.updateCustomer(7L, updated);

        assertEquals("New Name", result.getFullname());
        assertEquals("sree@example.com", result.getEmail()); // unchanged
        assertEquals("8888888888", result.getPhone());
        assertEquals("New Address", result.getAddress());
    }

    // -------------------- deleteCustomer --------------------

    @Test
    @DisplayName("deleteCustomer: deletes status (if exists) and then customer")
    void deleteCustomer_deletesStatusThenCustomer() {
        Customer existing = buildValidCustomer();
        existing.setCustomerid(3L);

        CustomerStatus status = new CustomerStatus(existing);

        when(customerRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(customerStatusRepository.findByCustomer_Customerid(3L))
                .thenReturn(Optional.of(status));

        customerServices.deleteCustomer(3L);

        verify(customerStatusRepository).delete(status);
        verify(customerRepository).delete(existing);
    }

    @Test
    @DisplayName("deleteCustomer: if status not found, deletes only customer")
    void deleteCustomer_statusMissing_deletesOnlyCustomer() {
        Customer existing = buildValidCustomer();
        existing.setCustomerid(4L);

        when(customerRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(customerStatusRepository.findByCustomer_Customerid(4L))
                .thenReturn(Optional.empty());

        customerServices.deleteCustomer(4L);

        verify(customerStatusRepository, never()).delete(any());
        verify(customerRepository).delete(existing);
    }

    // -------------------- getCustomerStatus --------------------

    @Test
    @DisplayName("getCustomerStatus: returns status if exists")
    void getCustomerStatus_found() {
        Customer c = buildValidCustomer();
        c.setCustomerid(11L);
        CustomerStatus status = new CustomerStatus(c);

        when(customerRepository.findById(11L)).thenReturn(Optional.of(c));
        when(customerStatusRepository.findByCustomer_Customerid(11L))
                .thenReturn(Optional.of(status));

        CustomerStatus result = customerServices.getCustomerStatus(11L);
        assertNotNull(result);
    }

    @Test
    @DisplayName("getCustomerStatus: throws if status missing")
    void getCustomerStatus_missing_throws() {
        Customer c = buildValidCustomer();
        c.setCustomerid(12L);

        when(customerRepository.findById(12L)).thenReturn(Optional.of(c));
        when(customerStatusRepository.findByCustomer_Customerid(12L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerServices.getCustomerStatus(12L));
    }

    // -------------------- activate/deactivate --------------------

    @Test
    @DisplayName("activateCustomer: sets accountactive true and default remarks if null")
    void activateCustomer_setsActiveTrue_defaultRemarks() {
        Customer c = buildValidCustomer();
        c.setCustomerid(20L);

        when(customerRepository.findById(20L)).thenReturn(Optional.of(c));
        when(customerStatusRepository.findByCustomer_Customerid(20L))
                .thenReturn(Optional.empty());
        when(customerStatusRepository.save(any(CustomerStatus.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CustomerStatus result = customerServices.activateCustomer(20L, null);

        assertTrue(result.getAccountactive());
        assertEquals("Activated", result.getRemarks());
    }

    @Test
    @DisplayName("deactivateCustomer: sets accountactive false and default remarks if null")
    void deactivateCustomer_setsActiveFalse_defaultRemarks() {
        Customer c = buildValidCustomer();
        c.setCustomerid(21L);

        when(customerRepository.findById(21L)).thenReturn(Optional.of(c));
        when(customerStatusRepository.findByCustomer_Customerid(21L))
                .thenReturn(Optional.empty());
        when(customerStatusRepository.save(any(CustomerStatus.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CustomerStatus result = customerServices.deactivateCustomer(21L, null);

        assertFalse(result.getAccountactive());
        assertEquals("Deactivated", result.getRemarks());
    }

    // -------------------- submitKycProof --------------------

    @Test
    @DisplayName("submitKycProof: throws if idProofType blank")
    void submitKycProof_typeBlank_throws() {
        Customer c = buildValidCustomer();
        c.setCustomerid(30L);

        when(customerRepository.findById(30L)).thenReturn(Optional.of(c));

        assertThrows(IllegalArgumentException.class,
                () -> customerServices.submitKycProof(30L, "   ", "123"));
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("submitKycProof: trims fields, sets KYC PENDING and saves")
    void submitKycProof_success_trimsAndPending() {
        Customer c = buildValidCustomer();
        c.setCustomerid(31L);

        when(customerRepository.findById(31L)).thenReturn(Optional.of(c));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Customer result = customerServices.submitKycProof(31L, "  PAN ", "  ABC123 ");

        assertEquals("PAN", result.getIdprooftype());
        assertEquals("ABC123", result.getIdproofnumber());
        assertEquals(KycStatus.PENDING, result.getKycstatus());
        verify(customerRepository).save(c);
    }

    // -------------------- updateKycStatus --------------------

    @Test
    @DisplayName("updateKycStatus: updates status and saves")
    void updateKycStatus_updatesAndSaves() {
        Customer c = buildValidCustomer();
        c.setCustomerid(40L);

        when(customerRepository.findById(40L)).thenReturn(Optional.of(c));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Customer result = customerServices.updateKycStatus(40L, KycStatus.VERIFIED);

        assertEquals(KycStatus.VERIFIED, result.getKycstatus());
        verify(customerRepository).save(c);
    }

    @Test
    void updateKycStatus_notFound_throws() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerServices.updateKycStatus(999L, KycStatus.VERIFIED));

        verify(customerRepository, never()).save(any());
    }


    // -------------------- exists checks --------------------

    @Test
    @DisplayName("existsByEmail: delegates to repository")
    void existsByEmail_delegates() {
        when(customerRepository.existsByEmail("a@b.com")).thenReturn(true);
        assertTrue(customerServices.existsByEmail("a@b.com"));
        verify(customerRepository).existsByEmail("a@b.com");
    }

    @Test
    @DisplayName("existsByPhone: delegates to repository")
    void existsByPhone_delegates() {
        when(customerRepository.existsByPhone("999")).thenReturn(false);
        assertFalse(customerServices.existsByPhone("999"));
        verify(customerRepository).existsByPhone("999");
    }

    @Test
    @DisplayName("existsByUserId: delegates to repository")
    void existsByUserId_delegates() {
        when(customerRepository.existsByUser_Userid(101L)).thenReturn(true);
        assertTrue(customerServices.existsByUserId(101L));
        verify(customerRepository).existsByUser_Userid(101L);
    }

    @Test
    void updateCustomer_updatesEmailWhenProvided() {
        Customer existing = buildValidCustomer();
        existing.setCustomerid(70L);

        Customer updated = new Customer();
        updated.setEmail("new@email.com");

        when(customerRepository.findById(70L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Customer result = customerServices.updateCustomer(70L, updated);

        assertEquals("new@email.com", result.getEmail());
        verify(customerRepository).save(existing);
    }
    @Test
    void updateCustomer_noFieldsProvided_doesNotChangeAnything() {
        Customer existing = buildValidCustomer();
        existing.setCustomerid(71L);

        Customer updated = new Customer(); // all fields null

        when(customerRepository.findById(71L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Customer result = customerServices.updateCustomer(71L, updated);

        assertEquals("Sree Miraa", result.getFullname());
        assertEquals("sree@example.com", result.getEmail());
        assertEquals("9999999999", result.getPhone());
        assertEquals("Hyderabad", result.getAddress());
        verify(customerRepository).save(existing);
    }

    @Test
    void updateCustomer_notFound_throws() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        Customer updated = new Customer();

        assertThrows(ResourceNotFoundException.class,
                () -> customerServices.updateCustomer(999L, updated));

        verify(customerRepository, never()).save(any());
    }


    @Test
    void activateCustomer_withRemarks_usesProvidedRemarks() {
        Customer c = buildValidCustomer();
        c.setCustomerid(22L);

        when(customerRepository.findById(22L)).thenReturn(Optional.of(c));
        when(customerStatusRepository.findByCustomer_Customerid(22L)).thenReturn(Optional.empty());
        when(customerStatusRepository.save(any(CustomerStatus.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerStatus result = customerServices.activateCustomer(22L, "ok");

        assertTrue(result.getAccountactive());
        assertEquals("ok", result.getRemarks());
    }

    @Test
    void deactivateCustomer_withRemarks_usesProvidedRemarks() {
        Customer c = buildValidCustomer();
        c.setCustomerid(23L);

        when(customerRepository.findById(23L)).thenReturn(Optional.of(c));
        when(customerStatusRepository.findByCustomer_Customerid(23L)).thenReturn(Optional.empty());
        when(customerStatusRepository.save(any(CustomerStatus.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerStatus result = customerServices.deactivateCustomer(23L, "not ok");

        assertFalse(result.getAccountactive());
        assertEquals("not ok", result.getRemarks());
    }

    @Test
    void submitKycProof_typeNull_throws() {
        Customer c = buildValidCustomer();
        c.setCustomerid(50L);

        when(customerRepository.findById(50L)).thenReturn(Optional.of(c));

        assertThrows(IllegalArgumentException.class,
                () -> customerServices.submitKycProof(50L, null, "123"));

        verify(customerRepository, never()).save(any());
    }

    @Test
    void submitKycProof_numberBlank_throws() {
        Customer c = buildValidCustomer();
        c.setCustomerid(51L);

        when(customerRepository.findById(51L)).thenReturn(Optional.of(c));

        assertThrows(IllegalArgumentException.class,
                () -> customerServices.submitKycProof(51L, "PAN", "   "));

        verify(customerRepository, never()).save(any());
    }

    @Test
    void pojoCoverage_smokeTest() {
        com.bankingsystem.bankingsystem.entity.User u = new com.bankingsystem.bankingsystem.entity.User();
        u.setUserid(1L);
        assertEquals(1L, u.getUserid());

        com.bankingsystem.bankingsystem.entity.Account a = new com.bankingsystem.bankingsystem.entity.Account();
        a.setAccountid(10L);
        assertEquals(10L, a.getAccountid());

        com.bankingsystem.bankingsystem.entity.FixedDeposit fd = new com.bankingsystem.bankingsystem.entity.FixedDeposit();
        fd.setFdid(100L);
        assertEquals(100L, fd.getFdid());

        com.bankingsystem.bankingsystem.entity.RecurringDeposit rd = new com.bankingsystem.bankingsystem.entity.RecurringDeposit();
        rd.setRdid(200L);
        assertEquals(200L, rd.getRdid());
    }

    @Test
    void activateCustomer_notFound_throws() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerServices.activateCustomer(999L, null));
    }

    @Test
    void deactivateCustomer_notFound_throws() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerServices.deactivateCustomer(999L, null));
    }

    @Test
    void deleteCustomer_notFound_throws() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerServices.deleteCustomer(999L));

        verify(customerRepository, never()).delete(any());
    }

    @Test
    void submitKycProof_notFound_throws() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerServices.submitKycProof(999L, "PAN", "123"));
    }

    @Test
    void getCustomerStatus_customerMissing_throws() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerServices.getCustomerStatus(999L));
    }

}

