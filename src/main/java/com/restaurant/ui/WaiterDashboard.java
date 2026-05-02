package com.restaurant.ui;

import com.restaurant.dao.MenuDAO;
import com.restaurant.dao.OrderDAO;
import com.restaurant.dao.TableDAO;
import com.restaurant.model.MenuItem;
import com.restaurant.model.Order;
import com.restaurant.model.OrderItem;
import com.restaurant.model.RestaurantTable;
import com.restaurant.util.Dialogs;
import com.restaurant.util.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.*;

import java.util.List;

public class WaiterDashboard {

    private final TableDAO tableDao = new TableDAO();
    private final MenuDAO menuDao = new MenuDAO();
    private final OrderDAO orderDao = new OrderDAO();

    private Order currentOrder;       // the order the waiter is currently building
    private final ObservableList<OrderItem> currentItems = FXCollections.observableArrayList();
    private final Label totalLbl = new Label("$0.00");

    public Node build() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
            new Tab("Place / edit order", buildOrderTab()),
            new Tab("Active orders", buildActiveOrders())
        );
        VBox wrap = new VBox(tabs);
        wrap.setPadding(new Insets(20));
        VBox.setVgrow(tabs, Priority.ALWAYS);
        return wrap;
    }
    private Node buildOrderTab() {

        ComboBox<RestaurantTable> tableBox = new ComboBox<>();
        refreshTableList(tableBox);

        Button newOrderBtn = new Button("Start new order for table");
        newOrderBtn.getStyleClass().add("btn-primary");

        Button loadExistingBtn = new Button("Load existing order for table");
        loadExistingBtn.getStyleClass().add("btn-ghost");

        Label currentLbl = new Label("No active order.");
        currentLbl.getStyleClass().add("muted");

        ListView<MenuItem> menuList = new ListView<>(FXCollections.observableArrayList(menuDao.findAllItems()));
        menuList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(MenuItem mi, boolean empty) {
                super.updateItem(mi, empty);
                if (empty || mi == null) { setText(null); return; }
                setText(String.format("[%s]  %s — $%.2f", mi.getSectionTitle(), mi.getTitle(), mi.getPrice()));
            }
        });

        Spinner<Integer> qty = new Spinner<>(1, 20, 1);
        Spinner<Integer> seat = new Spinner<>(1, 30, 1);

        Button addItemBtn = new Button("+ Add to order");
        addItemBtn.getStyleClass().add("btn-success");

        TableView<OrderItem> cart = new TableView<>(currentItems);
        cart.getColumns().add(col("Seat", oi -> oi.getSeatNumber() == null ? "-" : "Seat " + oi.getSeatNumber(), 80));
        cart.getColumns().add(col("Item", OrderItem::getItemTitle, 220));
        cart.getColumns().add(col("Qty", oi -> String.valueOf(oi.getQuantity()), 70));
        cart.getColumns().add(col("Price", oi -> String.format("$%.2f", oi.getPrice()), 90));
        cart.getColumns().add(col("Line", oi -> String.format("$%.2f", oi.getLineTotal()), 90));

        Button removeBtn = new Button("Remove selected line");
        removeBtn.getStyleClass().add("btn-danger");
        Button sendKitchenBtn = new Button("Send to kitchen");
        sendKitchenBtn.getStyleClass().add("btn-primary");

        newOrderBtn.setOnAction(e -> {
            RestaurantTable t = tableBox.getValue();
            if (t == null) { Dialogs.info("Order", "Pick a table first."); return; }
            int waiterId = Session.getCurrent().getId();
            int orderId = orderDao.createOrder(t.getId(), waiterId);
            if (orderId <= 0) { Dialogs.error("Order", "Could not create order."); return; }
            currentOrder = new Order();
            currentOrder.setId(orderId);
            currentOrder.setTableId(t.getId());
            currentOrder.setTableNumber(t.getTableNumber());
            currentItems.clear();
            currentLbl.setText("Active order #" + orderId + " for Table " + t.getTableNumber());
            currentLbl.getStyleClass().setAll("card-title");
            recalcTotal();
            refreshTableList(tableBox);
        });

        loadExistingBtn.setOnAction(e -> {
            RestaurantTable t = tableBox.getValue();
            if (t == null) return;
            // find latest active order for this table
            List<Order> active = orderDao.findActive();
            Order found = active.stream().filter(o -> o.getTableId() == t.getId()).findFirst().orElse(null);
            if (found == null) { Dialogs.info("Load", "No active order for that table."); return; }
            currentOrder = found;
            currentItems.setAll(orderDao.findItemsForOrder(found.getId()));
            currentLbl.setText("Active order #" + found.getId() + " for Table " + found.getTableNumber());
            currentLbl.getStyleClass().setAll("card-title");
            recalcTotal();
        });

        addItemBtn.setOnAction(e -> {
            if (currentOrder == null) { Dialogs.info("Add", "Start or load an order first."); return; }
            MenuItem mi = menuList.getSelectionModel().getSelectedItem();
            if (mi == null) { Dialogs.info("Add", "Pick a menu item from the list."); return; }
            orderDao.addItem(currentOrder.getId(), mi.getId(), qty.getValue(), seat.getValue());
            currentItems.setAll(orderDao.findItemsForOrder(currentOrder.getId()));
            recalcTotal();
        });

        removeBtn.setOnAction(e -> {
            OrderItem sel = cart.getSelectionModel().getSelectedItem();
            if (sel == null || currentOrder == null) return;
            orderDao.removeItem(sel.getId());
            currentItems.setAll(orderDao.findItemsForOrder(currentOrder.getId()));
            recalcTotal();
        });

        sendKitchenBtn.setOnAction(e -> {
            if (currentOrder == null) return;
            orderDao.updateStatus(currentOrder.getId(), Order.Status.PREPARING);
            Dialogs.info("Sent", "Order #" + currentOrder.getId() + " sent to the kitchen.");
            currentOrder = null;
            currentItems.clear();
            currentLbl.setText("No active order.");
            currentLbl.getStyleClass().setAll("muted");
            recalcTotal();
            refreshTableList(tableBox);
        });

        HBox topRow = new HBox(10,
            label("Table"), tableBox, newOrderBtn, loadExistingBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox menuBox = new VBox(8, label("Menu"), menuList,
            new HBox(8, label("Qty"), qty, label("Seat"), seat, addItemBtn));
        ((HBox) menuBox.getChildren().get(2)).setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(menuList, Priority.ALWAYS);
        menuBox.getStyleClass().add("card");

        Label totalCap = new Label("Total");
        totalCap.getStyleClass().add("muted");
        totalLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: #ff7b29;");

        VBox cartBox = new VBox(8, currentLbl, cart,
            new HBox(10, removeBtn, sendKitchenBtn,
                new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
                totalCap, totalLbl));
        ((HBox) cartBox.getChildren().get(2)).setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(cart, Priority.ALWAYS);
        cartBox.getStyleClass().add("card");

        HBox split = new HBox(16, menuBox, cartBox);
        HBox.setHgrow(menuBox, Priority.SOMETIMES);
        HBox.setHgrow(cartBox, Priority.ALWAYS);
        menuBox.setPrefWidth(420);

        VBox box = new VBox(14, sectionLabel("Place an order"), topRow, split);
        VBox.setVgrow(split, Priority.ALWAYS);
        box.setPadding(new Insets(20, 4, 4, 4));
        return box;
    }
    private void refreshTableList(ComboBox<RestaurantTable> box) {
        box.setItems(FXCollections.observableArrayList(tableDao.findAll()));
    }
    private void recalcTotal() {
        double t = 0;
        for (OrderItem oi : currentItems) t += oi.getLineTotal();
        totalLbl.setText(String.format("$%.2f", t));
    }
    private Node buildActiveOrders() {
        TableView<Order> tv = new TableView<>();
        ObservableList<Order> data = FXCollections.observableArrayList(orderDao.findActive());
        tv.setItems(data);
        tv.getColumns().add(col("Order #", o -> String.valueOf(o.getId()), 80));
        tv.getColumns().add(col("Table", o -> "Table " + o.getTableNumber(), 100));
        tv.getColumns().add(col("Waiter", Order::getWaiterName, 160));
        tv.getColumns().add(col("Status", o -> o.getStatus().name(), 120));
        tv.getColumns().add(col("Total", o -> String.format("$%.2f", o.getTotal()), 100));
        tv.getColumns().add(col("Created", o -> o.getCreatedAt().toString().replace('T', ' '), 180));

        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> data.setAll(orderDao.findActive()));

        VBox box = new VBox(14, sectionLabel("Active orders"), refresh, tv);
        VBox.setVgrow(tv, Priority.ALWAYS);
        box.setPadding(new Insets(20, 4, 4, 4));
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
