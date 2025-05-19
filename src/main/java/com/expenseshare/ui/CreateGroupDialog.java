package com.expenseshare.ui;

import com.expenseshare.model.Group;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CreateGroupDialog extends Dialog<Group> {
    private final TextField nameField;
    private final TextArea descriptionArea;

    public CreateGroupDialog() {
        setTitle("Create New Group");
        setHeaderText("Enter group details");
        initStyle(StageStyle.UTILITY);

        // Create the custom dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Add fields
        nameField = new TextField();
        nameField.setPromptText("Group Name");
        nameField.setPrefWidth(300);
        nameField.setStyle("-fx-font-size: 14px;");

        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Group Description");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setPrefWidth(300);
        descriptionArea.setStyle("-fx-font-size: 14px;");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);

        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Style the dialog
        getDialogPane().setStyle("-fx-background-color: white;");
        getDialogPane().lookupButton(ButtonType.OK).setStyle(
            "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 16;"
        );
        getDialogPane().lookupButton(ButtonType.CANCEL).setStyle(
            "-fx-background-color: #f5f5f5; -fx-text-fill: #666666; -fx-font-size: 14px; -fx-padding: 8 16;"
        );

        // Convert the result to a Group object when OK is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Group(
                    nameField.getText().trim(),
                    descriptionArea.getText().trim(),
                    null  // createdBy will be set by DashboardController
                );
            }
            return null;
        });

        // Add validation
        getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            getDialogPane().lookupButton(ButtonType.OK).setDisable(newValue.trim().isEmpty());
        });
    }
} 