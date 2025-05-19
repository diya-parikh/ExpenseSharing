package com.expenseshare.controller;

import com.expenseshare.dao.ExpenseDAO;
import com.expenseshare.dao.GroupDAO;
import com.expenseshare.model.Debt;
import com.expenseshare.model.Expense;
import com.expenseshare.model.Group;
import com.expenseshare.model.User;
import com.expenseshare.ui.AddExpenseDialog;
import com.expenseshare.ui.ExpenseCell;
import com.expenseshare.ui.SettlementDialog;
import com.expenseshare.util.DebtCalculator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupExpensesController {
    @FXML private Label groupNameLabel;
    @FXML private Button backButton;
    @FXML private Button addExpenseButton;
    @FXML private Button settleUpButton;
    @FXML private Label balanceLabel;
    @FXML private ListView<Expense> expensesListView;
    private Group group;
    private Long currentUserId;
    private final ExpenseDAO expenseDAO;
    private final GroupDAO groupDAO;

    public GroupExpensesController() {
        this.expenseDAO = new ExpenseDAO();
        this.groupDAO = new GroupDAO();
    }

    @FXML
    public void initialize() {
        setupEventHandlers();
    }

    public void setGroup(Group group) {
        this.group = group;
        groupNameLabel.setText(group.getName());
        loadGroupExpenses();
        updateBalanceLabel();
    }

    public void setCurrentUser(Long userId) {
        this.currentUserId = userId;
        if (group != null) {
            loadGroupExpenses();
            updateBalanceLabel();
        }
    }

    private void setupEventHandlers() {
        backButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                Parent root = loader.load();
                DashboardController controller = loader.getController();
                controller.setCurrentUser(currentUserId, "User"); // TODO: Get actual username
                Stage stage = (Stage) backButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        addExpenseButton.setOnAction(event -> {
            AddExpenseDialog dialog = new AddExpenseDialog(group, currentUserId);
            dialog.showAndWait().ifPresent(expense -> {
                loadGroupExpenses();
                updateBalanceLabel();
            });
        });

        settleUpButton.setOnAction(event -> {
            handleSettleUp();
        });
    }

    private void loadGroupExpenses() {
        try {
            List<Expense> expenses = expenseDAO.findByGroupId(group.getId());
            
            // Sort expenses by creation time in descending order (newest first)
            expenses.sort((e1, e2) -> e2.getCreatedAt().compareTo(e1.getCreatedAt()));
            
            expensesListView.getItems().setAll(expenses);
            expensesListView.setCellFactory(lv -> new ExpenseCell(currentUserId));
            expensesListView.setSelectionModel(null); // Disable selection
            updateBalanceLabel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateBalanceLabel() {
        try {
            if (group == null || currentUserId == null) {
                return;
            }

            List<User> members = groupDAO.getGroupMembers(group.getId());
            List<Expense> expenses = expenseDAO.findByGroupId(group.getId());
            
            // Debug print
            System.out.println("Calculating balances for group: " + group.getId());
            System.out.println("Current user: " + currentUserId);
            System.out.println("Number of expenses: " + expenses.size());
            System.out.println("Number of members: " + members.size());
            
            Map<Long, DebtCalculator.UserBalance> balances = DebtCalculator.calculateBalances(expenses, members);
            DebtCalculator.UserBalance userBalance = balances.get(currentUserId);
            
            if (userBalance == null) {
                System.out.println("User balance is null for user: " + currentUserId);
                balanceLabel.setText("You owe: INR 0.00 | Others owe you: INR 0.00");
                return;
            }
            
            BigDecimal myBalance = userBalance.getBalance();
            System.out.println("User balance: " + myBalance);
            
            BigDecimal iOwe = BigDecimal.ZERO;
            BigDecimal othersOwe = BigDecimal.ZERO;
            
            if (myBalance.compareTo(BigDecimal.ZERO) < 0) {
                iOwe = myBalance.abs();
            } else {
                othersOwe = myBalance;
            }
            
            balanceLabel.setText(String.format("You owe: INR %.2f | Others owe you: INR %.2f", 
                iOwe.doubleValue(), othersOwe.doubleValue()));
        } catch (Exception e) {
            e.printStackTrace();
            balanceLabel.setText("Error calculating balance");
        }
    }

    @FXML
    private void handleSettleUp() {
        try {
            List<User> members = groupDAO.getGroupMembers(group.getId());
            List<Expense> expenses = expenseDAO.findByGroupId(group.getId());
            
            Map<Long, DebtCalculator.UserBalance> balances = DebtCalculator.calculateBalances(expenses, members);
            List<Debt> debts = DebtCalculator.simplifyDebts(balances);
            
            List<Debt> myDebts = debts.stream()
                .filter(debt -> debt.getFromUserId().equals(currentUserId))
                .collect(Collectors.toList());
                
            if (myDebts.isEmpty()) {
                showSettlementDialog("You don't owe anything to others.");
            } else {
                StringBuilder message = new StringBuilder("You owe:\n");
                for (Debt debt : myDebts) {
                    message.append(String.format("INR %.2f to %s\n", 
                        debt.getAmount().doubleValue(), debt.getToUsername()));
                }
                showSettlementDialog(message.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSettlementDialog(String message) {
        SettlementDialog dialog = new SettlementDialog(message, currentUserId);
        dialog.showAndWait().ifPresent(result -> {
            // Handle settlement confirmation
            // TODO: Implement settlement recording
            updateBalanceLabel();
        });
    }
} 