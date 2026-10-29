package com.transactionservice.transactionservice.controllers;

import com.transactionservice.transactionservice.dto.AmountRequest;
import com.transactionservice.transactionservice.dto.StatementItem;
import com.transactionservice.transactionservice.dto.TransferRequest;
import com.transactionservice.transactionservice.entity.Transaction;
import com.transactionservice.transactionservice.entity.Transfer;
import com.transactionservice.transactionservice.services.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // ================= CRUD =================

    @GetMapping("/all")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getOneTransaction(id));
    }

    @PostMapping("/create")
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
        return ResponseEntity.ok(transactionService.save(transaction));
    }

    @PutMapping("/update")
    public ResponseEntity<Transaction> updateTransaction(@RequestBody Transaction transaction) {
        return ResponseEntity.ok(transactionService.updateTransaction(transaction));
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<Transaction> partialUpdate(
            @PathVariable Long id,
            @RequestBody Transaction transaction
    ) {
        return ResponseEntity.ok(transactionService.partialUpdate(id, transaction));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(Map.of("message", "Transaction deleted successfully"));
    }

    // ================= BANKING =================

    @PostMapping("/deposit")
    public ResponseEntity<Transaction> deposit(@RequestBody AmountRequest req) {
        if (req.getAccountNumber() != null && !req.getAccountNumber().isBlank()) {
            return ResponseEntity.ok(transactionService.deposit(req.getAccountNumber(), req.getAmount()));
        }
        return ResponseEntity.ok(transactionService.deposit(req.getAccountId(), req.getAmount()));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Transaction> withdraw(@RequestBody AmountRequest req) {
        if (req.getAccountNumber() != null && !req.getAccountNumber().isBlank()) {
            return ResponseEntity.ok(transactionService.withdraw(req.getAccountNumber(), req.getAmount()));
        }
        return ResponseEntity.ok(transactionService.withdraw(req.getAccountId(), req.getAmount()));
    }

    // UPDATED (SECURE TRANSFER)
    @PostMapping("/transfer")
    public ResponseEntity<Transfer> transfer(@RequestBody TransferRequest req) {
        return ResponseEntity.ok(transactionService.secureTransfer(req));
    }

    // STATEMENT (UNCHANGED)
    @GetMapping("/statement")
    public ResponseEntity<List<StatementItem>> getStatement(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        if (accountNumber != null && !accountNumber.isBlank()) {
            return ResponseEntity.ok(
                    transactionService.getStatementByAccountNumber(accountNumber, from, to)
            );
        }

        return ResponseEntity.ok(
                transactionService.getStatementByAccountId(accountId, from, to)
        );
    }
}