package com.minted.api.budget.service;

import com.minted.api.budget.dto.BudgetRequest;
import com.minted.api.budget.dto.BudgetResponse;
import com.minted.api.budget.entity.Budget;
import com.minted.api.transaction.entity.TransactionCategory;
import com.minted.api.user.entity.User;
import com.minted.api.common.exception.BadRequestException;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.budget.repository.BudgetRepository;
import com.minted.api.transaction.repository.TransactionCategoryRepository;
import com.minted.api.user.repository.UserRepository;
import com.minted.api.budget.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionCategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getAllByUserId(Long userId) {
        return budgetRepository.findByUserId(userId).stream()
                .map(BudgetResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getAllByUserIdAndMonthYear(Long userId, Integer month, Integer year) {
        return budgetRepository.findByUserIdAndMonthAndYear(userId, month, year).stream()
                .map(BudgetResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getAllByUserIdAndYear(Long userId, Integer year) {
        return budgetRepository.findByUserIdAndYear(userId, year).stream()
                .map(BudgetResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse getById(Long id, Long userId) {
        Budget budget = findBudgetByIdAndUserId(id, userId);
        return BudgetResponse.from(budget);
    }

    @Override
    @Transactional
    public BudgetResponse create(BudgetRequest request, Long userId) {
        // Check if budget already exists for this month/year/category combination
        if (budgetRepository.existsByUserIdAndMonthAndYearAndCategoryId(
                userId, request.month(), request.year(), request.categoryId())) {
            throw new com.minted.api.common.exception.DuplicateResourceException("Budget already exists for this month, year, and category");
        }

        User user = findUserById(userId);
        TransactionCategory category = null;
        if (request.categoryId() != null) {
            category = findCategoryByIdAndUserId(request.categoryId(), userId);
        }

        Budget budget = new Budget();
        budget.setName(request.name());
        budget.setAmount(request.amount());
        budget.setMonth(request.month());
        budget.setYear(request.year());
        budget.setCategory(category);
        budget.setUser(user);

        Budget saved = budgetRepository.save(budget);
        log.info("Budget created: id={}, name={}", saved.getId(), saved.getName());
        return BudgetResponse.from(saved);
    }

    @Override
    @Transactional
    public BudgetResponse update(Long id, BudgetRequest request, Long userId) {
        Budget budget = findBudgetByIdAndUserId(id, userId);

        // Check if month/year/category combination is changing and if it already exists
        boolean isChanging = !budget.getMonth().equals(request.month()) ||
                !budget.getYear().equals(request.year()) ||
                (budget.getCategory() != null && !budget.getCategory().getId().equals(request.categoryId())) ||
                (budget.getCategory() == null && request.categoryId() != null);

        if (isChanging && budgetRepository.existsByUserIdAndMonthAndYearAndCategoryId(
                userId, request.month(), request.year(), request.categoryId())) {
            throw new com.minted.api.common.exception.DuplicateResourceException("Budget already exists for this month, year, and category");
        }

        TransactionCategory category = null;
        if (request.categoryId() != null) {
            category = findCategoryByIdAndUserId(request.categoryId(), userId);
        }

        budget.setName(request.name());
        budget.setAmount(request.amount());
        budget.setMonth(request.month());
        budget.setYear(request.year());
        budget.setCategory(category);

        Budget updated = budgetRepository.save(budget);
        log.info("Budget updated: id={}", updated.getId());
        return BudgetResponse.from(updated);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        Budget budget = findBudgetByIdAndUserId(id, userId);
        budgetRepository.delete(budget);
        log.info("Budget deleted: id={}", id);
    }

    private Budget findBudgetByIdAndUserId(Long id, Long userId) {
        return budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
    }

    private TransactionCategory findCategoryByIdAndUserId(Long categoryId, Long userId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction category not found with id: " + categoryId));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
