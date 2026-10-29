package com.accountservice.accountservice.services;

import com.accountservice.accountservice.dto.CardControlsRequest;
import com.accountservice.accountservice.entity.Card;
import com.accountservice.accountservice.entity.Status;
import com.accountservice.accountservice.repositories.CardRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;
import java.util.UUID;

@Service
public class CardService {

    private final CardRepository repo;

    public CardService(CardRepository repo) {
        this.repo = repo;
    }

    public Card getOrCreate(Long accountId, String holderName) {

        Card card = repo.findByAccountid(accountId).orElseGet(() -> {
            Card c = new Card();
            c.setAccountid(accountId);
            c.setBankname("IG BANK");
            c.setCardnumber(generateCardNumber());
            c.setExpiry(LocalDate.now().plusYears(5));
            c.setCvv(generateCvv());
            return c;
        });

        // ✅ ALWAYS ensure correct holder name
        if (holderName != null && !holderName.isBlank()) {
            card.setCardholdername(holderName);
        }

        // ✅ Normalize existing cards (if old row had null limits)
        normalize(card);

        return repo.save(card);
    }

    public Card updateControls(Long cardId, CardControlsRequest req) {
        Card card = repo.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        // ✅ HARD RULE: locked card cannot be modified
        if (card.isLocked()) {
            throw new RuntimeException("Card is locked. Controls cannot be changed.");
        }

        card.setAtmEnabled(req.isAtmEnabled());
        card.setAtmLimit(req.getAtmLimit());

        card.setOnlineEnabled(req.isOnlineEnabled());
        card.setOnlineLimit(req.getOnlineLimit());

        card.setContactlessEnabled(req.isContactlessEnabled());
        card.setContactlessLimit(req.getContactlessLimit());

        normalize(card);
        return repo.save(card);
    }

    public Card setLocked(Long cardId, boolean locked) {
        Card card = repo.findById(cardId).orElseThrow(() -> new RuntimeException("Card not found"));
        card.setLocked(locked);
        return repo.save(card);
    }

    public Card setStatus(Long cardId, Status status) {
        Card card = repo.findById(cardId).orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(status);
        return repo.save(card);
    }

    private void normalize(Card card) {
        if (card.getStatus() == null) card.setStatus(Status.ACTIVE);
        if (card.getBankname() == null) card.setBankname("IG BANK");

        if (card.getAtmLimit() == null) card.setAtmLimit(new BigDecimal("20000"));
        if (card.getOnlineLimit() == null) card.setOnlineLimit(new BigDecimal("50000"));
        if (card.getContactlessLimit() == null) card.setContactlessLimit(new BigDecimal("5000"));

        // if booleans were false due to older saved row, keep as-is (user can toggle in UI)
        // but new cards will start enabled by default.
    }

    private String generateCardNumber() {
        return UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 16);
    }

    private String generateCvv() {
        return String.valueOf(new Random().nextInt(900) + 100);
    }
}
