package com.argonomix.utils;

import com.argonomix.database.*;
import com.argonomix.models.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class NotificationService {
    
    public static void checkAndAddNotifications(int userId) throws Exception {
        UserSettingsDAO settingsDAO = new UserSettingsDAO();
        UserSettings settings = settingsDAO.getOrCreate(userId);
        
        if (!settings.isNotificationsEnabled()) {
            return;
        }
        
        NotificationDAO notificationDAO = new NotificationDAO();
        TransactionDAO transactionDAO = new TransactionDAO();
        BudgetDAO budgetDAO = new BudgetDAO();
        GoalDAO goalDAO = new GoalDAO();
        SubscriptionDAO subscriptionDAO = new SubscriptionDAO();
        
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        
        // 1. Budget warnings
        List<Budget> budgets = budgetDAO.findByUserId(userId);
        List<Transaction> transactions = transactionDAO.findByUserIdAndDateRange(userId, monthStart, monthEnd);
        
        for (Budget budget : budgets) {
            double spent = transactions.stream()
                .filter(t -> t.getCategory() != null && 
                           t.getCategory().equals(budget.getCategory()) &&
                           t.getTransactionType() == Transaction.TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
            
            double percentage = budget.getAmountLimit() > 0 ? (spent / budget.getAmountLimit()) * 100 : 0;
            
            if (percentage > 100) {
                Notification notif = new Notification(
                    userId,
                    "Budget Exceeded!",
                    String.format("You've exceeded your %s budget by $%.2f.", budget.getCategory(), spent - budget.getAmountLimit()),
                    Notification.NotificationSeverity.ERROR,
                    "budget"
                );
                notificationDAO.save(notif);
            } else if (percentage > 80) {
                Notification notif = new Notification(
                    userId,
                    "Budget Warning",
                    String.format("You're close to exceeding your %s budget. You've spent %.1f%%.", budget.getCategory(), percentage),
                    Notification.NotificationSeverity.WARNING,
                    "budget"
                );
                notificationDAO.save(notif);
            }
        }
        
        // 2. Upcoming subscription renewals
        List<Subscription> subscriptions = subscriptionDAO.findByUserId(userId);
        for (Subscription sub : subscriptions) {
            if (sub.getNextBillingDate() != null) {
                long daysUntil = ChronoUnit.DAYS.between(now, sub.getNextBillingDate());
                if (daysUntil > 0 && daysUntil <= 3) {
                    Notification notif = new Notification(
                        userId,
                        "Upcoming Bill",
                        String.format("%s bill of $%.2f is due in %d days.", sub.getServiceName(), sub.getMonthlyCost(), daysUntil),
                        Notification.NotificationSeverity.INFO,
                        "bill"
                    );
                    notificationDAO.save(notif);
                }
            }
        }
        
        // 3. Goal milestones
        List<Goal> goals = goalDAO.findActiveByUserId(userId);
        for (Goal goal : goals) {
            if (goal.getCurrentAmount() >= goal.getTargetAmount() && !goal.isCompleted()) {
                goal.setCompleted(true);
                goalDAO.update(goal);
                
                Notification notif = new Notification(
                    userId,
                    "Goal Achieved!",
                    String.format("Congratulations! You've reached your goal: %s!", goal.getGoalName()),
                    Notification.NotificationSeverity.SUCCESS,
                    "goal"
                );
                notificationDAO.save(notif);
            }
        }
    }
}


