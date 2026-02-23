package com.minted.api.analytics.service;

import com.minted.api.analytics.dto.*;
import com.minted.api.dashboard.dto.ChartDataResponse;
import com.minted.api.account.entity.Account;
import com.minted.api.dashboard.entity.DashboardCard;
import com.minted.api.transaction.entity.Transaction;
import com.minted.api.transaction.enums.TransactionType;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.account.repository.AccountRepository;
import com.minted.api.dashboard.repository.DashboardCardRepository;
import com.minted.api.transaction.repository.TransactionRepository;
import com.minted.api.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final DashboardCardRepository dashboardCardRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                userId, TransactionType.INCOME, startDate, endDate);
        BigDecimal totalExpense = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                userId, TransactionType.EXPENSE, startDate, endDate);

        totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        totalExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;

        BigDecimal netBalance = totalIncome.subtract(totalExpense);
        Long transactionCount = transactionRepository.countByUserIdAndDateBetween(userId, startDate, endDate);

        return new AnalyticsSummaryResponse(totalIncome, totalExpense, netBalance, transactionCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryWiseResponse> getCategoryWise(Long userId, LocalDate startDate, LocalDate endDate, TransactionType type) {
        List<Object[]> results = transactionRepository.sumAmountGroupedByCategory(userId, type, startDate, endDate);

        BigDecimal total = results.stream()
                .map(r -> (BigDecimal) r[2])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return results.stream()
                .map(r -> {
                    Long categoryId = (Long) r[0];
                    String categoryName = (String) r[1];
                    BigDecimal amount = (BigDecimal) r[2];
                    Long count = (Long) r[3];
                    String icon = (String) r[4];
                    String color = (String) r[5];

                    double percentage = total.compareTo(BigDecimal.ZERO) > 0
                            ? amount.multiply(BigDecimal.valueOf(100))
                                    .divide(total, 1, RoundingMode.HALF_UP)
                                    .doubleValue()
                            : 0.0;

                    return new CategoryWiseResponse(categoryId, categoryName, icon, color, amount, count, percentage);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrendResponse> getTrend(Long userId, int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months - 1).withDayOfMonth(1);

        List<Object[]> incomeResults = transactionRepository.sumAmountGroupedByMonth(
                userId, TransactionType.INCOME, startDate, endDate);
        List<Object[]> expenseResults = transactionRepository.sumAmountGroupedByMonth(
                userId, TransactionType.EXPENSE, startDate, endDate);

        Map<String, BigDecimal> incomeMap = new HashMap<>();
        for (Object[] r : incomeResults) {
            String monthKey = String.format("%d-%02d", ((Number) r[0]).intValue(), ((Number) r[1]).intValue());
            incomeMap.put(monthKey, (BigDecimal) r[2]);
        }

        Map<String, BigDecimal> expenseMap = new HashMap<>();
        for (Object[] r : expenseResults) {
            String monthKey = String.format("%d-%02d", ((Number) r[0]).intValue(), ((Number) r[1]).intValue());
            expenseMap.put(monthKey, (BigDecimal) r[2]);
        }

        List<TrendResponse> trend = new ArrayList<>();
        YearMonth current = YearMonth.from(startDate);
        YearMonth end = YearMonth.from(endDate);

        while (!current.isAfter(end)) {
            String monthKey = current.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            BigDecimal income = incomeMap.getOrDefault(monthKey, BigDecimal.ZERO);
            BigDecimal expense = expenseMap.getOrDefault(monthKey, BigDecimal.ZERO);
            BigDecimal net = income.subtract(expense);
            trend.add(new TrendResponse(monthKey, income, expense, net));
            current = current.plusMonths(1);
        }

        return trend;
    }

    @Override
    @Transactional(readOnly = true)
    public ChartDataResponse getCardData(Long userId, Long cardId, LocalDate startDate, LocalDate endDate) {
        DashboardCard card = dashboardCardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Dashboard card not found with id: " + cardId));

        String xAxis = card.getXAxisMeasure();
        String yAxis = card.getYAxisMeasure();

        return switch (xAxis) {
            case "category" -> buildCategoryChartData(userId, startDate, endDate, yAxis);
            case "month" -> buildMonthlyChartData(userId, startDate, endDate, yAxis);
            case "account" -> buildAccountChartData(userId, startDate, endDate, yAxis);
            default -> buildMonthlyChartData(userId, startDate, endDate, yAxis);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpendingActivityResponse> getSpendingActivity(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = transactionRepository.sumExpenseGroupedByDate(userId, startDate, endDate);

        Map<LocalDate, BigDecimal> dailyMap = new LinkedHashMap<>();
        for (Object[] r : results) {
            LocalDate date = (LocalDate) r[0];
            BigDecimal amount = (BigDecimal) r[1];
            dailyMap.put(date, amount);
        }

        List<SpendingActivityResponse> activity = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String dayLabel = current.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            BigDecimal amount = dailyMap.getOrDefault(current, BigDecimal.ZERO);
            activity.add(new SpendingActivityResponse(current.toString(), dayLabel, amount));
            current = current.plusDays(1);
        }

        return activity;
    }

    @Override
    @Transactional(readOnly = true)
    public TotalBalanceResponse getTotalBalance(Long userId) {
        List<Account> activeAccounts = accountRepository.findByUserIdAndIsActiveTrue(userId);
        BigDecimal totalBalance = activeAccounts.stream()
                .map(Account::getBalance)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate now = LocalDate.now();
        LocalDate currentMonthStart = now.withDayOfMonth(1);
        LocalDate currentMonthEnd = now;
        LocalDate prevMonthStart = currentMonthStart.minusMonths(1);
        LocalDate prevMonthEnd = currentMonthStart.minusDays(1);

        BigDecimal currentIncome = Optional.ofNullable(
                transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(userId, TransactionType.INCOME, currentMonthStart, currentMonthEnd)
        ).orElse(BigDecimal.ZERO);
        BigDecimal prevIncome = Optional.ofNullable(
                transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(userId, TransactionType.INCOME, prevMonthStart, prevMonthEnd)
        ).orElse(BigDecimal.ZERO);

        BigDecimal currentExpense = Optional.ofNullable(
                transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(userId, TransactionType.EXPENSE, currentMonthStart, currentMonthEnd)
        ).orElse(BigDecimal.ZERO);
        BigDecimal prevExpense = Optional.ofNullable(
                transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(userId, TransactionType.EXPENSE, prevMonthStart, prevMonthEnd)
        ).orElse(BigDecimal.ZERO);

        BigDecimal previousMonthBalance = totalBalance;

        BigDecimal incomeChangePercent = computeChangePercent(currentIncome, prevIncome);
        BigDecimal expenseChangePercent = computeChangePercent(currentExpense, prevExpense);

        return new TotalBalanceResponse(totalBalance, previousMonthBalance, incomeChangePercent, expenseChangePercent);
    }

    private BigDecimal computeChangePercent(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 1, RoundingMode.HALF_UP);
    }

    private ChartDataResponse buildCategoryChartData(Long userId, LocalDate startDate, LocalDate endDate, String yAxis) {
        List<Object[]> results = transactionRepository.sumAmountGroupedByCategory(
                userId, TransactionType.EXPENSE, startDate, endDate);

        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();
        List<String> colors = new ArrayList<>();

        for (Object[] r : results) {
            labels.add((String) r[1]);
            BigDecimal amount = (BigDecimal) r[2];
            Long count = (Long) r[3];
            String color = (String) r[5];

            data.add("count".equals(yAxis) ? count : amount);
            colors.add(color != null ? color : "#94a3b8");
        }

        ChartDataResponse.ChartDataset dataset = new ChartDataResponse.ChartDataset(
                "Expenses by Category", data, colors);
        return new ChartDataResponse(labels, List.of(dataset));
    }

    private ChartDataResponse buildMonthlyChartData(Long userId, LocalDate startDate, LocalDate endDate, String yAxis) {
        List<Object[]> incomeResults = transactionRepository.sumAmountGroupedByMonth(
                userId, TransactionType.INCOME, startDate, endDate);
        List<Object[]> expenseResults = transactionRepository.sumAmountGroupedByMonth(
                userId, TransactionType.EXPENSE, startDate, endDate);

        List<String> labels = new ArrayList<>();
        Map<String, BigDecimal> incomeMap = new HashMap<>();
        Map<String, BigDecimal> expenseMap = new HashMap<>();

        for (Object[] r : incomeResults) {
            String key = String.format("%d-%02d", ((Number) r[0]).intValue(), ((Number) r[1]).intValue());
            incomeMap.put(key, (BigDecimal) r[2]);
        }
        for (Object[] r : expenseResults) {
            String key = String.format("%d-%02d", ((Number) r[0]).intValue(), ((Number) r[1]).intValue());
            expenseMap.put(key, (BigDecimal) r[2]);
        }

        YearMonth current = YearMonth.from(startDate);
        YearMonth end = YearMonth.from(endDate);
        List<Number> incomeData = new ArrayList<>();
        List<Number> expenseData = new ArrayList<>();

        while (!current.isAfter(end)) {
            String key = current.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            labels.add(current.getMonth().toString().substring(0, 3));
            incomeData.add(incomeMap.getOrDefault(key, BigDecimal.ZERO));
            expenseData.add(expenseMap.getOrDefault(key, BigDecimal.ZERO));
            current = current.plusMonths(1);
        }

        ChartDataResponse.ChartDataset incomeDs = new ChartDataResponse.ChartDataset(
                "Income", incomeData, List.of("#22c55e"));
        ChartDataResponse.ChartDataset expenseDs = new ChartDataResponse.ChartDataset(
                "Expenses", expenseData, List.of("#c48821"));

        return new ChartDataResponse(labels, List.of(incomeDs, expenseDs));
    }

    private ChartDataResponse buildAccountChartData(Long userId, LocalDate startDate, LocalDate endDate, String yAxis) {
        List<Object[]> results = transactionRepository.sumAmountGroupedByAccount(userId, startDate, endDate);

        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();
        List<String> colors = new ArrayList<>();
        String[] palette = {"#c48821", "#166534", "#3b82f6", "#ef4444", "#8b5cf6", "#f59e0b"};

        int i = 0;
        for (Object[] r : results) {
            labels.add((String) r[1]);
            data.add((BigDecimal) r[2]);
            colors.add(palette[i % palette.length]);
            i++;
        }

        ChartDataResponse.ChartDataset dataset = new ChartDataResponse.ChartDataset(
                "By Account", data, colors);
        return new ChartDataResponse(labels, List.of(dataset));
    }
}
