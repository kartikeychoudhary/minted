package com.minted.api.statement.entity;

import com.minted.api.account.entity.Account;
import com.minted.api.job.entity.JobExecution;
import com.minted.api.statement.enums.StatementStatus;
import com.minted.api.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Types;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_card_statements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreditCardStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize = 0L;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(Types.VARCHAR)
    @Column(name = "status", nullable = false, length = 30)
    private StatementStatus status = StatementStatus.UPLOADED;

    @Column(name = "current_step", nullable = false)
    private Integer currentStep = 1;

    @Column(name = "extracted_text", columnDefinition = "LONGTEXT")
    private String extractedText;

    @Column(name = "llm_response_json", columnDefinition = "LONGTEXT")
    private String llmResponseJson;

    @Column(name = "parsed_count")
    private Integer parsedCount = 0;

    @Column(name = "duplicate_count")
    private Integer duplicateCount = 0;

    @Column(name = "imported_count")
    private Integer importedCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_execution_id")
    private JobExecution jobExecution;

    @Column(name = "pdf_password_hint", length = 20)
    private String pdfPasswordHint;

    @Column(name = "file_type", length = 10)
    private String fileType = "PDF";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
