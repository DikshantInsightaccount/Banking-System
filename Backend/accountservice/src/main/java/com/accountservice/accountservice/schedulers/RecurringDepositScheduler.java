package com.accountservice.accountservice.schedulers;

import com.accountservice.accountservice.entity.DepositStatus;
import com.accountservice.accountservice.entity.RecurringDeposit;
import com.accountservice.accountservice.repositories.AccountRepository;
import com.accountservice.accountservice.repositories.RecurringDepositRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class RecurringDepositScheduler {

    private final RecurringDepositRepository recurringDepositRepository;
    private final AccountRepository accountRepository;

    public RecurringDepositScheduler(RecurringDepositRepository recurringDepositRepository,
                                     AccountRepository accountRepository) {
        this.recurringDepositRepository = recurringDepositRepository;
        this.accountRepository = accountRepository;
    }

    @Scheduled(cron = "0 0 1 * * *") // runs daily at 1 AM
    @Transactional
    public void deductMonthlyInstallments() {

        LocalDate today = LocalDate.now();

        List<RecurringDeposit> dueRds =
                recurringDepositRepository.findByStatusAndNextduedateLessThanEqual(
                        DepositStatus.ACTIVE, today);

        for (RecurringDeposit rd : dueRds) {

            if (rd.getPaidmonths() >= rd.getTenuremonths()) {
                rd.setStatus(DepositStatus.MATURED);
                rd.setNextduedate(null);
                recurringDepositRepository.save(rd);
                continue;
            }

            BigDecimal monthly = rd.getMonthlyamount();
            var acc = rd.getAccount();

            if (acc.getBalance().compareTo(monthly) < 0) continue;

            acc.setBalance(acc.getBalance().subtract(monthly));
            accountRepository.save(acc);

            rd.setPaidmonths(rd.getPaidmonths() + 1);
            rd.setLastpaiddate(today);
            rd.setNextduedate(today.plusMonths(1));

            recurringDepositRepository.save(rd);
        }
    }
}
