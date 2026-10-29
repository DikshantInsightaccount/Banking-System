package com.accountservice.accountservice;

import com.accountservice.accountservice.entity.Account;
import com.accountservice.accountservice.entity.AccountStatus;
import com.accountservice.accountservice.exceptions.AccountExistException;
import com.accountservice.accountservice.exceptions.AccountNotFoundException;
import com.accountservice.accountservice.repositories.AccountRepository;
import com.accountservice.accountservice.services.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void getAllAccounts_success() {
        when(accountRepository.findAll()).thenReturn(List.of(new Account(customer, "ACC123", AccountType.SAVINGS)));
        assertEquals(1, accountService.getAllAccounts().size());
    }

    @Test
    void getOneAccount_found() {
        Account acc = new Account(customer, "ACC123", AccountType.SAVINGS);
        acc.setAccountid(1L);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));

        assertEquals(1L, accountService.getOneAccount(1L).getAccountid());
    }

    @Test
    void getOneAccount_notFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> accountService.getOneAccount(1L));
    }

    @Test
    void openAccount_success() {
        Account acc = new Account(customer, "ACC123", AccountType.SAVINGS);

        when(accountRepository.save(acc)).thenReturn(acc);

        assertNotNull(accountService.openAccount(acc));
    }

    @Test
    void openAccount_accountIdExists() {
        Account acc = new Account(customer, "ACC123", AccountType.SAVINGS);
        acc.setAccountid(1L);

        when(accountRepository.existsById(1L)).thenReturn(true);

        assertThrows(AccountExistException.class,
                () -> accountService.openAccount(acc));
    }

    @Test
    void updateAccount_success() {
        Account acc = new Account(customer, "ACC123", AccountType.SAVINGS);
        acc.setAccountid(1L);

        when(accountRepository.existsById(1L)).thenReturn(true);
        when(accountRepository.save(acc)).thenReturn(acc);

        assertNotNull(accountService.updateAccount(acc));
    }

    @Test
    void deleteAccount_success() {
        when(accountRepository.existsById(1L)).thenReturn(true);
        doNothing().when(accountRepository).deleteById(1L);

        accountService.deleteAccount(1L);
        verify(accountRepository).deleteById(1L);
    }

    @Test
    void activateAccount_success() {
        Account acc = new Account(customer, "ACC123", AccountType.SAVINGS);
        acc.setStatus(AccountStatus.INACTIVE);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));
        when(accountRepository.save(acc)).thenReturn(acc);

        Account result = accountService.activateAccount(1L);
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
    }
}
