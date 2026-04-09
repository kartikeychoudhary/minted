package com.minted.api.integration.entity;

import com.minted.api.split.entity.SplitTransaction;
import com.minted.api.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "split_splitwise_pushes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SplitSplitwisePush {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "split_transaction_id", nullable = false)
    private SplitTransaction splitTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "splitwise_expense_id", nullable = false)
    private Long splitwiseExpenseId;

    @Column(name = "pushed_at")
    private LocalDateTime pushedAt = LocalDateTime.now();
}
