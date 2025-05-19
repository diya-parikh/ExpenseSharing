package com.expenseshare.ui;

import com.expenseshare.dao.CategoryDAO;
import com.expenseshare.dao.ExpenseDAO;
import com.expenseshare.dao.GroupDAO;
import com.expenseshare.model.Category;
import com.expenseshare.model.Expense;
import com.expenseshare.model.ExpenseSplit;
import com.expenseshare.model.Group;
import com.expenseshare.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AddExpenseDialog extends Dialog<Expense> {
    private final TextField descriptionField;
    private final TextField amountField;
    private final ComboBox<String> currencyComboBox;
    private final ComboBox<Category> categoryComboBox;
    private final DatePicker datePicker;
    private final ComboBox<User> payerComboBox;
    private final ComboBox<String> splitMethodComboBox;
    private final VBox selectedMembersBox;
    private final Label totalAmountLabel;
    private final Label messageLabel;
    private final Group group;
    private final Long currentUserId;
    private final List<CheckBox> memberCheckBoxes;
    private final CategoryDAO categoryDAO;
    private final GroupDAO groupDAO;
    private final ExpenseDAO expenseDAO;
    private final Map<User, TextField> exactAmountFields;
    private final Map<User, TextField> percentageFields;
    private final VBox splitDetailsBox;

    public AddExpenseDialog(Group group, Long currentUserId) {
        this.group = group;
        this.currentUserId = currentUserId;
        this.memberCheckBoxes = new ArrayList<>();
        this.categoryDAO = new CategoryDAO();
        this.groupDAO = new GroupDAO();
        this.expenseDAO = new ExpenseDAO();
        this.exactAmountFields = new HashMap<>();
        this.percentageFields = new HashMap<>();

        // Initialize UI components
        descriptionField = new TextField();
        amountField = new TextField();
        currencyComboBox = new ComboBox<>(FXCollections.observableArrayList("USD", "EUR", "GBP", "INR"));
        categoryComboBox = new ComboBox<>();
        datePicker = new DatePicker(LocalDate.now());
        payerComboBox = new ComboBox<>();
        splitMethodComboBox = new ComboBox<>(FXCollections.observableArrayList("EQUAL", "EXACT", "PERCENTAGE"));
        selectedMembersBox = new VBox(5);
        totalAmountLabel = new Label("Total Amount: 0.00");
        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");
        splitDetailsBox = new VBox(5);

        // Set up dialog
        setTitle("Add New Expense");
        setHeaderText("Add expense for " + group.getName());

        // Create dialog pane
        DialogPane dialogPane = getDialogPane();
        dialogPane.setPrefWidth(600);
        dialogPane.setPrefHeight(800);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Create content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Create form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        // Add form fields with labels
        grid.add(new Label("Description:"), 0, 0);
        grid.add(descriptionField, 1, 0);
        grid.add(new Label("Amount:"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Currency:"), 0, 2);
        grid.add(currencyComboBox, 1, 2);
        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryComboBox, 1, 3);
        grid.add(new Label("Date:"), 0, 4);
        grid.add(datePicker, 1, 4);
        grid.add(new Label("Paid by:"), 0, 5);
        grid.add(payerComboBox, 1, 5);
        grid.add(new Label("Select members to split with:"), 0, 6);
        grid.add(selectedMembersBox, 1, 6);
        grid.add(new Label("Split Method:"), 0, 7);
        grid.add(splitMethodComboBox, 1, 7);

        // Add split details section
        splitDetailsBox.setPadding(new Insets(10));
        grid.add(splitDetailsBox, 0, 8, 2, 1);

        // Add total amount label
        totalAmountLabel.setStyle("-fx-font-weight: bold;");
        totalAmountLabel.setPadding(new Insets(10));

        // Add all components to content
        content.getChildren().addAll(grid, totalAmountLabel, messageLabel);

        // Set dialog content
        dialogPane.setContent(content);

        // Load data
        loadCategories();
        loadGroupMembers();
        setupEventHandlers();

        // Set result converter
        setResultConverter(new Callback<ButtonType, Expense>() {
            @Override
            public Expense call(ButtonType buttonType) {
                if (buttonType == ButtonType.OK) {
                    return createExpense();
                }
                return null;
            }
        });
    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryDAO.findAll();
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
            categoryComboBox.setCellFactory(lv -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName());
                }
            });
            categoryComboBox.setButtonCell(new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadGroupMembers() {
        try {
            List<User> members = groupDAO.getGroupMembers(group.getId());
            payerComboBox.setItems(FXCollections.observableArrayList(members));
            payerComboBox.setCellFactory(lv -> new ListCell<User>() {
                @Override
                protected void updateItem(User item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getUsername());
                }
            });
            payerComboBox.setButtonCell(new ListCell<User>() {
                @Override
                protected void updateItem(User item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getUsername());
                }
            });
            
            // Clear existing checkboxes
            memberCheckBoxes.clear();
            selectedMembersBox.getChildren().clear();
            
            // Create checkboxes for each member
            for (User member : members) {
                CheckBox checkBox = new CheckBox(member.getUsername());
                checkBox.setSelected(true);
                memberCheckBoxes.add(checkBox);
                selectedMembersBox.getChildren().add(checkBox);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupEventHandlers() {
        // Add amount validation
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.isEmpty()) {
                    BigDecimal amount = new BigDecimal(newValue);
                    totalAmountLabel.setText(String.format("Total Amount: %.2f", amount));
                    updateSplitDetails();
                } else {
                    totalAmountLabel.setText("Total Amount: 0.00");
                    clearSplitDetails();
                }
            } catch (NumberFormatException e) {
                messageLabel.setText("Please enter a valid amount");
            }
        });

        // Split method change handler
        splitMethodComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateSplitDetails();
            }
        });

        // Member selection change handler
        memberCheckBoxes.forEach(checkBox -> 
            checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                updateSplitDetails();
            })
        );
    }

    private void updateSplitDetails() {
        splitDetailsBox.getChildren().clear();
        exactAmountFields.clear();
        percentageFields.clear();

        try {
            if (amountField.getText().trim().isEmpty()) {
                return;
            }

            BigDecimal totalAmount = new BigDecimal(amountField.getText().trim());
            List<User> selectedMembers = getSelectedMembers();
            
            if (selectedMembers.isEmpty()) {
                return;
            }

            String splitMethod = splitMethodComboBox.getValue();
            if (splitMethod == null) {
                return;
            }

            switch (splitMethod) {
                case "EQUAL":
                    BigDecimal splitAmount = totalAmount.divide(BigDecimal.valueOf(selectedMembers.size()), 2, BigDecimal.ROUND_HALF_UP);
                    for (User member : selectedMembers) {
                        if (!member.getId().equals(payerComboBox.getValue().getId())) {
                            Label label = new Label(String.format("%s: %.2f", member.getUsername(), splitAmount));
                            splitDetailsBox.getChildren().add(label);
                        }
                    }
                    break;

                case "EXACT":
                    for (User member : selectedMembers) {
                        if (!member.getId().equals(payerComboBox.getValue().getId())) {
                            HBox row = new HBox(10);
                            row.getChildren().add(new Label(member.getUsername() + ":"));
                            TextField amountField = new TextField();
                            amountField.setPromptText("Enter amount");
                            exactAmountFields.put(member, amountField);
                            row.getChildren().add(amountField);
                            splitDetailsBox.getChildren().add(row);
                        }
                    }
                    break;

                case "PERCENTAGE":
                    for (User member : selectedMembers) {
                        if (!member.getId().equals(payerComboBox.getValue().getId())) {
                            HBox row = new HBox(10);
                            row.getChildren().add(new Label(member.getUsername() + ":"));
                            TextField percentageField = new TextField();
                            percentageField.setPromptText("Enter percentage");
                            percentageFields.put(member, percentageField);
                            row.getChildren().add(percentageField);
                            splitDetailsBox.getChildren().add(row);
                        }
                    }
                    break;
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Please enter a valid amount");
        }
    }

    private void clearSplitDetails() {
        splitDetailsBox.getChildren().clear();
        exactAmountFields.clear();
        percentageFields.clear();
    }

    private List<User> getSelectedMembers() {
        return memberCheckBoxes.stream()
                .filter(CheckBox::isSelected)
                .map(checkBox -> {
                    String username = checkBox.getText();
                    return payerComboBox.getItems().stream()
                            .filter(user -> user.getUsername().equals(username))
                            .findFirst()
                            .orElse(null);
                })
                .filter(user -> user != null)
                .collect(Collectors.toList());
    }

    private Expense createExpense() {
        // Validate inputs
        if (descriptionField.getText().trim().isEmpty()) {
            messageLabel.setText("Please enter a description");
            return null;
        }
        if (amountField.getText().trim().isEmpty()) {
            messageLabel.setText("Please enter an amount");
            return null;
        }
        if (categoryComboBox.getValue() == null) {
            messageLabel.setText("Please select a category");
            return null;
        }
        if (payerComboBox.getValue() == null) {
            messageLabel.setText("Please select who paid");
            return null;
        }

        // Get selected members
        List<User> selectedMembers = getSelectedMembers();
        if (selectedMembers.isEmpty()) {
            messageLabel.setText("Please select at least one member to split with");
            return null;
        }

        try {
            BigDecimal totalAmount = new BigDecimal(amountField.getText().trim());
            BigDecimal totalSplit = BigDecimal.ZERO;

            // Validate splits based on method
            String splitMethod = splitMethodComboBox.getValue();
            if (splitMethod == null) {
                messageLabel.setText("Please select a split method");
                return null;
            }

            switch (splitMethod) {
                case "EXACT":
                    for (TextField field : exactAmountFields.values()) {
                        if (!field.getText().trim().isEmpty()) {
                            totalSplit = totalSplit.add(new BigDecimal(field.getText().trim()));
                        }
                    }
                    if (!totalSplit.equals(totalAmount)) {
                        messageLabel.setText("Total split amount must equal the total expense amount");
                        return null;
                    }
                    break;

                case "PERCENTAGE":
                    BigDecimal totalPercentage = BigDecimal.ZERO;
                    for (TextField field : percentageFields.values()) {
                        if (!field.getText().trim().isEmpty()) {
                            totalPercentage = totalPercentage.add(new BigDecimal(field.getText().trim()));
                        }
                    }
                    if (!totalPercentage.equals(new BigDecimal("100"))) {
                        messageLabel.setText("Total percentage must equal 100%");
                        return null;
                    }
                    break;
            }

            // Create expense
            Expense expense = new Expense(
                group.getId(),
                payerComboBox.getValue().getId(),
                categoryComboBox.getValue().getId(),
                descriptionField.getText().trim(),
                totalAmount,
                currencyComboBox.getValue(),
                splitMethod,
                datePicker.getValue().atStartOfDay()
            );

            // Save expense
            if (expenseDAO.create(expense)) {
                // Create expense splits
                switch (expense.getSplitMethod()) {
                    case "EQUAL":
                        BigDecimal splitAmount = expense.getAmount()
                                .divide(BigDecimal.valueOf(selectedMembers.size()), 2, BigDecimal.ROUND_HALF_UP);
                        for (User member : selectedMembers) {
                            if (!member.getId().equals(expense.getPayerId())) {
                                ExpenseSplit split = new ExpenseSplit(
                                    expense.getId(),
                                    member.getId(),
                                    splitAmount,
                                    new BigDecimal(100.0 / selectedMembers.size())
                                );
                                expenseDAO.createExpenseSplit(split);
                            }
                        }
                        break;

                    case "EXACT":
                        for (Map.Entry<User, TextField> entry : exactAmountFields.entrySet()) {
                            if (!entry.getValue().getText().trim().isEmpty()) {
                                BigDecimal amount = new BigDecimal(entry.getValue().getText().trim());
                                BigDecimal percentage = amount.multiply(new BigDecimal("100"))
                                        .divide(totalAmount, 2, BigDecimal.ROUND_HALF_UP);
                                ExpenseSplit split = new ExpenseSplit(
                                    expense.getId(),
                                    entry.getKey().getId(),
                                    amount,
                                    percentage
                                );
                                expenseDAO.createExpenseSplit(split);
                            }
                        }
                        break;

                    case "PERCENTAGE":
                        for (Map.Entry<User, TextField> entry : percentageFields.entrySet()) {
                            if (!entry.getValue().getText().trim().isEmpty()) {
                                BigDecimal percentage = new BigDecimal(entry.getValue().getText().trim());
                                BigDecimal amount = totalAmount.multiply(percentage)
                                        .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
                                ExpenseSplit split = new ExpenseSplit(
                                    expense.getId(),
                                    entry.getKey().getId(),
                                    amount,
                                    percentage
                                );
                                expenseDAO.createExpenseSplit(split);
                            }
                        }
                        break;
                }
                return expense;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error creating expense: " + e.getMessage());
            return null;
        }
    }
} 