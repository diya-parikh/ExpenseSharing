package com.expenseshare.controller;

import com.expenseshare.dao.UserDAO;
import com.expenseshare.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label messageLabel;
    
    private final UserDAO userDAO = new UserDAO();
    
    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();
        
        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill in all fields");
            return;
        }
        
        Optional<User> userOpt = userDAO.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (BCrypt.checkpw(password, user.getPasswordHash())) {
                try {
                    // Load dashboard scene
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                    Parent root = loader.load();
                    
                    // Get the dashboard controller and set the current user
                    DashboardController dashboardController = loader.getController();
                    dashboardController.setCurrentUser(user.getId(), user.getUsername());
                    
                    Stage stage = (Stage) emailField.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Dashboard - Expense Share");
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    messageLabel.setText("Error loading dashboard");
                }
            } else {
                messageLabel.setText("Invalid password");
            }
        } else {
            messageLabel.setText("User not found");
        }
    }
    
    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Register - Expense Share");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Error loading registration form");
        }
    }
} 