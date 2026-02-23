package com.minted.api.dashboard.entity;

import com.minted.api.dashboard.enums.CardWidth;
import com.minted.api.dashboard.enums.ChartType;
import com.minted.api.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dashboard_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "chart_type", nullable = false)
    private ChartType chartType;

    @Column(name = "x_axis_measure", nullable = false, length = 50)
    private String xAxisMeasure;

    @Column(name = "y_axis_measure", nullable = false, length = 50)
    private String yAxisMeasure;

    @Column(name = "filters", columnDefinition = "JSON")
    private String filters;

    @Column(name = "position_order")
    private Integer positionOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "width")
    private CardWidth width = CardWidth.HALF;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
