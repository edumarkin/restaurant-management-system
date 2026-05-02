package com.restaurant.ui;

import com.restaurant.dao.EmployeeDAO;
import com.restaurant.model.Employee;
import com.restaurant.util.Dialogs;
import com.restaurant.util.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class LoginView {

    public static Scene build(Stage stage) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2c1810, #5c3317);");

        VBox card = new VBox(14);
        card.setStyle(
                "-fx-background-color: #fdf8f0;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 40;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 30, 0.2, 0, 8);"
        );
        card.setMaxWidth(400);
        card.setAlignment(Pos.TOP_LEFT);

        Label logo = new Label("\uD83C\uDF7D");
        logo.setStyle("-fx-font-size: 42px;");
        Label title = new Label("Welcome back");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: 700; -fx-text-fill: #2c1810;");
        Label subtitle = new Label("Sign in to the restaurant management system");
        subtitle.setStyle("-fx-text-fill: #8b6e5a; -fx-font-size: 13px;");
        subtitle.setWrapText(true);

        VBox header = new VBox(6, logo, title, subtitle);
        header.setPadding(new Insets(0, 0, 10, 0));

        Label userLbl = new Label("Username");
        userLbl.setStyle("-fx-text-fill: #8b6e5a;");
        TextField user = new TextField();
        user.setPromptText("e.g. manager");

        Label passLbl = new Label("Password");
        passLbl.setStyle("-fx-text-fill: #8b6e5a;");
        PasswordField pass = new PasswordField();
        pass.setPromptText("••••");

        Button loginBtn = new Button("Sign In");
        loginBtn.setStyle(
                "-fx-background-color: #8b4513; -fx-text-fill: white;" +
                        "-fx-font-weight: 700; -fx-background-radius: 6; -fx-padding: 10 16;"
        );
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(40);

        Label hint = new Label("Demo accounts: manager / receptionist / waiter / chef / cashier  —  password: 1234");
        hint.setStyle("-fx-text-fill: #8b6e5a; -fx-font-size: 11px; -fx-padding: 8 0 0 0;");
        hint.setWrapText(true);

        card.getChildren().addAll(header, userLbl, user, passLbl, pass, loginBtn, hint);

        EmployeeDAO dao = new EmployeeDAO();
        Runnable doLogin = () -> {
            String u = user.getText().trim();
            String p = pass.getText();
            if (u.isEmpty() || p.isEmpty()) {
                Dialogs.error("Login", "Please enter both username and password.");
                return;
            }
            Employee emp = dao.login(u, p);
            if (emp == null) {
                Dialogs.error("Login", "Invalid username or password.");
                return;
            }
            Session.setCurrent(emp);
            Scene s = MainView.build(stage, emp);
            s.getStylesheets().add(LoginView.class.getResource("/css/theme.css").toExternalForm());
            stage.setScene(s);
        };
        loginBtn.setOnAction(e -> doLogin.run());
        pass.setOnAction(e -> doLogin.run());
        user.setOnAction(e -> pass.requestFocus());

        root.getChildren().add(card);
        return new Scene(root);
    }
}