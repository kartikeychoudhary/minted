package com.minted.api.service.impl;

import com.minted.api.dto.RecurringTransactionRequest;
import com.minted.api.dto.RecurringTransactionResponse;
import com.minted.api.dto.RecurringSummaryResponse;
import com.minted.api.entity.Account;
import com.minted.api.entity.RecurringTransaction;
import com.minted.api.entity.TransactionCategory;
import com.minted.api.entity.User;
import com.minted.api.enums.RecurringFrequency;
import com.minted.api.enums.RecurringStatus;
import com.minted.api.enums.TransactionType;
import com.minted.api.exception.ResourceNotFoundException;
import com.minted.api.repository.AccountRepository;
import com.minted.api.repository.RecurringTransactionRepository;
import com.minted.api.repository.TransactionCategoryRepository;
import com.minted.api.repository.UserRepository;
import com.minted.api.service.RecurringTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecurringTransactionServiceImpl implements RecurringTransactionService {

    private final RecurringTransactionRepository recurringRepo;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionCategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RecurringTransactionResponse> getAllByUserId(Long userId) {
        return recurringRepo.findByUserId(userId).stream()
                .map(RecurringTransactionResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RecurringTransactionResponse getById(Long id, Long userId) {
        RecurringTransaction entity = recurringRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));
        return RecurringTransactionResponse.from(entity);
    }

    @Override
    @Transactional
    public RecurringTransactionResponse create(RecurringTransactionRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Account account = accountRepository.findByIdAndUserId(request.accountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        TransactionCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        RecurringTransaction entity = new RecurringTransaction();
        entity.setName(request.name());
        entity.setAmount(request.amount());
        entity.setType(TransactionType.valueOf(request.type()));
        entity.setCategory(category);
        entity.setAccount(account);
        entity.setUser(user);
        entity.setFrequency(request.frequency() != null ?
                RecurringFrequency.valueOf(request.frequency()) : RecurringFrequency.MONTHLY);
        entity.setDayOfMonth(request.dayOfMonth() != null ? request.dayOfMonth() : 1);
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setStatus(RecurringStatus.ACTIVE);
        entity.setNextExecutionDate(calculateNextExecutionDate(request.startDate(), request.dayOfMonth() != null ? request.dayOfMonth() : 1));

        RecurringTransaction saved = recurringRepo.save(entity);
        return RecurringTransactionResponse.from(saved);
    }

    @Override
    @Transactional
    public RecurringTransactionResponse update(Long id, RecurringTransactionRequest request, Long userId) {
        RecurringTransaction entity = recurringRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));
        Account account = accountRepository.findByIdAndUserId(request.accountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        TransactionCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        entity.setName(request.name());
        entity.setAmount(request.amount());
        entity.setType(TransactionType.valueOf(request.type()));
        entity.setCategory(category);
        entity.setAccount(account);
        if (request.frequency() != null) {
            entity.setFrequency(RecurringFrequency.valueOf(request.frequency()));
        }
        if (request.dayOfMonth() != null) {
            entity.setDayOfMonth(request.dayOfMonth());
        }
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setNextExecutionDate(calculateNextExecutionDate(
                entity.getStartDate(), entity.getDayOfMonth()));

        RecurringTransaction saved = recurringRepo.save(entity);
        return RecurringTransactionResponse.from(saved);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        RecurringTransaction entity = recurringRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));
        recurringRepo.delete(entity);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id, Long userId) {
        RecurringTransaction entity = recurringRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));
        entity.setStatus(entity.getStatus() == RecurringStatus.ACTIVE ?
                RecurringStatus.PAUSED : RecurringStatus.ACTIVE);
        recurringRepo.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public RecurringSummaryResponse getSummary(Long userId) {
        BigDecimal monthlyExpenses = recurringRepo.sumAmountByUserIdAndStatusAndType(
                userId, RecurringStatus.ACTIVE, TransactionType.EXPENSE);
        BigDecimal monthlyIncome = recurringRepo.sumAmountByUserIdAndStatusAndType(
                userId, RecurringStatus.ACTIVE, TransactionType.INCOME);

        monthlyExpenses = monthlyExpenses != null ? monthlyExpenses : BigDecimal.ZERO;
        monthlyIncome = monthlyIncome != null ? monthlyIncome : BigDecimal.ZERO;

        BigDecimal netFlux = monthlyIncome.subtract(monthlyExpenses);
        Long activeCount = recurringRepo.countByUserIdAndStatus(userId, RecurringStatus.ACTIVE);
        Long pausedCount = recurringRepo.countByUserIdAndStatus(userId, RecurringStatus.PAUSED);

        return new RecurringSummaryResponse(monthlyExpenses, monthlyIncome, netFlux, activeCount, pausedCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringTransactionResponse> search(Long userId, String query) {
        return recurringRepo.searchByName(userId, query).stream()
                .map(RecurringTransactionResponse::from)
                .collect(Collectors.toList());
    }

    public LocalDate calculateNextExecutionDate(LocalDate startDate, int dayOfMonth) {
        LocalDate today = LocalDate.now();
        LocalDate candidate;

        if (today.isBefore(startDate)) {
            // If start is in the future, use start month
            int maxDay = startDate.lengthOfMonth();
            candidate = startDate.withDayOfMonth(Math.min(dayOfMonth, maxDay));
            if (candidate.isBefore(startDate)) {
                candidate = candidate.plusMonths(1);
                maxDay = candidate.lengthOfMonth();
                candidate = candidate.withDayOfMonth(Math.min(dayOfMonth, maxDay));
            }
        } else {
            // Try this month first
            int maxDay = today.lengthOfMonth();
            candidate = today.withDayOfMonth(Math.min(dayOfMonth, maxDay));
            if (!candidate.isAfter(today)) {
                // Already past this month, go to next
                candidate = candidate.plusMonths(1);
                maxDay = candidate.lengthOfMonth();
                candidate = candidate.withDayOfMonth(Math.min(dayOfMonth, maxDay));
            }
        }
        return candidate;
    }
}
