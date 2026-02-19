package com.minted.api.repository;

import com.minted.api.entity.RecurringTransaction;
import com.minted.api.enums.RecurringStatus;
import com.minted.api.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    // Named queries defined on RecurringTransaction entity

    List<RecurringTransaction> findByUserId(@Param("userId") Long userId);

    List<RecurringTransaction> findByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") RecurringStatus status
    );

    Optional<RecurringTransaction> findByIdAndUserId(
            @Param("id") Long id,
            @Param("userId") Long userId
    );

    BigDecimal sumAmountByUserIdAndStatusAndType(
            @Param("userId") Long userId,
            @Param("status") RecurringStatus status,
            @Param("type") TransactionType type
    );

    Long countByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") RecurringStatus status
    );

    List<RecurringTransaction> searchByName(
            @Param("userId") Long userId,
            @Param("search") String search
    );

    List<RecurringTransaction> findByStatusAndNextExecutionDateLessThanEqual(
            RecurringStatus status,
            java.time.LocalDate date
    );

    // Spring Data derived query for pagination
    Page<RecurringTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
