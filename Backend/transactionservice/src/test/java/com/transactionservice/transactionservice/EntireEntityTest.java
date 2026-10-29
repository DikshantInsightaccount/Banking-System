package com.transactionservice.transactionservice;

import com.transactionservice.transactionservice.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EntireEntityTest {

    @Test
    void testAllEntitiesCoverage() {

        //  User
        User user = new User();
        user.setUserid(1L);
        assertEquals(1L, user.getUserid());

        //  Customer
        Customer customer = new Customer();
        customer.setUser(user);
        customer.setFullname("Sree");
        customer.setEmail("sree@test.com");
        customer.setPhone("999");
        customer.setAddress("Hyd");

        customer.setKycstatus(null);
        customer.prePersist(); // trigger

        assertNotNull(customer.getCreatedat());
        assertEquals(KycStatus.PENDING, customer.getKycstatus());

        //  CustomerStatus
        CustomerStatus status = new CustomerStatus(customer);
        status.setAccountactive(true);
        status.setRemarks("ok");
        status.updateTimestamp(); // cover @PreUpdate

        assertNotNull(status.getUpdatedat());
        assertTrue(status.getAccountactive());

        //  Employee
        Employee employee = new Employee(user, "Emp", "Clerk", "HYD");
        employee.prePersist();

        assertNotNull(employee.getCreatedat());
        assertEquals("Emp", employee.getFullname());

        //  Account
        Account account = new Account(customer, "ACC1", AccountType.SAVINGS);

        account.setBalance(null);
        account.setStatus(null);
        account.prePersist();

        assertNotNull(account.getOpenedat());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertEquals(AccountStatus.INACTIVE, account.getStatus());

        //  FixedDeposit
        FixedDeposit fd = new FixedDeposit(account,
                new BigDecimal("1000"),
                new BigDecimal("5"),
                12);

        fd.setStartdate(null);
        fd.setStatus(null);
        fd.prePersist();

        assertNotNull(fd.getStartdate());
        assertEquals(FixedDepositStatus.ACTIVE, fd.getStatus());

        //  RecurringDeposit
        RecurringDeposit rd = new RecurringDeposit(account,
                new BigDecimal("500"),
                12,
                new BigDecimal("6"));

        rd.setStartdate(null);
        rd.setStatus(null);
        rd.prePersist();

        assertNotNull(rd.getStartdate());
        assertEquals(DepositStatus.ACTIVE, rd.getStatus());

        //  Transfer
        Transfer transfer = new Transfer(account, account,
                new BigDecimal("100"));

        transfer.setStatus(null);
        transfer.prePersist();

        assertNotNull(transfer.getTxntimestamp());
        assertEquals(TransferStatus.SUCCESS, transfer.getStatus());

        //  Transaction
        Transaction txn = new Transaction(account,
                TransactionType.DEPOSIT,
                new BigDecimal("200"));

        txn.setStatus(null);
        txn.prePersist();

        assertNotNull(txn.getTxntimestamp());
        assertEquals(TransactionStatus.SUCCESS, txn.getStatus());
    }

    @Test
    void testFixedDepositEntityCoverage() {

        Account account = new Account();
        FixedDeposit fd = new FixedDeposit();

        //  setters
        fd.setFdid(1L);
        fd.setAccount(account);
        fd.setPrincipalamount(new BigDecimal("1000"));
        fd.setInterestrate(new BigDecimal("5"));
        fd.setTenuremonths(12);
        fd.setMaturityamount(new BigDecimal("1200"));
        fd.setStartdate(LocalDate.now());

        //  getters
        assertEquals(1L, fd.getFdid());
        assertEquals(account, fd.getAccount());
        assertEquals(new BigDecimal("1000"), fd.getPrincipalamount());
        assertEquals(new BigDecimal("5"), fd.getInterestrate());
        assertEquals(12, fd.getTenuremonths());
        assertEquals(new BigDecimal("1200"), fd.getMaturityamount());
        assertNotNull(fd.getStartdate());

        // prePersist
        fd.setStartdate(null);
        fd.prePersist();

        assertNotNull(fd.getStartdate());
    }

    @Test
    void testEmployee_gettersSettersCoverage() {

        User user = new User();
        user.setUserid(1L);

        Employee emp = new Employee();

        // setters
        emp.setEmployeeid(10L);
        emp.setUser(user);
        emp.setFullname("Sree Miraa");
        emp.setDesignation("SE Intern");
        emp.setBranch("Hyderabad");

        // getters
        assertEquals(10L, emp.getEmployeeid());
        assertEquals(user, emp.getUser());
        assertEquals("Sree Miraa", emp.getFullname());
        assertEquals("SE Intern", emp.getDesignation());
        assertEquals("Hyderabad", emp.getBranch());
    }

}