    package com.bankingsystem.bankingsystem.controllers;

    import com.bankingsystem.bankingsystem.entity.Customer;
    import com.bankingsystem.bankingsystem.entity.CustomerStatus;
    import com.bankingsystem.bankingsystem.entity.KycStatus;
    import com.bankingsystem.bankingsystem.services.CustomerServiceImp;
    import com.bankingsystem.bankingsystem.util.ResponseMessage;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import java.util.List;
    import java.util.Map;

    @RestController
    @RequestMapping("/api/customers")

    public class CustomerController {

        private static final String EXISTS_KEY = "exists";

        private final CustomerServiceImp customerService;

        public CustomerController(CustomerServiceImp customerService) {
            this.customerService = customerService;
        }

        // -------------------- Customer CRUD --------------------

        // Register customer (creates customer + default CustomerStatus)
        @PostMapping("/register")
        public ResponseEntity<Customer> registerCustomer(@RequestBody Customer customer) {
            return ResponseEntity.ok(customerService.registerCustomer(customer));
        }

        // Get customer by customerId
        @GetMapping("/{customerId}")
        public ResponseEntity<Customer> getCustomerById(@PathVariable Long customerId) {
            return ResponseEntity.ok(customerService.getCustomerById(customerId));
        }

        // Get customer by userId (auth userId reference)
        @GetMapping("/by-user/{userId}")
        public ResponseEntity<Customer> getCustomerByUserId(@PathVariable Long userId) {
            return ResponseEntity.ok(customerService.getCustomerByUserId(userId));
        }

        // Get by email
        @GetMapping("/by-email")
        public ResponseEntity<Customer> getCustomerByEmail(@RequestParam String email) {
            return ResponseEntity.ok(customerService.getCustomerByEmail(email));
        }

        // Get by phone
        @GetMapping("/by-phone")
        public ResponseEntity<Customer> getCustomerByPhone(@RequestParam String phone) {
            return ResponseEntity.ok(customerService.getCustomerByPhone(phone));
        }

        // Get all customers (employee use-case)
        @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<List<Customer>> getAllCustomers() {
            return ResponseEntity.ok(customerService.getAllCustomers());
        }

        // Update customer profile fields
        @PutMapping("/update/{customerId}")
        public ResponseEntity<Customer> updateCustomer(@PathVariable Long customerId,
                                                       @RequestBody Customer updated) {
            return ResponseEntity.ok(customerService.updateCustomer(customerId, updated));
        }

        // Delete customer
        @DeleteMapping("/delete/{customerId}")
        public ResponseEntity<ResponseMessage> deleteCustomer(@PathVariable Long customerId) {

            customerService.deleteCustomer(customerId);

            return ResponseEntity.ok(new ResponseMessage("Customer deleted"));
        }

        // -------------------- Customer Status Management --------------------

        // Get customer status
        @GetMapping("/{customerId}/status")
        public ResponseEntity<CustomerStatus> getCustomerStatus(@PathVariable Long customerId) {
            return ResponseEntity.ok(customerService.getCustomerStatus(customerId));
        }

        // Activate customer (employee action)
        @PutMapping("/{customerId}/status/activate")
        public ResponseEntity<CustomerStatus> activateCustomer(@PathVariable Long customerId,
                                                               @RequestParam(required = false) String remarks) {
            return ResponseEntity.ok(customerService.activateCustomer(customerId, remarks));
        }

        // Deactivate customer (employee action)
        @PutMapping("/{customerId}/status/deactivate")
        public ResponseEntity<CustomerStatus> deactivateCustomer(@PathVariable Long customerId,
                                                                 @RequestParam(required = false) String remarks) {
            return ResponseEntity.ok(customerService.deactivateCustomer(customerId, remarks));
        }

        // -------------------- KYC Management --------------------

        // Update KYC status (employee action)
        @PutMapping("/{customerId}/kyc")
        public ResponseEntity<Customer> updateKycStatus(@PathVariable Long customerId,
                                                        @RequestParam KycStatus status) {
            return ResponseEntity.ok(customerService.updateKycStatus(customerId, status));
        }

        // -------------------- KYC Management --------------------

        // NEW: Customer submits KYC proof
        @PutMapping("/{customerId}/kyc/proof")
        public ResponseEntity<Customer> submitKycProof(
                @PathVariable Long customerId,
                @RequestBody com.bankingsystem.bankingsystem.dto.KycProofRequest request
        ) {
            return ResponseEntity.ok(
                    customerService.submitKycProof(
                            customerId,
                            request.getIdprooftype(),
                            request.getIdproofnumber()
                    )
            );
        }


        // -------------------- Convenience Checks --------------------



        @GetMapping("/exists/email")
        public ResponseEntity<Map<String, Boolean>> existsByEmail(@RequestParam String email) {
            return ResponseEntity.ok(
                    Map.of(EXISTS_KEY, customerService.existsByEmail(email))
            );
        }

        @GetMapping("/exists/phone")
        public ResponseEntity<Map<String, Boolean>> existsByPhone(@RequestParam String phone) {
            return ResponseEntity.ok(
                    Map.of(EXISTS_KEY, customerService.existsByPhone(phone))
            );
        }

        @GetMapping("/exists/user")
        public ResponseEntity<Map<String, Boolean>> existsByUserId(@RequestParam Long userId) {
            return ResponseEntity.ok(
                    Map.of(EXISTS_KEY, customerService.existsByUserId(userId))
            );
        }

    }
