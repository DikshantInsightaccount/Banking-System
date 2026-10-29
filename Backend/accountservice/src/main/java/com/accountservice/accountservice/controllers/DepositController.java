package com.accountservice.accountservice.controllers;

import com.accountservice.accountservice.dto.SecureDepositRequest;
import com.accountservice.accountservice.entity.FixedDeposit;
import com.accountservice.accountservice.entity.RecurringDeposit;
import com.accountservice.accountservice.services.DepositService;
import com.accountservice.accountservice.dto.RecurringInstallmentRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deposits")
public class DepositController {

    private final DepositService depositService;

    public DepositController(DepositService depositService) {
        this.depositService = depositService;
    }

    @PostMapping("/fixed/{accountId}")
    public FixedDeposit createFixedDeposit(
            @PathVariable Long accountId,
            @RequestBody SecureDepositRequest request
    ) {

        return depositService
                .createFixedDepositWithPassword(
                        accountId,
                        request.getUserId(),
                        request.getPassword(),
                        request.getIdempotencyKey(),
                        request.getFd()
                );
    }

    @PutMapping("/fixed/{accountId}/{fdId}")
    public FixedDeposit updateFixedDeposit(
            @PathVariable Long accountId,
            @PathVariable Long fdId,
            @RequestBody FixedDeposit fd) {

        return depositService.updateFixedDeposit(
                accountId,
                fdId,
                fd
        );
    }

    @GetMapping("/fixed/account/{accountId}")
    public List<FixedDeposit> getFixedDeposits(
            @PathVariable Long accountId) {

        return depositService.getFixedDeposits(accountId);
    }

    @GetMapping("/fixed/{fdId}")
    public FixedDeposit getFixedDeposit(
            @PathVariable Long fdId) {

        return depositService.getFixedDeposit(fdId);
    }

    @PostMapping("/recurring/{accountId}")
    public RecurringDeposit createRecurringDeposit(
            @PathVariable Long accountId,
            @RequestBody SecureDepositRequest request
    ) {

        return depositService
                .createRecurringDepositWithPassword(
                        accountId,
                        request.getUserId(),
                        request.getPassword(),
                        request.getIdempotencyKey(),
                        request.getRd()
                );
    }

    @PutMapping("/recurring/{accountId}/{rdId}")
    public RecurringDeposit updateRecurringDeposit(
            @PathVariable Long accountId,
            @PathVariable Long rdId,
            @RequestBody RecurringDeposit rd) {

        return depositService.updateRecurringDeposit(
                accountId,
                rdId,
                rd
        );
    }

    @PostMapping("/recurring/{rdId}/pay")
    public RecurringDeposit payMonthly(
            @PathVariable Long rdId,
            @RequestBody RecurringInstallmentRequest request
    ) {

        return depositService
                .payRecurringInstallment(
                        rdId,
                        request.getUserId(),
                        request.getPassword(),
                        request.getDueDate()
                );
    }

    @GetMapping("/recurring/account/{accountId}")
    public List<RecurringDeposit> getRecurringDeposits(
            @PathVariable Long accountId) {

        return depositService.getRecurringDeposits(accountId);
    }

    @GetMapping("/recurring/{rdId}")
    public RecurringDeposit getRecurringDeposit(
            @PathVariable Long rdId) {

        return depositService.getRecurringDeposit(rdId);
    }
}