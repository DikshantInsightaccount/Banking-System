package com.authservice.authservice;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.authservice.authservice.entity.*;


class AccountEntityTest {

    @Test
    void testAccountEntity_fullCoverage() {

        // ✅ Create dependencies
        Customer customer = new Customer();

        // ✅ Constructor coverage
        Account account = new Account(customer, "ACC123", AccountType.SAVINGS);

        assertEquals(customer, account.getCustomer());
        assertEquals("ACC123", account.getAccountnumber());
        assertEquals(AccountType.SAVINGS, account.getAccounttype());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertEquals(AccountStatus.INACTIVE, account.getStatus());

        // ✅ Setters coverage
        account.setAccountid(10L);
        account.setAccountnumber("NEW123");
        account.setAccounttype(AccountType.CURRENT);
        account.setBalance(new BigDecimal("1000"));
        account.setStatus(AccountStatus.ACTIVE);

        // ✅ Getters coverage
        assertEquals(10L, account.getAccountid());
        assertEquals("NEW123", account.getAccountnumber());
        assertEquals(AccountType.CURRENT, account.getAccounttype());
        assertEquals(new BigDecimal("1000"), account.getBalance());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());

        // ✅ PrePersist coverage (force null conditions)
        Account account2 = new Account();
        account2.setCustomer(customer);

        // explicitly set nulls
        account2.setBalance(null);
        account2.setStatus(null);

        account2.prePersist(); // ✅ trigger method

        assertNotNull(account2.getOpenedat()); // timestamp set
        assertEquals(BigDecimal.ZERO, account2.getBalance());
        assertEquals(AccountStatus.INACTIVE, account2.getStatus());
    }
}