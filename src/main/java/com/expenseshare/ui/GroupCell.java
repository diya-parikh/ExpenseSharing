package com.expenseshare.ui;

import com.expenseshare.dao.GroupDAO;
import com.expenseshare.model.Group;
import com.expenseshare.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;

public class GroupCell extends ListCell<Group> {
    private final VBox content;
    private final Label nameLabel;
    private final Label descriptionLabel;
    private final Label memberCountLabel;
    private final Button manageMembersButton;
    private final Button deleteGroupButton;
    private final Long currentUserId;
    private final GroupDAO groupDAO;

    public GroupCell(Long currentUserId) {
        this.currentUserId = currentUserId;
        this.groupDAO = new GroupDAO();
        content = new VBox(5);
        content.setPadding(new Insets(10));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 5;");

        nameLabel = new Label();
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setStyle("-fx-text-fill: #2196F3;");

        descriptionLabel = new Label();
        descriptionLabel.setFont(Font.font("System", 12));
        descriptionLabel.setStyle("-fx-text-fill: #666666;");
        descriptionLabel.setWrapText(true);

        memberCountLabel = new Label();
        memberCountLabel.setFont(Font.font("System", 12));
        memberCountLabel.setStyle("-fx-text-fill: #666666;");

        manageMembersButton = new Button("Manage Members");
        manageMembersButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px;");
        manageMembersButton.setVisible(false);

        deleteGroupButton = new Button("Delete Group");
        deleteGroupButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12px;");
        deleteGroupButton.setVisible(false);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(nameLabel, memberCountLabel);

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.getChildren().addAll(manageMembersButton, deleteGroupButton);

        content.getChildren().addAll(header, descriptionLabel, footer);
    }

    @Override
    protected void updateItem(Group group, boolean empty) {
        super.updateItem(group, empty);
        if (empty) {
            setGraphic(null);
            setText(null);
        } else {
            nameLabel.setText(group.getName());
            descriptionLabel.setText(group.getDescription());
            
            // Get the actual member count
            List<User> members = groupDAO.getGroupMembers(group.getId());
            int memberCount = members.size();
            memberCountLabel.setText(memberCount + (memberCount == 1 ? " member" : " members"));
            
            // Show manage members and delete buttons only for admin (creator)
            boolean isAdmin = group.getCreatedBy().equals(currentUserId);
            manageMembersButton.setVisible(isAdmin);
            deleteGroupButton.setVisible(isAdmin);
            
            manageMembersButton.setOnAction(e -> {
                ManageGroupMembersDialog dialog = new ManageGroupMembersDialog(group, currentUserId);
                dialog.showAndWait();
                // Refresh the member count after dialog closes
                List<User> updatedMembers = groupDAO.getGroupMembers(group.getId());
                int updatedCount = updatedMembers.size();
                memberCountLabel.setText(updatedCount + (updatedCount == 1 ? " member" : " members"));
            });

            deleteGroupButton.setOnAction(e -> {
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Delete Group");
                confirmDialog.setHeaderText("Delete Group Confirmation");
                confirmDialog.setContentText("Are you sure you want to delete the group '" + group.getName() + "'? This action cannot be undone.");
                
                confirmDialog.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        if (groupDAO.delete(group.getId())) {
                            // Refresh the groups list
                            getListView().getItems().remove(group);
                        } else {
                            Alert errorDialog = new Alert(Alert.AlertType.ERROR);
                            errorDialog.setTitle("Error");
                            errorDialog.setHeaderText("Delete Failed");
                            errorDialog.setContentText("Failed to delete the group. Please try again.");
                            errorDialog.showAndWait();
                        }
                    }
                });
            });
            
            setGraphic(content);
            setText(null);
        }
    }
} 