package com.restaurant.util;

import com.restaurant.dao.ReservationDAO;
import com.restaurant.model.Reservation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationService {
    private static Timeline timer;
    private static final Set<Integer> alreadyNotified = new HashSet<>();

    public static void start() {
        timer = new Timeline(new KeyFrame(Duration.seconds(60), e -> checkReservations()));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();

        checkReservations();
    }
    public static void stop() {
        if (timer != null) {
            timer.stop();
        }
        alreadyNotified.clear();
    }
    private static void checkReservations() {
        ReservationDAO dao = new ReservationDAO();
        List<Reservation> upcoming = dao.findUpcoming();
        LocalDateTime now = LocalDateTime.now();

        for (Reservation r : upcoming) {
            long minutesUntil = ChronoUnit.MINUTES.between(now, r.getReservationTime());

            if (minutesUntil >= 0 && minutesUntil <= 30 && !alreadyNotified.contains(r.getId())) {
                alreadyNotified.add(r.getId());

                Platform.runLater(() -> showAlert(r, minutesUntil));
            }
        }
    }

    private static void showAlert(Reservation r, long minutesUntil) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reservation Reminder");
        alert.setHeaderText("Upcoming Reservation in " + minutesUntil + " minutes!");
        alert.setContentText(
                "Customer: " + r.getCustomerName() + "\n" +
                        "Table: Table " + r.getTableNumber() + "\n" +
                        "People: " + r.getPeopleCount() + "\n" +
                        "Time: " + r.getReservationTime().toString().replace("T", " ")
        );
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.show();
    }
}