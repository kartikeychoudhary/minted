package com.minted.api.recurring.entity;

import com.minted.api.account.entity.Account;
import com.minted.api.recurring.enums.RecurringFrequency;
import com.minted.api.recurring.enums.RecurringStatus;
import com.minted.api.transaction.entity.TransactionCategory;
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
@Table(name = "recurring_transactions")
@NamedQueries({
    @NamedQuery(
        name = "RecurringTransaction.findByUserId",
        query = "SELECT r FROM RecurringTransaction r WHERE r.user.id = :userId ORDER BY r.createdAt DESC"
    ),
    @NamedQuery(
        name = "RecurringTransaction.findByUserIdAndStatus",
        query = "SELECT r FROM RecurringTransaction r WHERE r.user.id = :userId AND r.status = :status ORDER BY r.nextExecutionDate ASC"
    ),
    @NamedQuery(
        name = "RecurringTransaction.findByIdAndUserId",
        query = "SELECT r FROM RecurringTransaction r WHERE r.id = :id AND r.user.id = :userId"
    ),
    @NamedQuery(
        name = "RecurringTransaction.sumAmountByUserIdAndStatusAndType",
        query = "SELECT COALESCE(SUM(r.amount), 0) FROM RecurringTransaction r WHERE r.user.id = :userId AND r.status = :status AND r.type = :type"
    ),
    @NamedQuery(
        name = "RecurringTransaction.countByUserIdAndStatus",
        query = "SELECT COUNT(r) FROM RecurringTransaction r WHERE r.user.id = :userId AND r.status = :status"
    ),
    @NamedQuery(
        name = "RecurringTransaction.searchByName",
        query = "SELECT r FROM RecurringTransaction r WHERE r.user.id = :userId AND LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY r.createdAt DESC"
    )
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private TransactionCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false)
    private RecurringFrequency frequency = RecurringFrequency.MONTHLY;

    @Column(name = "day_of_month", nullable = false)
    private Integer dayOfMonth = 1;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RecurringStatus status = RecurringStatus.ACTIVE;

    @Column(name = "next_execution_date")
    private LocalDate nextExecutionDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
