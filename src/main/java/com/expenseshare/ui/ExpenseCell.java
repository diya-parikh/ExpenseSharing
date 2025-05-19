package com.expenseshare.ui;

import com.expenseshare.model.Expense;
import com.expenseshare.dao.UserDAO;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import java.time.format.DateTimeFormatter;

public class ExpenseCell extends ListCell<Expense> {
    private final Long currentUserId;
    private final VBox content;
    private final Label descriptionLabel;
    private final Label amountLabel;
    private final Label dateLabel;
    private final Label splitLabel;
    private final UserDAO userDAO;
    private final DateTimeFormatter dateFormatter;
    
    public ExpenseCell(Long currentUserId) {
        this.currentUserId = currentUserId;
        this.userDAO = new UserDAO();
        this.dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        
        content = new VBox(5);
        content.setPadding(new Insets(10));
        
        // Disable selection highlighting
        setStyle("-fx-selection-bar: transparent; -fx-selection-bar-non-focused: transparent;");
        
        descriptionLabel = new Label();
        descriptionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        amountLabel = new Label();
        amountLabel.setStyle("-fx-text-fill: #2196F3;");
        
        dateLabel = new Label();
        dateLabel.setStyle("-fx-text-fill: #757575;");
        
        splitLabel = new Label();
        splitLabel.setWrapText(true);
        
        content.getChildren().addAll(descriptionLabel, amountLabel, dateLabel, splitLabel);
        content.setStyle("-fx-background-color: #E1F5FE; -fx-background-radius: 5;");
    }
    
    @Override
    protected void updateItem(Expense expense, boolean empty) {
        super.updateItem(expense, empty);
        
        if (empty || expense == null) {
            setGraphic(null);
        } else {
            descriptionLabel.setText(expense.getDescription());
            amountLabel.setText(String.format("INR %.2f", expense.getAmount().doubleValue()));
            dateLabel.setText(expense.getCreatedAt().format(dateFormatter));
            
            StringBuilder splitText = new StringBuilder();
            String payerName = userDAO.findById(expense.getPayerId()).getUsername();
            splitText.append("Paid by: ").append(payerName);
            
            // Add split details
            expense.getSplits().forEach(split -> {
                String userName = userDAO.findById(split.getUserId()).getUsername();
                splitText.append("\nSplit with: ")
                        .append(userName)
                        .append(" (")
                        .append(String.format("INR %.2f", split.getAmount().doubleValue()))
                        .append(")");
            });
            
            splitLabel.setText(splitText.toString());
            setGraphic(content);
        }
    }
} 