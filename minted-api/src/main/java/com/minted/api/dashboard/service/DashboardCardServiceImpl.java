package com.minted.api.dashboard.service;

import com.minted.api.dashboard.dto.DashboardCardRequest;
import com.minted.api.dashboard.dto.DashboardCardResponse;
import com.minted.api.dashboard.entity.DashboardCard;
import com.minted.api.user.entity.User;
import com.minted.api.dashboard.enums.CardWidth;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.dashboard.repository.DashboardCardRepository;
import com.minted.api.user.repository.UserRepository;
import com.minted.api.dashboard.service.DashboardCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardCardServiceImpl implements DashboardCardService {

    private final DashboardCardRepository cardRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DashboardCardResponse> getAllActiveByUserId(Long userId) {
        return cardRepository.findByUserIdAndIsActiveTrueOrderByPositionOrderAsc(userId).stream()
                .map(DashboardCardResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardCardResponse> getAllByUserId(Long userId) {
        return cardRepository.findByUserIdOrderByPositionOrderAsc(userId).stream()
                .map(DashboardCardResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardCardResponse getById(Long id, Long userId) {
        DashboardCard card = findCardByIdAndUserId(id, userId);
        return DashboardCardResponse.from(card);
    }

    @Override
    @Transactional
    public DashboardCardResponse create(DashboardCardRequest request, Long userId) {
        User user = findUserById(userId);

        // Get the highest position order and increment
        List<DashboardCard> existingCards = cardRepository.findByUserIdOrderByPositionOrderAsc(userId);
        int nextPosition = existingCards.isEmpty() ? 1 :
                existingCards.get(existingCards.size() - 1).getPositionOrder() + 1;

        DashboardCard card = new DashboardCard();
        card.setTitle(request.title());
        card.setChartType(request.chartType());
        card.setXAxisMeasure(request.xAxisMeasure());
        card.setYAxisMeasure(request.yAxisMeasure());
        card.setFilters(request.filters());
        card.setPositionOrder(request.positionOrder() != null ? request.positionOrder() : nextPosition);
        card.setWidth(request.width() != null ? request.width() : CardWidth.HALF);
        card.setUser(user);
        card.setIsActive(true);

        DashboardCard saved = cardRepository.save(card);
        log.info("DashboardCard created: id={}, title={}", saved.getId(), saved.getTitle());
        return DashboardCardResponse.from(saved);
    }

    @Override
    @Transactional
    public DashboardCardResponse update(Long id, DashboardCardRequest request, Long userId) {
        DashboardCard card = findCardByIdAndUserId(id, userId);

        card.setTitle(request.title());
        card.setChartType(request.chartType());
        card.setXAxisMeasure(request.xAxisMeasure());
        card.setYAxisMeasure(request.yAxisMeasure());
        card.setFilters(request.filters());
        if (request.positionOrder() != null) {
            card.setPositionOrder(request.positionOrder());
        }
        if (request.width() != null) {
            card.setWidth(request.width());
        }

        DashboardCard updated = cardRepository.save(card);
        log.info("DashboardCard updated: id={}", updated.getId());
        return DashboardCardResponse.from(updated);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        DashboardCard card = findCardByIdAndUserId(id, userId);
        cardRepository.delete(card);
        log.info("DashboardCard deleted: id={}", id);
    }

    @Override
    @Transactional
    public void toggleActive(Long id, Long userId) {
        DashboardCard card = findCardByIdAndUserId(id, userId);
        card.setIsActive(!card.getIsActive());
        cardRepository.save(card);
    }

    @Override
    @Transactional
    public void reorderCards(Long userId, List<Long> cardIds) {
        List<DashboardCard> cards = cardIds.stream()
                .map(cardId -> findCardByIdAndUserId(cardId, userId))
                .collect(Collectors.toList());

        for (int i = 0; i < cards.size(); i++) {
            cards.get(i).setPositionOrder(i + 1);
        }

        cardRepository.saveAll(cards);
    }

    private DashboardCard findCardByIdAndUserId(Long id, Long userId) {
        return cardRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Dashboard card not found with id: " + id));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
