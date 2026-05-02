package com.restaurant.ui;

import com.restaurant.model.Employee;
import com.restaurant.util.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainView {

    public static Scene build(Stage stage, Employee emp) {
        BorderPane root = new BorderPane();

        root.setTop(buildTopBar(stage, emp));
        root.setCenter(buildContentForRole(emp));

        return new Scene(root);
    }

    private static HBox buildTopBar(Stage stage, Employee emp) {
        HBox bar = new HBox(16);
        bar.getStyleClass().add("topbar");
        bar.setAlignment(Pos.CENTER_LEFT);

        Label brand = new Label("\uD83C\uDF7D  Restaurant MS");
        brand.getStyleClass().add("topbar-title");

        Label rolePill = new Label(emp.getRole().name());
        rolePill.getStyleClass().add("role-pill");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label hello = new Label("Signed in as " + emp.getFullName());
        hello.getStyleClass().add("topbar-user");

        Button logout = new Button("Logout");
        logout.getStyleClass().add("btn-ghost");
        logout.setOnAction(e -> {
            Session.clear();
            Scene s = LoginView.build(stage);
            s.getStylesheets().add(MainView.class.getResource("/css/theme.css").toExternalForm());
            stage.setScene(s);
        });

        bar.getChildren().addAll(brand, rolePill, spacer, hello, logout);
        bar.setPadding(new Insets(14, 24, 14, 24));
        return bar;
    }
    private static Node buildContentForRole(Employee emp) {
        return switch (emp.getRole()) {
            case MANAGER       -> new ManagerDashboard().build();
            case RECEPTIONIST  -> new ReceptionistDashboard().build();
            case WAITER        -> new WaiterDashboard().build();
            case CHEF          -> new ChefDashboard().build();
            case CASHIER       -> new CashierDashboard().build();
        };
    }
}
