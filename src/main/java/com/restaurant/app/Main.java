package com.restaurant.app;

import com.restaurant.ui.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.sql.Connection;

public class Main extends Application {
    public static final String APP_TITLE = "Restaurant Management System";

    @Override
    public void start(Stage stage) {
        try (Connection ignored = com.restaurant.db.Database.get()) {

        } catch (Exception e) {
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR,
                "Could not connect to MySQL.\n\n" + e.getMessage() +
                "\n\nMake sure:\n" +
                " MySQL server is running\n" +
                " You ran the schema.sql file\n" +
                " db.properties has the right user/password");
            a.setHeaderText("Database connection failed");
            a.showAndWait();
            return;
        }
        Scene scene = LoginView.build(stage);
        scene.getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());

        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
        stage.setWidth(1200);
        stage.setHeight(780);
        stage.setMinWidth(1000);
        stage.setMinHeight(680);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
