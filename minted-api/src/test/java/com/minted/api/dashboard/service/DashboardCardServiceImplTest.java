package com.minted.api.dashboard.service;

import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.dashboard.dto.DashboardCardRequest;
import com.minted.api.dashboard.dto.DashboardCardResponse;
import com.minted.api.dashboard.entity.DashboardCard;
import com.minted.api.dashboard.enums.CardWidth;
import com.minted.api.dashboard.enums.ChartType;
import com.minted.api.dashboard.repository.DashboardCardRepository;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardCardServiceImplTest {

    @Mock private DashboardCardRepository cardRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private DashboardCardServiceImpl dashboardCardService;

    // ── getAllActiveByUserId ───────────────────────────────────────────────────

    @Test
    void getAllActiveByUserId_returnsList() {
        DashboardCard card = buildCard(1L, "Revenue", true);
        when(cardRepository.findByUserIdAndIsActiveTrueOrderByPositionOrderAsc(1L)).thenReturn(List.of(card));

        List<DashboardCardResponse> result = dashboardCardService.getAllActiveByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Revenue");
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_notFound_throwsResourceNotFound() {
        when(cardRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dashboardCardService.getById(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_success_autoAssignsPosition() {
        User user = buildUser(1L);
        DashboardCard saved = buildCard(10L, "Revenue", true);
        DashboardCard existing = buildCard(5L, "Expenses", true);
        existing.setPositionOrder(3);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.findByUserIdOrderByPositionOrderAsc(1L)).thenReturn(List.of(existing));
        when(cardRepository.save(any(DashboardCard.class))).thenReturn(saved);

        DashboardCardRequest request = new DashboardCardRequest("Revenue", ChartType.BAR, "month", "amount", null, null, null);
        DashboardCardResponse response = dashboardCardService.create(request, 1L);

        assertThat(response.id()).isEqualTo(10L);
        verify(cardRepository).save(any(DashboardCard.class));
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_success() {
        DashboardCard card = buildCard(1L, "Revenue", true);
        when(cardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(card));

        dashboardCardService.delete(1L, 1L);

        verify(cardRepository).delete(card);
    }

    // ── toggleActive ──────────────────────────────────────────────────────────

    @Test
    void toggleActive_activatedBecomesInactive() {
        DashboardCard card = buildCard(1L, "Revenue", true);
        when(cardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);

        dashboardCardService.toggleActive(1L, 1L);

        assertThat(card.getIsActive()).isFalse();
    }

    // ── reorderCards ──────────────────────────────────────────────────────────

    @Test
    void reorderCards_updatesPositionOrder() {
        DashboardCard card1 = buildCard(1L, "A", true); card1.setPositionOrder(2);
        DashboardCard card2 = buildCard(2L, "B", true); card2.setPositionOrder(1);
        when(cardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(card1));
        when(cardRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(card2));

        dashboardCardService.reorderCards(1L, List.of(1L, 2L));

        assertThat(card1.getPositionOrder()).isEqualTo(1);
        assertThat(card2.getPositionOrder()).isEqualTo(2);
        verify(cardRepository).saveAll(any());
    }

    // helpers

    private DashboardCard buildCard(Long id, String title, boolean isActive) {
        DashboardCard card = new DashboardCard();
        card.setId(id);
        card.setTitle(title);
        card.setChartType(ChartType.BAR);
        card.setXAxisMeasure("month");
        card.setYAxisMeasure("amount");
        card.setIsActive(isActive);
        card.setPositionOrder(1);
        card.setWidth(CardWidth.HALF);
        return card;
    }

    private User buildUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("alice");
        return u;
    }
}
