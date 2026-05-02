package com.restaurant.ui;

import com.restaurant.dao.CustomerDAO;
import com.restaurant.dao.ReservationDAO;
import com.restaurant.dao.TableDAO;
import com.restaurant.model.Customer;
import com.restaurant.model.Reservation;
import com.restaurant.model.RestaurantTable;
import com.restaurant.util.Dialogs;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ReceptionistDashboard {

    private final CustomerDAO custDao = new CustomerDAO();
    private final ReservationDAO resDao = new ReservationDAO();
    private final TableDAO tableDao = new TableDAO();

    public Node build() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
            new Tab("New reservation", buildSearchAndReserve()),
            new Tab("All reservations", buildList()),
            new Tab("Tables", buildTablesView())
        );

        VBox wrap = new VBox(tabs);
        wrap.setPadding(new Insets(20));
        VBox.setVgrow(tabs, Priority.ALWAYS);
        return wrap;
    }

    private Node buildSearchAndReserve() {
        Label title = new Label("Find a free table");
        title.getStyleClass().add("section-title");

        DatePicker date = new DatePicker(LocalDate.now());
        ComboBox<String> hour = new ComboBox<>();
        for (int h = 8; h <= 23; h++) for (int m : new int[]{0, 30}) hour.getItems().add(String.format("%02d:%02d", h, m));
        hour.setValue("19:00");
        Spinner<Integer> people = new Spinner<>(1, 30, 2);
        people.setEditable(true);

        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("btn-primary");

        HBox search = new HBox(10,
            label("Date"), date,
            label("Time"), hour,
            label("People"), people,
            searchBtn
        );
        search.setAlignment(Pos.CENTER_LEFT);

        TableView<RestaurantTable> tv = new TableView<>();
        tv.getColumns().add(col("ID", t -> String.valueOf(t.getId()), 60));
        tv.getColumns().add(col("Number", t -> "Table " + t.getTableNumber(), 100));
        tv.getColumns().add(col("Capacity", t -> String.valueOf(t.getMaxCapacity()), 90));
        tv.getColumns().add(col("Location", RestaurantTable::getLocationIdentifier, 130));
        tv.getColumns().add(col("Status", t -> t.getStatus().name(), 110));

        Button reserveBtn = new Button("Reserve selected table");
        reserveBtn.getStyleClass().add("btn-success");

        searchBtn.setOnAction(e -> {
            LocalDateTime when = LocalDateTime.of(date.getValue(), LocalTime.parse(hour.getValue() + ":00"));
            tv.setItems(FXCollections.observableArrayList(tableDao.searchAvailable(people.getValue(), when)));
        });

        reserveBtn.setOnAction(e -> {
            RestaurantTable sel = tv.getSelectionModel().getSelectedItem();
            if (sel == null) { Dialogs.info("Reserve", "Select a table from the list first."); return; }

            String phone = Dialogs.prompt("Customer phone", "Enter customer phone (we'll look them up)", "");
            if (phone == null || phone.isBlank()) return;
            Customer c = custDao.findByPhone(phone.trim());
            if (c == null) {
                String name = Dialogs.prompt("New customer", "No customer found. Full name:", "");
                if (name == null || name.isBlank()) return;
                String email = Dialogs.prompt("New customer", "Email (optional):", "");
                Customer nc = new Customer();
                nc.setFullName(name.trim());
                nc.setPhone(phone.trim());
                nc.setEmail(email == null ? "" : email.trim());
                int id = custDao.create(nc);
                if (id <= 0) { Dialogs.error("Reserve", "Failed to save customer."); return; }
                nc.setId(id);
                c = nc;
            }

            LocalDateTime when = LocalDateTime.of(date.getValue(), LocalTime.parse(hour.getValue() + ":00"));
            Reservation r = new Reservation();
            r.setCustomerId(c.getId());
            r.setTableId(sel.getId());
            r.setReservationTime(when);
            r.setPeopleCount(people.getValue());
            r.setStatus(Reservation.Status.CONFIRMED);
            r.setNotes("");
            int newId = resDao.create(r);
            if (newId > 0) {
                Dialogs.info("Reservation",
                    "Reserved Table " + sel.getTableNumber() +
                    " for " + c.getFullName() +
                    " on " + when.toString().replace('T', ' '));
                tv.setItems(FXCollections.observableArrayList(tableDao.searchAvailable(people.getValue(), when)));
            } else {
                Dialogs.error("Reservation", "Failed to create reservation.");
            }
        });

        VBox card = new VBox(14, title, search, tv, reserveBtn);
        card.setPadding(new Insets(20, 4, 4, 4));
        VBox.setVgrow(tv, Priority.ALWAYS);
        return card;
    }
    private Node buildList() {
        TableView<Reservation> tv = new TableView<>();
        ObservableList<Reservation> data = FXCollections.observableArrayList(resDao.findAll());
        tv.setItems(data);

        tv.getColumns().add(col("ID", r -> "#" + r.getId(), 60));
        tv.getColumns().add(col("Customer", Reservation::getCustomerName, 180));
        tv.getColumns().add(col("Table", r -> "Table " + r.getTableNumber(), 90));
        tv.getColumns().add(col("People", r -> String.valueOf(r.getPeopleCount()), 80));
        tv.getColumns().add(col("When", r -> r.getReservationTime().toString().replace('T', ' '), 180));
        tv.getColumns().add(col("Status", r -> r.getStatus().name(), 120));

        Button checkInBtn = new Button("Check-in");
        checkInBtn.getStyleClass().add("btn-success");
        checkInBtn.setOnAction(e -> {
            Reservation sel = tv.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            resDao.checkIn(sel.getId());
            data.setAll(resDao.findAll());
        });

        Button cancelBtn = new Button("Cancel reservation");
        cancelBtn.getStyleClass().add("btn-danger");
        cancelBtn.setOnAction(e -> {
            Reservation sel = tv.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            if (!Dialogs.confirm("Cancel", "Cancel reservation #" + sel.getId() + "?")) return;
            resDao.cancel(sel.getId());
            data.setAll(resDao.findAll());
            Dialogs.info("Cancelled", "Reservation cancelled and table freed.");
        });

        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> data.setAll(resDao.findAll()));

        HBox actions = new HBox(10, refresh, checkInBtn, cancelBtn);

        VBox box = new VBox(14, sectionLabel("All reservations"), actions, tv);
        box.setPadding(new Insets(20, 4, 4, 4));
        VBox.setVgrow(tv, Priority.ALWAYS);
        return box;
    }
    private Node buildTablesView() {
        TableView<RestaurantTable> tv = new TableView<>();
        ObservableList<RestaurantTable> data = FXCollections.observableArrayList(tableDao.findAll());
        tv.setItems(data);
        tv.getColumns().add(col("ID", t -> String.valueOf(t.getId()), 60));
        tv.getColumns().add(col("Number", t -> "Table " + t.getTableNumber(), 100));
        tv.getColumns().add(col("Capacity", t -> String.valueOf(t.getMaxCapacity()), 90));
        tv.getColumns().add(col("Location", RestaurantTable::getLocationIdentifier, 120));
        tv.getColumns().add(col("Status", t -> t.getStatus().name(), 110));

        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> data.setAll(tableDao.findAll()));
        VBox box = new VBox(14, sectionLabel("Tables"), refresh, tv);
        box.setPadding(new Insets(20, 4, 4, 4));
        VBox.setVgrow(tv, Priority.ALWAYS);
        return box;
    }

    private static Label label(String t) { Label l = new Label(t); l.getStyleClass().add("muted"); return l; }
    private static Label sectionLabel(String t) { Label l = new Label(t); l.getStyleClass().add("section-title"); return l; }
    private static <T> TableColumn<T, String> col(String title, java.util.function.Function<T, String> map, double width) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setCellValueFactory(d -> new SimpleStringProperty(map.apply(d.getValue()) == null ? "" : map.apply(d.getValue())));
        return c;
    }
}
