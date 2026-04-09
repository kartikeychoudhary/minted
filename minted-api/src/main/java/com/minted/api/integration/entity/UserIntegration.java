package com.minted.api.integration.entity;

import com.minted.api.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_integrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "token_type", length = 20)
    private String tokenType = "bearer";

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "splitwise_user_id")
    private Long splitwiseUserId;

    @Column(name = "splitwise_user_name", length = 200)
    private String splitwiseUserName;

    @Column(name = "splitwise_user_email", length = 255)
    private String splitwiseUserEmail;

    @Column(name = "connected_at")
    private LocalDateTime connectedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
