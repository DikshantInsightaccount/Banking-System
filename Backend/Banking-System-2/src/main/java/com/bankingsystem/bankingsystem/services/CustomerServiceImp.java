package com.bankingsystem.bankingsystem.services;

import com.bankingsystem.bankingsystem.entity.Customer;
import com.bankingsystem.bankingsystem.entity.CustomerStatus;
import com.bankingsystem.bankingsystem.entity.KycStatus;

import java.util.List;

public interface CustomerServiceImp {

    // Customer CRUD
    Customer registerCustomer(Customer customer);
    Customer getCustomerById(Long customerId);
    Customer getCustomerByUserId(Long userId);
    Customer getCustomerByEmail(String email);
    Customer getCustomerByPhone(String phone);
    List<Customer> getAllCustomers();
    Customer updateCustomer(Long customerId, Customer updated);
    void deleteCustomer(Long customerId);

    // Customer status management (Employee actions)
    CustomerStatus getCustomerStatus(Long customerId);
    CustomerStatus activateCustomer(Long customerId, String remarks);
    CustomerStatus deactivateCustomer(Long customerId, String remarks);

    // ✅ KYC management
    Customer submitKycProof(Long customerId, String idProofType, String idProofNumber); // ✅ NEW
    Customer updateKycStatus(Long customerId, KycStatus status);                         // existing

    // Convenience checks
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByUserId(Long userId);
}