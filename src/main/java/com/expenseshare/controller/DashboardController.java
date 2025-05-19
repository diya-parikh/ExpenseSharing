package com.expenseshare.controller;

import com.expenseshare.dao.ExpenseDAO;
import com.expenseshare.dao.GroupDAO;
import com.expenseshare.model.Expense;
import com.expenseshare.model.Group;
import com.expenseshare.ui.AddExpenseDialog;
import com.expenseshare.ui.CreateGroupDialog;
import com.expenseshare.ui.ExpenseCell;
import com.expenseshare.ui.GroupCell;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class DashboardController {
    
    @FXML
    private Button logoutButton;
    @FXML
    private Button createGroupButton;
    @FXML
    private ListView<Group> groupsListView;
    @FXML
    private Label welcomeLabel;

    private final GroupDAO groupDAO = new GroupDAO();
    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private Long currentUserId;

    @FXML
    public void initialize() {
        // Initialize UI components
        setupEventHandlers();
    }

    public void setCurrentUser(Long userId, String username) {
        if (userId == null) {
            showError("Error", "User ID cannot be null");
            return;
        }
        this.currentUserId = userId;
        welcomeLabel.setText("Welcome, " + username + "!");
        loadUserGroups();
    }

    private void setupEventHandlers() {
        // Create Group button handler
        createGroupButton.setOnAction(event -> {
            CreateGroupDialog dialog = new CreateGroupDialog();
            dialog.showAndWait().ifPresent(group -> {
                try {
                    group.setCreatedBy(currentUserId);
                    if (groupDAO.create(group)) {
                        loadUserGroups();
                    } else {
                        showError("Error", "Failed to create group. Please try again.");
                    }
                } catch (Exception e) {
                    showError("Error", "Failed to create group: " + e.getMessage());
                }
            });
        });

        // Logout button handler
        logoutButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Group selection handler
        groupsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                navigateToGroupExpenses(newSelection);
            }
        });
    }

    private void loadUserGroups() {
        if (currentUserId != null) {
            // Set up the groups ListView with custom cell factory
            groupsListView.setCellFactory(lv -> new GroupCell(currentUserId));
            
            List<Group> groups = groupDAO.findByUserId(currentUserId);
            groupsListView.getItems().setAll(groups);
        }
    }

    private void navigateToGroupExpenses(Group group) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/group_expenses.fxml"));
            Parent root = loader.load();
            GroupExpensesController controller = loader.getController();
            controller.setGroup(group);
            controller.setCurrentUser(currentUserId);

            Stage stage = (Stage) groupsListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 