package com.accountservice.accountservice.controllers;



import com.accountservice.accountservice.services.AccountService;
import com.accountservice.accountservice.entity.Account;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    //  Get all accounts
    @GetMapping
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    //  Get account by accountId
    @GetMapping("/{accountId}")
    public Account getOneAccount(@PathVariable Long accountId) {
        return accountService.getOneAccount(accountId);
    }

    //  Get accounts by customerId
    @GetMapping("/customer/{customerId}")
    public List<Account> getAccountsByCustomer(@PathVariable Long customerId) {
        return accountService.getAccountsByCustomer(customerId);
    }

    //  Open account
    @PostMapping
    public Account openAccount(@RequestBody Account account) {
        return accountService.openAccount(account);
    }

    //  Full update account
    @PutMapping
    public Account updateAccount(@RequestBody Account account) {
        return accountService.updateAccount(account);
    }

    //  Partial update account
    @PatchMapping("/{accountId}")
    public Account partialUpdate(@PathVariable Long accountId,
                                 @RequestBody Account account) {
        return accountService.partialUpdate(accountId, account);
    }

    //  Activate account
    @PutMapping("/{accountId}/activate")
    public Account activateAccount(@PathVariable Long accountId) {
        return accountService.activateAccount(accountId);
    }

    //  Deactivate account
    @PutMapping("/{accountId}/deactivate")
    public Account deactivateAccount(@PathVariable Long accountId) {
        return accountService.deactivateAccount(accountId);
    }

    //  Delete account
    @DeleteMapping("/{accountId}")
    public void deleteAccount(@PathVariable Long accountId) {
        accountService.deleteAccount(accountId);
    }
}
