package com.restaurant.ui;

import com.restaurant.dao.OrderDAO;
import com.restaurant.model.Order;
import com.restaurant.model.OrderItem;
import com.restaurant.util.Dialogs;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ChefDashboard {

    private final OrderDAO orderDao = new OrderDAO();

    public Node build() {
        TableView<Order> tv = new TableView<>();
        ObservableList<Order> data = FXCollections.observableArrayList(orderDao.findByStatus(Order.Status.PREPARING));
        tv.setItems(data);
        tv.getColumns().add(col("Order #", o -> String.valueOf(o.getId()), 80));
        tv.getColumns().add(col("Table", o -> "Table " + o.getTableNumber(), 100));
        tv.getColumns().add(col("Waiter", Order::getWaiterName, 160));
        tv.getColumns().add(col("Status", o -> o.getStatus().name(), 120));
        tv.getColumns().add(col("Created", o -> o.getCreatedAt().toString().replace('T', ' '), 180));

        TableView<OrderItem> items = new TableView<>();
        items.getColumns().add(col("Seat", oi -> oi.getSeatNumber() == null ? "-" : "Seat " + oi.getSeatNumber(), 80));
        items.getColumns().add(col("Item", OrderItem::getItemTitle, 220));
        items.getColumns().add(col("Qty", oi -> String.valueOf(oi.getQuantity()), 70));

        tv.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) items.getItems().clear();
            else items.setItems(FXCollections.observableArrayList(orderDao.findItemsForOrder(sel.getId())));
        });

        Button markDone = new Button("Mark as Ready (Complete)");
        markDone.getStyleClass().add("btn-success");
        markDone.setOnAction(e -> {
            Order sel = tv.getSelectionModel().getSelectedItem();
            if (sel == null) { Dialogs.info("Mark", "Select an order first."); return; }
            orderDao.updateStatus(sel.getId(), Order.Status.RECEIVED); // RECEIVED = ready, awaiting cashier
            Dialogs.info("Done", "Order #" + sel.getId() + " marked ready. Cashier can now bill.");
            data.setAll(orderDao.findByStatus(Order.Status.PREPARING));
            items.getItems().clear();
        });

        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> data.setAll(orderDao.findByStatus(Order.Status.PREPARING)));

        HBox actions = new HBox(10, refresh, markDone);
        actions.setAlignment(Pos.CENTER_LEFT);

        Label tip = new Label("Showing orders with status = PREPARING. Mark one as ready when the food is plated.");
        tip.getStyleClass().add("muted");

        VBox left = new VBox(10, sectionLabel("Kitchen queue"), tv);
        VBox.setVgrow(tv, Priority.ALWAYS);
        left.getStyleClass().add("card");

        VBox right = new VBox(10, sectionLabel("Order details"), items);
        VBox.setVgrow(items, Priority.ALWAYS);
        right.getStyleClass().add("card");

        HBox split = new HBox(16, left, right);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        VBox box = new VBox(14, sectionLabel("Chef view"), tip, actions, split);
        VBox.setVgrow(split, Priority.ALWAYS);
        box.setPadding(new Insets(20));
        return box;
    }

    private static Label sectionLabel(String t) { Label l = new Label(t); l.getStyleClass().add("section-title"); return l; }
    private static <T> TableColumn<T, String> col(String title, java.util.function.Function<T, String> map, double width) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setCellValueFactory(d -> new SimpleStringProperty(map.apply(d.getValue()) == null ? "" : map.apply(d.getValue())));
        return c;
    }
}
