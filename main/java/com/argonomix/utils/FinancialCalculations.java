package com.argonomix.utils;

import com.argonomix.database.*;
import com.argonomix.models.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class FinancialCalculations {
    
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("MMM dd");
    
    public static double calculateTotalBalance(int userId) throws Exception {
        TransactionDAO transactionDAO = new TransactionDAO();
        List<Transaction> transactions = transactionDAO.findByUserId(userId);
        
        return transactions.stream()
            .mapToDouble(t -> {
                double amount = t.getAmount();
                return t.getTransactionType() == Transaction.TransactionType.INCOME ? amount : -amount;
            })
            .sum();
    }
    
    public static double calculateThisMonthSpending(int userId) throws Exception {
        TransactionDAO transactionDAO = new TransactionDAO();
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        
        List<Transaction> transactions = transactionDAO.findByUserIdAndDateRange(userId, monthStart, monthEnd);
        
        return transactions.stream()
            .filter(t -> t.getTransactionType() == Transaction.TransactionType.EXPENSE)
            .mapToDouble(Transaction::getAmount)
            .sum();
    }
    
    public static double calculateSavingsRate(int userId) throws Exception {
        TransactionDAO transactionDAO = new TransactionDAO();
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        
        List<Transaction> transactions = transactionDAO.findByUserIdAndDateRange(userId, monthStart, monthEnd);
        
        double income = transactions.stream()
            .filter(t -> t.getTransactionType() == Transaction.TransactionType.INCOME)
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        double expenses = transactions.stream()
            .filter(t -> t.getTransactionType() == Transaction.TransactionType.EXPENSE)
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        if (income == 0) return 0;
        double rate = ((income - expenses) / income) * 100;
        return Double.isNaN(rate) ? 0 : rate;
    }
    
    public static class HealthScoreResult {
        private int score;
        private Map<String, Integer> breakdown;
        
        public HealthScoreResult(int score, Map<String, Integer> breakdown) {
            this.score = score;
            this.breakdown = breakdown;
        }
        
        public int getScore() { return score; }
        public Map<String, Integer> getBreakdown() { return breakdown; }
    }
    
    public static HealthScoreResult calculateFinancialHealthScore(int userId) throws Exception {
        TransactionDAO transactionDAO = new TransactionDAO();
        BudgetDAO budgetDAO = new BudgetDAO();
        GoalDAO goalDAO = new GoalDAO();
        UserDAO userDAO = new UserDAO();
        
        User user = userDAO.findById(userId);
        if (user == null) {
            return new HealthScoreResult(0, new HashMap<>());
        }
        
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        
        List<Transaction> transactions = transactionDAO.findByUserIdAndDateRange(userId, monthStart, monthEnd);
        
        double monthlyIncome = transactions.stream()
            .filter(t -> t.getTransactionType() == Transaction.TransactionType.INCOME)
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        if (monthlyIncome == 0 && user.getMonthlyIncome() > 0) {
            monthlyIncome = user.getMonthlyIncome();
        }
        
        double monthlyExpenses = transactions.stream()
            .filter(t -> t.getTransactionType() == Transaction.TransactionType.EXPENSE)
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        double monthlySavings = monthlyIncome - monthlyExpenses;
        
        // 1. Savings Rate (30%)
        double savingsRate = monthlyIncome > 0 ? (monthlySavings / monthlyIncome) * 100 : 0;
        double savingsRateScore = Math.min(savingsRate / 20 * 30, 30);
        
        // 2. Budget Adherence (25%)
        double budgetAdherenceScore = 25;
        List<Budget> budgets = budgetDAO.findByUserId(userId);
        if (!budgets.isEmpty()) {
            List<Double> overspending = budgets.stream()
                .map(budget -> {
                    double categorySpending = transactions.stream()
                        .filter(t -> t.getCategory() != null && 
                                   t.getCategory().equals(budget.getCategory()) &&
                                   t.getTransactionType() == Transaction.TransactionType.EXPENSE)
                        .mapToDouble(Transaction::getAmount)
                        .sum();
                    double limit = budget.getAmountLimit();
                    if (limit == 0) return 0.0;
                    return Math.max(0.0, (categorySpending - limit) / limit * 100);
                })
                .collect(Collectors.toList());
            
            if (!overspending.isEmpty()) {
                double avgOverspending = overspending.stream().mapToDouble(Double::doubleValue).sum() / overspending.size();
                budgetAdherenceScore = Math.max(0, (1 - avgOverspending / 100) * 25);
            }
        }
        
        // 3. Emergency Fund Coverage (25%)
        List<Goal> goals = goalDAO.findByUserId(userId);
        double totalSavings = goals.stream()
            .mapToDouble(Goal::getCurrentAmount)
            .sum();
        double emergencyFundMonths = monthlyExpenses > 0 ? totalSavings / monthlyExpenses : 0;
        double emergencyFundScore = Math.min((emergencyFundMonths / 6) * 25, 25);
        
        // 4. Debt-to-Income Ratio (20%)
        double debtToIncomeScore = 20; // Full score if no debt
        
        int totalScore = (int) Math.round(savingsRateScore + budgetAdherenceScore + emergencyFundScore + debtToIncomeScore);
        
        Map<String, Integer> breakdown = new HashMap<>();
        breakdown.put("savingsRate", (int) Math.round(savingsRateScore));
        breakdown.put("budgetAdherence", (int) Math.round(budgetAdherenceScore));
        breakdown.put("emergencyFund", (int) Math.round(emergencyFundScore));
        breakdown.put("debtToIncome", (int) Math.round(debtToIncomeScore));
        
        return new HealthScoreResult(totalScore, breakdown);
    }
    
    public static class CategorySpending {
        private String category;
        private double amount;
        
        public CategorySpending(String category, double amount) {
            this.category = category;
            this.amount = amount;
        }
        
        public String getCategory() { return category; }
        public double getAmount() { return amount; }
    }
    
    public static List<CategorySpending> calculateSpendingByCategory(int userId, String dateRange) throws Exception {
        TransactionDAO transactionDAO = new TransactionDAO();
        LocalDate now = LocalDate.now();
        LocalDate start, end;
        
        switch (dateRange) {
            case "week":
                start = now.minusDays(7);
                end = now;
                break;
            case "month":
                start = now.withDayOfMonth(1);
                end = now.withDayOfMonth(now.lengthOfMonth());
                break;
            case "3months":
                start = now.minusMonths(3);
                end = now;
                break;
            default:
                start = now.minusMonths(6);
                end = now;
        }
        
        List<Transaction> transactions = transactionDAO.findByUserIdAndDateRange(userId, start, end);
        
        Map<String, Double> categorySpending = transactions.stream()
            .filter(t -> t.getTransactionType() == Transaction.TransactionType.EXPENSE)
            .collect(Collectors.groupingBy(
                t -> t.getCategory() != null ? t.getCategory() : "Other",
                Collectors.summingDouble(Transaction::getAmount)
            ));
        
        return categorySpending.entrySet().stream()
            .map(e -> new CategorySpending(e.getKey(), e.getValue()))
            .sorted((a, b) -> Double.compare(b.getAmount(), a.getAmount()))
            .collect(Collectors.toList());
    }
    
    public static class SpendingTrendData {
        private String date;
        private double amount;
        
        public SpendingTrendData(String date, double amount) {
            this.date = date;
            this.amount = amount;
        }
        
        public String getDate() { return date; }
        public double getAmount() { return amount; }
    }
    
    public static List<SpendingTrendData> calculateSpendingTrend(int userId, int days) throws Exception {
        TransactionDAO transactionDAO = new TransactionDAO();
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusDays(days);
        
        List<Transaction> transactions = transactionDAO.findByUserIdAndDateRange(userId, start, now);
        
        Map<String, Double> dailySpending = transactions.stream()
            .filter(t -> t.getTransactionType() == Transaction.TransactionType.EXPENSE)
            .collect(Collectors.groupingBy(
                t -> t.getTransactionDate().toString(),
                Collectors.summingDouble(Transaction::getAmount)
            ));
        
        List<SpendingTrendData> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = start.plusDays(i);
            String dateKey = date.toString();
            String formattedDate = date.format(DAY_FORMATTER);
            double amount = dailySpending.getOrDefault(dateKey, 0.0);
            result.add(new SpendingTrendData(formattedDate, amount));
        }
        
        return result;
    }
    
    public static class MonthlyData {
        private String month;
        private double income;
        private double expenses;
        private double net;
        
        public MonthlyData(String month, double income, double expenses, double net) {
            this.month = month;
            this.income = income;
            this.expenses = expenses;
            this.net = net;
        }
        
        public String getMonth() { return month; }
        public double getIncome() { return income; }
        public double getExpenses() { return expenses; }
        public double getNet() { return net; }
    }
    
    public static List<MonthlyData> calculateMonthlyIncomeVsExpenses(int userId, int months) throws Exception {
        TransactionDAO transactionDAO = new TransactionDAO();
        List<MonthlyData> result = new ArrayList<>();
        
        for (int i = months - 1; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.now().minusMonths(i);
            LocalDate monthStart = yearMonth.atDay(1);
            LocalDate monthEnd = yearMonth.atEndOfMonth();
            
            List<Transaction> transactions = transactionDAO.findByUserIdAndDateRange(userId, monthStart, monthEnd);
            
            double income = transactions.stream()
                .filter(t -> t.getTransactionType() == Transaction.TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
            
            double expenses = transactions.stream()
                .filter(t -> t.getTransactionType() == Transaction.TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
            
            result.add(new MonthlyData(yearMonth.format(MONTH_FORMATTER), income, expenses, income - expenses));
        }
        
        return result;
    }
    
    public static class NetWorthResult {
        private double assets;
        private double liabilities;
        private double netWorth;
        
        public NetWorthResult(double assets, double liabilities, double netWorth) {
            this.assets = assets;
            this.liabilities = liabilities;
            this.netWorth = netWorth;
        }
        
        public double getAssets() { return assets; }
        public double getLiabilities() { return liabilities; }
        public double getNetWorth() { return netWorth; }
    }
    
    public static NetWorthResult calculateNetWorth(int userId) throws Exception {
        double totalBalance = calculateTotalBalance(userId);
        
        InvestmentDAO investmentDAO = new InvestmentDAO();
        List<Investment> investments = investmentDAO.findByUserId(userId);
        double investmentValue = investments.stream()
            .mapToDouble(Investment::getCurrentValue)
            .sum();
        
        double assets = totalBalance + investmentValue;
        double liabilities = 0; // Assuming no debt tracking
        
        return new NetWorthResult(assets, liabilities, assets - liabilities);
    }
    
    public static class CashFlowForecast {
        private String month;
        private double forecast;
        
        public CashFlowForecast(String month, double forecast) {
            this.month = month;
            this.forecast = forecast;
        }
        
        public String getMonth() { return month; }
        public double getForecast() { return forecast; }
    }
    
    public static List<CashFlowForecast> calculateCashFlowForecast(int userId) throws Exception {
        TransactionDAO transactionDAO = new TransactionDAO();
        
        // Get last 3 months average
        List<Double> last3Months = new ArrayList<>();
        for (int i = 2; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.now().minusMonths(i);
            LocalDate monthStart = yearMonth.atDay(1);
            LocalDate monthEnd = yearMonth.atEndOfMonth();
            
            List<Transaction> transactions = transactionDAO.findByUserIdAndDateRange(userId, monthStart, monthEnd);
            
            double income = transactions.stream()
                .filter(t -> t.getTransactionType() == Transaction.TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
            
            double expenses = transactions.stream()
                .filter(t -> t.getTransactionType() == Transaction.TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
            
            last3Months.add(income - expenses);
        }
        
        double avgCashFlow = last3Months.stream().mapToDouble(Double::doubleValue).sum() / last3Months.size();
        
        // Forecast next 3 months
        List<CashFlowForecast> result = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            YearMonth yearMonth = YearMonth.now().plusMonths(i);
            result.add(new CashFlowForecast(yearMonth.format(MONTH_FORMATTER), avgCashFlow));
        }
        
        return result;
    }
    
    public static class SavingsRateData {
        private String month;
        private int rate;
        
        public SavingsRateData(String month, int rate) {
            this.month = month;
            this.rate = rate;
        }
        
        public String getMonth() { return month; }
        public int getRate() { return rate; }
    }
    
    public static List<SavingsRateData> calculateSavingsRateOverTime(int userId, int months) throws Exception {
        TransactionDAO transactionDAO = new TransactionDAO();
        List<SavingsRateData> result = new ArrayList<>();
        
        for (int i = months - 1; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.now().minusMonths(i);
            LocalDate monthStart = yearMonth.atDay(1);
            LocalDate monthEnd = yearMonth.atEndOfMonth();
            
            List<Transaction> transactions = transactionDAO.findByUserIdAndDateRange(userId, monthStart, monthEnd);
            
            double income = transactions.stream()
                .filter(t -> t.getTransactionType() == Transaction.TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
            
            double expenses = transactions.stream()
                .filter(t -> t.getTransactionType() == Transaction.TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
            
            double savingsRate = income > 0 ? ((income - expenses) / income) * 100 : 0;
            
            result.add(new SavingsRateData(yearMonth.format(MONTH_FORMATTER), (int) Math.round(savingsRate)));
        }
        
        return result;
    }
    
    public static class BuyDecisionResult {
        private double hoursOfWork;
        private double daysToDelayGoal;
        private double percentOfIncome;
        private String recommendation;
        private String reasoning;
        
        public BuyDecisionResult(double hoursOfWork, double daysToDelayGoal, double percentOfIncome, 
                                String recommendation, String reasoning) {
            this.hoursOfWork = hoursOfWork;
            this.daysToDelayGoal = daysToDelayGoal;
            this.percentOfIncome = percentOfIncome;
            this.recommendation = recommendation;
            this.reasoning = reasoning;
        }
        
        public double getHoursOfWork() { return hoursOfWork; }
        public double getDaysToDelayGoal() { return daysToDelayGoal; }
        public double getPercentOfIncome() { return percentOfIncome; }
        public String getRecommendation() { return recommendation; }
        public String getReasoning() { return reasoning; }
    }
    
    public static BuyDecisionResult calculateBuyDecision(int userId, double itemPrice) throws Exception {
        UserDAO userDAO = new UserDAO();
        TransactionDAO transactionDAO = new TransactionDAO();
        
        User user = userDAO.findById(userId);
        double hourlyWage = user != null ? user.getHourlyWage() : 0;
        double monthlyIncome = user != null ? user.getMonthlyIncome() : 0;
        
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        
        List<Transaction> transactions = transactionDAO.findByUserIdAndDateRange(userId, monthStart, monthEnd);
        
        double monthlySavings = transactions.stream()
            .mapToDouble(t -> {
                double amount = t.getAmount();
                return t.getTransactionType() == Transaction.TransactionType.INCOME ? amount : -amount;
            })
            .sum();
        
        double hoursOfWork = hourlyWage > 0 ? itemPrice / hourlyWage : 0;
        double daysToDelayGoal = monthlySavings > 0 ? itemPrice / (monthlySavings / 30) : 0;
        double percentOfIncome = monthlyIncome > 0 ? (itemPrice / monthlyIncome) * 100 : 0;
        
        String recommendation;
        String reasoning;
        
        if (percentOfIncome > 20) {
            recommendation = "not-recommended";
            reasoning = "This purchase represents more than 20% of your monthly income. Consider saving up for it instead.";
        } else if (percentOfIncome > 10 || hoursOfWork > 40) {
            recommendation = "think-twice";
            reasoning = "This is a significant purchase. Make sure it aligns with your financial goals.";
        } else {
            recommendation = "affordable";
            reasoning = "This purchase fits comfortably within your budget.";
        }
        
        return new BuyDecisionResult(
            Math.round(hoursOfWork * 10) / 10.0,
            Math.round(daysToDelayGoal * 10) / 10.0,
            Math.round(percentOfIncome * 10) / 10.0,
            recommendation,
            reasoning
        );
    }
}


