package com.minted.api.transaction.entity;

import com.minted.api.account.entity.Account;
import com.minted.api.transaction.enums.TransactionType;
import com.minted.api.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@NamedQueries({
    @NamedQuery(
        name = "Transaction.findByUserIdAndDateRangeOrderByDateDesc",
        query = "SELECT t FROM Transaction t WHERE t.user.id = :userId " +
                "AND t.transactionDate BETWEEN :startDate AND :endDate " +
                "ORDER BY t.transactionDate DESC, t.createdAt DESC"
    ),
    @NamedQuery(
        name = "Transaction.sumAmountByUserIdAndTypeAndDateBetween",
        query = "SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId " +
                "AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate " +
                "AND (t.excludeFromAnalysis = false OR t.excludeFromAnalysis IS NULL)"
    ),
    @NamedQuery(
        name = "Transaction.findByFilters",
        query = "SELECT t FROM Transaction t WHERE t.user.id = :userId " +
                "AND (:accountId IS NULL OR t.account.id = :accountId) " +
                "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
                "AND (:type IS NULL OR t.type = :type) " +
                "AND t.transactionDate BETWEEN :startDate AND :endDate " +
                "ORDER BY t.transactionDate DESC"
    ),
    @NamedQuery(
        name = "Transaction.countByUserIdAndDateBetween",
        query = "SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId " +
                "AND t.transactionDate BETWEEN :startDate AND :endDate"
    ),
    @NamedQuery(
        name = "Transaction.sumAmountGroupedByCategory",
        query = "SELECT t.category.id, t.category.name, SUM(t.amount), COUNT(t), t.category.icon, t.category.color " +
                "FROM Transaction t WHERE t.user.id = :userId " +
                "AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate " +
                "AND (t.excludeFromAnalysis = false OR t.excludeFromAnalysis IS NULL) " +
                "GROUP BY t.category.id, t.category.name, t.category.icon, t.category.color " +
                "ORDER BY SUM(t.amount) DESC"
    ),
    @NamedQuery(
        name = "Transaction.sumAmountGroupedByMonth",
        query = "SELECT YEAR(t.transactionDate), MONTH(t.transactionDate), SUM(t.amount) " +
                "FROM Transaction t WHERE t.user.id = :userId " +
                "AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate " +
                "AND (t.excludeFromAnalysis = false OR t.excludeFromAnalysis IS NULL) " +
                "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate) " +
                "ORDER BY YEAR(t.transactionDate), MONTH(t.transactionDate)"
    ),
    @NamedQuery(
        name = "Transaction.sumAmountGroupedByAccount",
        query = "SELECT t.account.id, t.account.name, SUM(t.amount) " +
                "FROM Transaction t WHERE t.user.id = :userId " +
                "AND t.transactionDate BETWEEN :startDate AND :endDate " +
                "AND (t.excludeFromAnalysis = false OR t.excludeFromAnalysis IS NULL) " +
                "GROUP BY t.account.id, t.account.name " +
                "ORDER BY SUM(t.amount) DESC"
    ),
    @NamedQuery(
        name = "Transaction.sumExpenseGroupedByDate",
        query = "SELECT t.transactionDate, SUM(t.amount) " +
                "FROM Transaction t WHERE t.user.id = :userId " +
                "AND t.type = com.minted.api.transaction.enums.TransactionType.EXPENSE " +
                "AND t.transactionDate BETWEEN :startDate AND :endDate " +
                "AND (t.excludeFromAnalysis = false OR t.excludeFromAnalysis IS NULL) " +
                "GROUP BY t.transactionDate " +
                "ORDER BY t.transactionDate ASC"
    )
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    private Account toAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private TransactionCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_recurring")
    private Boolean isRecurring = false;

    @Column(name = "exclude_from_analysis")
    private Boolean excludeFromAnalysis = false;

    @Column(name = "tags", length = 500)
    private String tags;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
