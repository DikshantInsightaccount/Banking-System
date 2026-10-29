package com.accountservice.accountservice.repositories;

import com.accountservice.accountservice.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyRepository
        extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey>
    findByIdempotencyKey(
            String idempotencyKey
    );
}