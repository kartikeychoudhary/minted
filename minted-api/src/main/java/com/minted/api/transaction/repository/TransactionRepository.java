package com.minted.api.transaction.repository;

import com.minted.api.transaction.entity.Transaction;
import com.minted.api.transaction.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserId(Long userId);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByUserIdAndType(Long userId, TransactionType type);

    List<Transaction> findByUserIdAndAccountId(Long userId, Long accountId);

    List<Transaction> findByUserIdAndCategoryId(Long userId, Long categoryId);

    List<Transaction> findByUserIdAndTransactionDateBetweenAndType(
            Long userId, LocalDate startDate, LocalDate endDate, TransactionType type
    );

    // Named queries defined on Transaction entity

    List<Transaction> findByUserIdAndDateRangeOrderByDateDesc(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    BigDecimal sumAmountByUserIdAndTypeAndDateBetween(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<Transaction> findByFilters(
            @Param("userId") Long userId,
            @Param("accountId") Long accountId,
            @Param("categoryId") Long categoryId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    Long countByUserIdAndDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<Object[]> sumAmountGroupedByCategory(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<Object[]> sumAmountGroupedByMonth(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<Object[]> sumAmountGroupedByAccount(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<Object[]> sumExpenseGroupedByDate(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    boolean existsByTransactionDateAndAmountAndDescriptionAndAccountIdAndUserId(
            LocalDate transactionDate, BigDecimal amount, String description, Long accountId, Long userId);

    boolean existsByAccountIdAndAmountAndTransactionDateBetweenAndDescriptionContainingIgnoreCase(
            Long accountId, BigDecimal amount, LocalDate startDate, LocalDate endDate, String description);

    // Category-exclusion-aware analytics queries

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND (t.excludeFromAnalysis = false OR t.excludeFromAnalysis IS NULL) " +
           "AND t.category.id NOT IN :excludedCategoryIds")
    BigDecimal sumAmountExcludingCategories(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludedCategoryIds") List<Long> excludedCategoryIds
    );

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND (t.excludeFromAnalysis = false OR t.excludeFromAnalysis IS NULL) " +
           "AND t.category.id NOT IN :excludedCategoryIds")
    Long countExcludingCategories(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludedCategoryIds") List<Long> excludedCategoryIds
    );

    @Query("SELECT t.category.id, t.category.name, SUM(t.amount), COUNT(t), t.category.icon, t.category.color " +
           "FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND (t.excludeFromAnalysis = false OR t.excludeFromAnalysis IS NULL) " +
           "AND t.category.id NOT IN :excludedCategoryIds " +
           "GROUP BY t.category.id, t.category.name, t.category.icon, t.category.color " +
           "ORDER BY SUM(t.amount) DESC")
    List<Object[]> sumAmountGroupedByCategoryExcluding(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludedCategoryIds") List<Long> excludedCategoryIds
    );

    @Query("SELECT YEAR(t.transactionDate), MONTH(t.transactionDate), SUM(t.amount) " +
           "FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND (t.excludeFromAnalysis = false OR t.excludeFromAnalysis IS NULL) " +
           "AND t.category.id NOT IN :excludedCategoryIds " +
           "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate) " +
           "ORDER BY YEAR(t.transactionDate), MONTH(t.transactionDate)")
    List<Object[]> sumAmountGroupedByMonthExcluding(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludedCategoryIds") List<Long> excludedCategoryIds
    );

    @Query("SELECT t.account.id, t.account.name, SUM(t.amount) " +
           "FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND (t.excludeFromAnalysis = false OR t.excludeFromAnalysis IS NULL) " +
           "AND t.category.id NOT IN :excludedCategoryIds " +
           "GROUP BY t.account.id, t.account.name " +
           "ORDER BY SUM(t.amount) DESC")
    List<Object[]> sumAmountGroupedByAccountExcluding(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludedCategoryIds") List<Long> excludedCategoryIds
    );

    @Query("SELECT t.transactionDate, SUM(t.amount) " +
           "FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.type = com.minted.api.transaction.enums.TransactionType.EXPENSE " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND (t.excludeFromAnalysis = false OR t.excludeFromAnalysis IS NULL) " +
           "AND t.category.id NOT IN :excludedCategoryIds " +
           "GROUP BY t.transactionDate " +
           "ORDER BY t.transactionDate ASC")
    List<Object[]> sumExpenseGroupedByDateExcluding(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludedCategoryIds") List<Long> excludedCategoryIds
    );
}
