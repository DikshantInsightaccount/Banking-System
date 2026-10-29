package com.accountservice.accountservice.repositories;

import com.accountservice.accountservice.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByAccountid(Long accountid);
}