package com.authservice.authservice.service.passkey;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryPasskeyChallengeStore implements PasskeyChallengeStore {

    private final Map<String, ChallengeRecord> store = new ConcurrentHashMap<>();

    @Override
    public String put(ChallengeRecord record) {
        String id = UUID.randomUUID().toString();
        store.put(id, record);
        return id;
    }

    @Override
    public ChallengeRecord consume(String requestId) {
        ChallengeRecord record = store.remove(requestId); // one-time use
        if (record == null) return null;

        long now = Instant.now().toEpochMilli();
        if (now > record.expiresAtEpochMs()) {
            return null;
        }
        return record;
    }
}
