
package com.bankingsystem.bankingsystem.services;

import com.bankingsystem.bankingsystem.entity.Customer;
import com.bankingsystem.bankingsystem.entity.CustomerStatus;
import com.bankingsystem.bankingsystem.entity.KycStatus;
import com.bankingsystem.bankingsystem.entity.User;
import com.bankingsystem.bankingsystem.exceptions.ResourceNotFoundException;
import com.bankingsystem.bankingsystem.repositories.CustomerRepository;
import com.bankingsystem.bankingsystem.repositories.CustomerStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CustomerServices implements CustomerServiceImp {

    private static final String CUSTOMER_NOT_FOUND = "Customer not found with id: ";


    private final CustomerRepository customerRepository;
    private final CustomerStatusRepository customerStatusRepository;

    public CustomerServices(CustomerRepository customerRepository,
                               CustomerStatusRepository customerStatusRepository) {
        this.customerRepository = customerRepository;
        this.customerStatusRepository = customerStatusRepository;
    }

    // -------------------- Customer CRUD --------------------

    @Override
    public Customer registerCustomer(Customer customer) {

        validateCustomerBasics(customer);
        applyKycPendingAndValidateProof(customer);

        Long userId = customer.getUser().getUserid();

        // ✅ ✅ NEW VALIDATIONS (CRITICAL FIX)
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (customerRepository.existsByPhone(customer.getPhone())) {
            throw new RuntimeException("Phone already exists");
        }

        if (customerRepository.existsByUser_Userid(userId)) {
            throw new RuntimeException("Customer already exists for this user");
        }

        // ✅ Attach user reference
        User managedUser = new User();
        managedUser.setUserid(userId);
        customer.setUser(managedUser);

        Customer saved = customerRepository.save(customer);

        CustomerStatus status = customerStatusRepository
                .findByCustomer_Customerid(saved.getCustomerid())
                .orElseGet(() -> new CustomerStatus(saved));

        status.setRemarks("Registered - pending activation");
        customerStatusRepository.save(status);

        return saved;
    }

    private void validateCustomerBasics(Customer customer) {
        if (customer.getUser() == null || customer.getUser().getUserid() == null) {
            throw new IllegalArgumentException("UserId is required");
        }

        requireNonBlank(customer.getFullname(), "Fullname is required");
        requireNonBlank(customer.getEmail(), "Email is required");
        requireNonBlank(customer.getPhone(), "Phone is required");
    }

    private void applyKycPendingAndValidateProof(Customer customer) {
        // Always set KYC to PENDING initially (same as your code)
        customer.setKycstatus(KycStatus.PENDING);

        // If ID proof is provided → validate both fields (same condition as your code: checks null only)
        boolean anyProofProvided = customer.getIdprooftype() != null || customer.getIdproofnumber() != null;
        if (!anyProofProvided) {
            return;
        }

        requireNonBlank(customer.getIdprooftype(), "idprooftype is required");
        requireNonBlank(customer.getIdproofnumber(), "idproofnumber is required");

        // Trim for safety (same as your code)
        customer.setIdprooftype(customer.getIdprooftype().trim());
        customer.setIdproofnumber(customer.getIdproofnumber().trim());
    }

    private void requireNonBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(CUSTOMER_NOT_FOUND+ customerId));
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerByUserId(Long userId) {
        return customerRepository.findByUser_Userid(userId)
                .orElseThrow(() -> new ResourceNotFoundException(CUSTOMER_NOT_FOUND + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(CUSTOMER_NOT_FOUND+ email));
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerByPhone(String phone) {
        return customerRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException(CUSTOMER_NOT_FOUND+ phone));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return this.customerRepository.findAll();
    }


    @Override
    public Customer updateCustomer(Long customerId, Customer updated) {

        Customer existing = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(CUSTOMER_NOT_FOUND+ customerId)
                );

        if (updated.getFullname() != null) existing.setFullname(updated.getFullname());
        if (updated.getEmail() != null) existing.setEmail(updated.getEmail());
        if (updated.getPhone() != null) existing.setPhone(updated.getPhone());
        if (updated.getAddress() != null) existing.setAddress(updated.getAddress());

        return customerRepository.save(existing);
    }


    @Override
    public void deleteCustomer(Long customerId) {

        Customer existing = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(CUSTOMER_NOT_FOUND + customerId)
                );

        customerStatusRepository.findByCustomer_Customerid(customerId)
                .ifPresent(customerStatusRepository::delete);

        customerRepository.delete(existing);
    }


    // -------------------- Customer Status Management --------------------

    @Override
    @Transactional(readOnly = true)
    public CustomerStatus getCustomerStatus(Long customerId) {

        // ensure customer exists
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(CUSTOMER_NOT_FOUND + customerId)
                );

        return customerStatusRepository.findByCustomer_Customerid(customer.getCustomerid())
                .orElseThrow(() ->
                        new ResourceNotFoundException(CUSTOMER_NOT_FOUND+ customerId)
                );
    }

    @Override
    public CustomerStatus activateCustomer(Long customerId, String remarks) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(CUSTOMER_NOT_FOUND + customerId)
                );

        CustomerStatus status = customerStatusRepository
                .findByCustomer_Customerid(customer.getCustomerid())
                .orElseGet(() -> new CustomerStatus(customer));

        status.setAccountactive(true);
        status.setRemarks(remarks != null ? remarks : "Activated");

        return customerStatusRepository.save(status);
    }


    @Override
    public CustomerStatus deactivateCustomer(Long customerId, String remarks) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(CUSTOMER_NOT_FOUND+ customerId)
                );

        CustomerStatus status = customerStatusRepository
                .findByCustomer_Customerid(customer.getCustomerid())
                .orElseGet(() -> new CustomerStatus(customer));

        status.setAccountactive(false);
        status.setRemarks(remarks != null ? remarks : "Deactivated");

        return customerStatusRepository.save(status);
    }


    @Override
    public Customer submitKycProof(Long customerId, String idProofType, String idProofNumber) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(CUSTOMER_NOT_FOUND+ customerId)
                );

        if (idProofType == null || idProofType.trim().isEmpty()) {
            throw new IllegalArgumentException("idprooftype is required");
        }

        if (idProofNumber == null || idProofNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("idproofnumber is required");
        }

        customer.setIdprooftype(idProofType.trim());
        customer.setIdproofnumber(idProofNumber.trim());

        // Customer submits proof → keep status PENDING
        customer.setKycstatus(KycStatus.PENDING);

        return customerRepository.save(customer);
    }


    // -------------------- KYC Management --------------------

    @Override
    public Customer updateKycStatus(Long customerId, KycStatus status) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(CUSTOMER_NOT_FOUND + customerId)
                );

        customer.setKycstatus(status);

        return customerRepository.save(customer); // ✅ THIS LINE WAS MISSING
    }


    // -------------------- Existence checks --------------------

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPhone(String phone) {
        return customerRepository.existsByPhone(phone);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserId(Long userId) {
        return customerRepository.existsByUser_Userid(userId);
    }
}