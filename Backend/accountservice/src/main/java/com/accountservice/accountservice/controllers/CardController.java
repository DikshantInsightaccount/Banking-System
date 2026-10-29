package com.accountservice.accountservice.controllers;

import com.accountservice.accountservice.dto.CardControlsRequest;
import com.accountservice.accountservice.dto.LockRequest;
import com.accountservice.accountservice.dto.StatusRequest;
import com.accountservice.accountservice.entity.Card;
import com.accountservice.accountservice.services.CardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cards")
public class CardController {

    private final CardService service;

    public CardController(CardService service) {
        this.service = service;
    }

    //  holderName comes from frontend (customer fullname)
    @GetMapping("/{accountId}")
    public Card getCard(@PathVariable Long accountId,
                        @RequestParam(required = false) String holderName) {
        return service.getOrCreate(accountId, holderName);
    }

    @PutMapping("/{cardId}/controls")
    public Card updateControls(@PathVariable Long cardId,
                               @RequestBody CardControlsRequest req) {
        return service.updateControls(cardId, req);
    }

    @PutMapping("/{cardId}/lock")
    public Card lockUnlock(@PathVariable Long cardId,
                           @RequestBody LockRequest req) {
        return service.setLocked(cardId, req.isLocked());
    }

    @PutMapping("/{cardId}/status")
    public Card activateDeactivate(@PathVariable Long cardId,
                                   @RequestBody StatusRequest req) {
        return service.setStatus(cardId, req.getStatus());
    }
}
