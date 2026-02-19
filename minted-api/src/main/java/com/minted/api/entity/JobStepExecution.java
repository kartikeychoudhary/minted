package com.minted.api.entity;

import com.minted.api.enums.JobStepStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import java.sql.Types;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_step_executions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobStepExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_execution_id", nullable = false)
    private JobExecution jobExecution;

    @Column(name = "step_name", nullable = false, length = 200)
    private String stepName;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(Types.VARCHAR)
    @Column(nullable = false, length = 20)
    private JobStepStatus status = JobStepStatus.PENDING;

    @Column(name = "context_json", columnDefinition = "TEXT")
    private String contextJson;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
