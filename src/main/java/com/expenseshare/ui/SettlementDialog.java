package com.expenseshare.ui;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;

public class SettlementDialog extends Dialog<Boolean> {
    
    public SettlementDialog(String message, Long currentUserId) {
        setTitle("Confirm Settlement");
        
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        content.getChildren().add(messageLabel);
        
        getDialogPane().setContent(content);
        
        setResultConverter(dialogButton -> dialogButton == okButtonType);
    }
} 