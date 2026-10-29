
package com.accountservice.accountservice.services;

import com.accountservice.accountservice.exceptions.AccountExistException;
import com.accountservice.accountservice.exceptions.AccountNotFoundException;
import com.accountservice.accountservice.repositories.AccountRepository;
import com.accountservice.accountservice.entity.Account;
import com.accountservice.accountservice.entity.AccountStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getOneAccount(Long id) {
        Optional<Account> accOp = accountRepository.findById(id);
        if (accOp.isPresent()) {
            return accOp.get();
        } else {
            throw new AccountNotFoundException("Account with id " + id + " not found");
        }
    }

    public List<Account> getAccountsByCustomer(Long customerId) {
        return accountRepository.findByCustomerid(customerId);
    }

    public Account openAccount(Account account) {

        if (account.getAccountid() != null &&
                accountRepository.existsById(account.getAccountid())) {
            throw new AccountExistException("Account already exists");
        }

        if (account.getAccountnumber() != null &&
                accountRepository.existsByAccountnumber(account.getAccountnumber())) {
            throw new AccountExistException("Account number already exists");
        }

        return accountRepository.save(account);
    }

    public Account updateAccount(Account account) {
        if (account.getAccountid() == null ||
                !accountRepository.existsById(account.getAccountid())) {
            throw new AccountNotFoundException("Account not found");
        }
        return accountRepository.save(account);
    }

    public void deleteAccount(Long id) {
        if (accountRepository.existsById(id)) {
            accountRepository.deleteById(id);
        } else {
            throw new AccountNotFoundException("Account with id " + id + " not found");
        }
    }

    public Account activateAccount(Long id) {
        Account acc = accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountNotFoundException("Account not found"));
        acc.setStatus(AccountStatus.ACTIVE);
        return accountRepository.save(acc);
    }

    public Account deactivateAccount(Long id) {
        Account acc = accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountNotFoundException("Account not found"));
        acc.setStatus(AccountStatus.INACTIVE);
        return accountRepository.save(acc);
    }

    public Account partialUpdate(Long id, Account partial) {
        Account target = accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountNotFoundException("Account not found"));

        if (partial.getAccountnumber() != null) {
            target.setAccountnumber(partial.getAccountnumber());
        }
        if (partial.getAccounttype() != null) {
            target.setAccounttype(partial.getAccounttype());
        }
        if (partial.getBalance() != null) {
            target.setBalance(partial.getBalance());
        }
        if (partial.getStatus() != null) {
            target.setStatus(partial.getStatus());
        }

        return accountRepository.save(target);
    }
}