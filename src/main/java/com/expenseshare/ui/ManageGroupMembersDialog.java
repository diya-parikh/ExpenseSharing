package com.expenseshare.ui;

import com.expenseshare.dao.GroupDAO;
import com.expenseshare.dao.UserDAO;
import com.expenseshare.model.Group;
import com.expenseshare.model.User;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.control.TextField;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ManageGroupMembersDialog extends Dialog<Void> {
    private final Group group;
    private final GroupDAO groupDAO;
    private final UserDAO userDAO;
    private final ListView<User> membersListView;
    private final TextField searchField;
    private final ListView<User> searchResultsListView;
    private final Label messageLabel;
    private final Long currentUserId;

    public ManageGroupMembersDialog(Group group, Long currentUserId) {
        this.group = group;
        this.currentUserId = currentUserId;
        this.groupDAO = new GroupDAO();
        this.userDAO = new UserDAO();
        
        setTitle("Manage Group Members - " + group.getName());
        setHeaderText("Add or remove members from the group");
        initStyle(StageStyle.UTILITY);

        // Create the main content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        // Search section
        VBox searchSection = new VBox(5);
        searchSection.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5;");
        
        Label searchLabel = new Label("Search Users");
        searchLabel.setStyle("-fx-font-weight: bold;");
        
        searchField = new TextField();
        searchField.setPromptText("Enter username to search");
        searchField.setStyle("-fx-padding: 5;");
        
        searchResultsListView = new ListView<>();
        searchResultsListView.setPrefHeight(150);
        searchResultsListView.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(user.getUsername() + " (" + user.getEmail() + ")");
                    Button addButton = new Button("Add");
                    addButton.setOnAction(e -> handleAddMember(user));
                    addButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                    setGraphic(addButton);
                }
            }
        });
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                List<User> allUsers = userDAO.findAll();
                List<User> currentMembers = groupDAO.getGroupMembers(group.getId());
                List<User> searchResults = allUsers.stream()
                    .filter(user -> !currentMembers.stream().anyMatch(member -> member.getId().equals(user.getId())))
                    .filter(user -> user.getUsername().toLowerCase().contains(newValue.toLowerCase()))
                    .collect(Collectors.toList());
                searchResultsListView.getItems().setAll(searchResults);
            } else {
                searchResultsListView.getItems().clear();
            }
        });
        
        searchSection.getChildren().addAll(searchLabel, searchField, searchResultsListView);

        // Members list
        Label membersLabel = new Label("Current Members");
        membersLabel.setStyle("-fx-font-weight: bold;");
        
        membersListView = new ListView<>();
        membersListView.setPrefHeight(300);
        membersListView.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(user.getUsername() + " (" + user.getEmail() + ")");
                    // Don't show remove button for admin
                    if (!user.getId().equals(currentUserId)) {
                        Button removeButton = new Button("Remove");
                        removeButton.setOnAction(e -> handleRemoveMember(user));
                        removeButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                        setGraphic(removeButton);
                    }
                }
            }
        });
        
        loadGroupMembers();

        // Message label
        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        content.getChildren().addAll(searchSection, membersLabel, membersListView, messageLabel);
        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    }

    private void loadGroupMembers() {
        List<User> members = groupDAO.getGroupMembers(group.getId());
        membersListView.getItems().setAll(members);
    }

    private void handleAddMember(User user) {
        if (groupDAO.addUserToGroup(group.getId(), user.getId(), "MEMBER")) {
            loadGroupMembers();
            searchField.clear();
            searchResultsListView.getItems().clear();
            messageLabel.setText("Member added successfully");
        } else {
            messageLabel.setText("Failed to add member");
        }
    }

    private void handleRemoveMember(User user) {
        if (groupDAO.removeUserFromGroup(group.getId(), user.getId())) {
            loadGroupMembers();
            messageLabel.setText("Member removed successfully");
        } else {
            messageLabel.setText("Failed to remove member");
        }
    }
} 