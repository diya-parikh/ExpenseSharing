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

public class RegisterController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Label messageLabel;
    
    private final UserDAO userDAO = new UserDAO();
    
    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validate input
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            messageLabel.setText("Please fill in all fields");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match");
            return;
        }
        
        if (password.length() < 6) {
            messageLabel.setText("Password must be at least 6 characters long");
            return;
        }
        
        // Check if email already exists
        Optional<User> existingUser = userDAO.findByEmail(email);
        if (existingUser.isPresent()) {
            messageLabel.setText("Email already registered");
            return;
        }
        
        // Create new user
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        User newUser = new User(username, email, passwordHash);
        
        if (userDAO.create(newUser)) {
            try {
                // Load login scene
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Parent root = loader.load();
                
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Login - Expense Share");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                messageLabel.setText("Error loading login form");
            }
        } else {
            messageLabel.setText("Error creating account");
        }
    }
    
    @FXML
    private void handleLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login - Expense Share");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Error loading login form");
        }
    }
} 