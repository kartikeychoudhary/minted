package com.minted.api.transaction.service;

import com.minted.api.transaction.dto.TransactionRequest;
import com.minted.api.transaction.dto.TransactionResponse;
import com.minted.api.account.entity.Account;
import com.minted.api.transaction.entity.Transaction;
import com.minted.api.transaction.entity.TransactionCategory;
import com.minted.api.user.entity.User;
import com.minted.api.transaction.enums.TransactionType;
import com.minted.api.common.exception.BadRequestException;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.account.repository.AccountRepository;
import com.minted.api.transaction.repository.TransactionCategoryRepository;
import com.minted.api.transaction.repository.TransactionRepository;
import com.minted.api.user.repository.UserRepository;
import com.minted.api.split.repository.SplitTransactionRepository;
import com.minted.api.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final SplitTransactionRepository splitTransactionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllByUserId(Long userId) {
        Set<Long> splitIds = new HashSet<>(splitTransactionRepository.findSourceTransactionIdsByUserId(userId));
        return transactionRepository.findByUserId(userId).stream()
                .map(t -> TransactionResponse.from(t, splitIds.contains(t.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        Set<Long> splitIds = new HashSet<>(splitTransactionRepository.findSourceTransactionIdsByUserId(userId));
        return transactionRepository.findByUserIdAndDateRangeOrderByDateDesc(userId, startDate, endDate).stream()
                .map(t -> TransactionResponse.from(t, splitIds.contains(t.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllByFilters(Long userId, Long accountId, Long categoryId,
                                                     TransactionType type, LocalDate startDate, LocalDate endDate) {
        Set<Long> splitIds = new HashSet<>(splitTransactionRepository.findSourceTransactionIdsByUserId(userId));
        return transactionRepository.findByFilters(userId, accountId, categoryId, type, startDate, endDate).stream()
                .map(t -> TransactionResponse.from(t, splitIds.contains(t.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getById(Long id, Long userId) {
        Set<Long> splitIds = new HashSet<>(splitTransactionRepository.findSourceTransactionIdsByUserId(userId));
        Transaction transaction = findTransactionByIdAndUserId(id, userId);
        return TransactionResponse.from(transaction, splitIds.contains(transaction.getId()));
    }

    @Override
    @Transactional
    public TransactionResponse create(TransactionRequest request, Long userId) {
        User user = findUserById(userId);
        Account account = findAccountByIdAndUserId(request.accountId(), userId);
        TransactionCategory category = findCategoryByIdAndUserId(request.categoryId(), userId);

        // Validate transaction type matches category type
        if (!request.type().equals(category.getType())) {
            throw new BadRequestException("Transaction type must match category type");
        }

        Account toAccount = null;
        if (request.type() == TransactionType.TRANSFER) {
            if (request.toAccountId() == null) {
                throw new BadRequestException("To account is required for transfer transactions");
            }
            toAccount = findAccountByIdAndUserId(request.toAccountId(), userId);
            if (account.getId().equals(toAccount.getId())) {
                throw new BadRequestException("Source and destination accounts cannot be the same");
            }
        }

        Transaction transaction = new Transaction();
        transaction.setAmount(request.amount());
        transaction.setType(request.type());
        transaction.setDescription(request.description());
        transaction.setNotes(request.notes());
        transaction.setTransactionDate(request.transactionDate());
        transaction.setAccount(account);
        transaction.setToAccount(toAccount);
        transaction.setCategory(category);
        transaction.setUser(user);
        transaction.setIsRecurring(request.isRecurring() != null ? request.isRecurring() : false);
        transaction.setTags(request.tags());

        Transaction saved = transactionRepository.save(transaction);

        // Update account balances
        updateAccountBalancesForCreate(account, toAccount, request.type(), request.amount());

        log.info("Transaction created: id={}, type={}, amount={}", saved.getId(), saved.getType(), saved.getAmount());
        return TransactionResponse.from(saved, false);
    }

    @Override
    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request, Long userId) {
        Transaction transaction = findTransactionByIdAndUserId(id, userId);

        // Revert old balance changes
        reverseAccountBalances(transaction);

        Account account = findAccountByIdAndUserId(request.accountId(), userId);
        TransactionCategory category = findCategoryByIdAndUserId(request.categoryId(), userId);

        if (!request.type().equals(category.getType())) {
            throw new BadRequestException("Transaction type must match category type");
        }

        Account toAccount = null;
        if (request.type() == TransactionType.TRANSFER) {
            if (request.toAccountId() == null) {
                throw new BadRequestException("To account is required for transfer transactions");
            }
            toAccount = findAccountByIdAndUserId(request.toAccountId(), userId);
            if (account.getId().equals(toAccount.getId())) {
                throw new BadRequestException("Source and destination accounts cannot be the same");
            }
        }

        transaction.setAmount(request.amount());
        transaction.setType(request.type());
        transaction.setDescription(request.description());
        transaction.setNotes(request.notes());
        transaction.setTransactionDate(request.transactionDate());
        transaction.setAccount(account);
        transaction.setToAccount(toAccount);
        transaction.setCategory(category);
        transaction.setIsRecurring(request.isRecurring() != null ? request.isRecurring() : false);
        transaction.setTags(request.tags());

        Transaction updated = transactionRepository.save(transaction);

        // Apply new balance changes
        updateAccountBalancesForCreate(account, toAccount, request.type(), request.amount());

        log.info("Transaction updated: id={}", updated.getId());
        Set<Long> splitIds = new HashSet<>(splitTransactionRepository.findSourceTransactionIdsByUserId(userId));
        return TransactionResponse.from(updated, splitIds.contains(updated.getId()));
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        Transaction transaction = findTransactionByIdAndUserId(id, userId);

        // Revert balance changes
        reverseAccountBalances(transaction);

        transactionRepository.delete(transaction);
        log.info("Transaction deleted: id={}", id);
    }

    private void updateAccountBalancesForCreate(Account account, Account toAccount,
                                               TransactionType type, java.math.BigDecimal amount) {
        switch (type) {
            case INCOME:
                account.setBalance(account.getBalance().add(amount));
                accountRepository.save(account);
                break;
            case EXPENSE:
                account.setBalance(account.getBalance().subtract(amount));
                accountRepository.save(account);
                break;
            case TRANSFER:
                account.setBalance(account.getBalance().subtract(amount));
                toAccount.setBalance(toAccount.getBalance().add(amount));
                accountRepository.save(account);
                accountRepository.save(toAccount);
                break;
        }
    }

    private void reverseAccountBalances(Transaction transaction) {
        Account account = transaction.getAccount();
        Account toAccount = transaction.getToAccount();

        switch (transaction.getType()) {
            case INCOME:
                account.setBalance(account.getBalance().subtract(transaction.getAmount()));
                accountRepository.save(account);
                break;
            case EXPENSE:
                account.setBalance(account.getBalance().add(transaction.getAmount()));
                accountRepository.save(account);
                break;
            case TRANSFER:
                account.setBalance(account.getBalance().add(transaction.getAmount()));
                toAccount.setBalance(toAccount.getBalance().subtract(transaction.getAmount()));
                accountRepository.save(account);
                accountRepository.save(toAccount);
                break;
        }
    }

    private Transaction findTransactionByIdAndUserId(Long id, Long userId) {
        return transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
    }

    private Account findAccountByIdAndUserId(Long accountId, Long userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
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
