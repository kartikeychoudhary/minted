package com.minted.api.statement.repository;

import com.minted.api.statement.entity.CreditCardStatement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditCardStatementRepository extends JpaRepository<CreditCardStatement, Long> {

    List<CreditCardStatement> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<CreditCardStatement> findByIdAndUserId(Long id, Long userId);

    Page<CreditCardStatement> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
