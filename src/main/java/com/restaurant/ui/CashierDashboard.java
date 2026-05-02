package com.restaurant.ui;

import com.restaurant.dao.BillDAO;
import com.restaurant.dao.OrderDAO;
import com.restaurant.model.Bill;
import com.restaurant.model.Order;
import com.restaurant.util.Dialogs;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class CashierDashboard {

    private final OrderDAO orderDao = new OrderDAO();
    private final BillDAO billDao = new BillDAO();

    public Node build() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
            new Tab("Issue check", buildIssueCheckTab()),
            new Tab("Unpaid bills", buildUnpaidTab())
        );
        VBox box = new VBox(tabs);
        box.setPadding(new Insets(20));
        VBox.setVgrow(tabs, Priority.ALWAYS);
        return box;
    }
    private Node buildIssueCheckTab() {
        TableView<Order> tv = new TableView<>();
        ObservableList<Order> data = FXCollections.observableArrayList(orderDao.findActive());
        tv.setItems(data);
        tv.getColumns().add(col("Order #", o -> String.valueOf(o.getId()), 80));
        tv.getColumns().add(col("Table", o -> "Table " + o.getTableNumber(), 100));
        tv.getColumns().add(col("Waiter", Order::getWaiterName, 160));
        tv.getColumns().add(col("Status", o -> o.getStatus().name(), 120));
        tv.getColumns().add(col("Subtotal", o -> String.format("$%.2f", o.getTotal()), 100));
        tv.getColumns().add(col("Created", o -> o.getCreatedAt().toString().replace('T', ' '), 180));

        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> data.setAll(orderDao.findActive()));

        Button issueBtn = new Button("Issue check (10% tax)");
        issueBtn.getStyleClass().add("btn-primary");
        issueBtn.setOnAction(e -> {
            Order sel = tv.getSelectionModel().getSelectedItem();
            if (sel == null) { Dialogs.info("Check", "Select an order first."); return; }
            Bill b = billDao.createOrGet(sel.getId());
            if (b == null) { Dialogs.error("Check", "Could not create bill."); return; }
            Dialogs.info("Bill created",
                String.format("Bill #%d for Table %d%nSubtotal: $%.2f%nTax: $%.2f%nTotal: $%.2f",
                    b.getId(), b.getTableNumber(), b.getAmount(), b.getTax(), b.getTotal()));
            data.setAll(orderDao.findActive());
        });

        HBox actions = new HBox(10, refresh, issueBtn);

        VBox box = new VBox(14, sectionLabel("Active orders to bill"), actions, tv);
        VBox.setVgrow(tv, Priority.ALWAYS);
        box.setPadding(new Insets(20, 4, 4, 4));
        return box;
    }

    private Node buildUnpaidTab() {
        TableView<Bill> tv = new TableView<>();
        ObservableList<Bill> data = FXCollections.observableArrayList(billDao.findUnpaid());
        tv.setItems(data);
        tv.getColumns().add(col("Bill #", b -> String.valueOf(b.getId()), 80));
        tv.getColumns().add(col("Order #", b -> String.valueOf(b.getOrderId()), 80));
        tv.getColumns().add(col("Table", b -> "Table " + b.getTableNumber(), 100));
        tv.getColumns().add(col("Subtotal", b -> String.format("$%.2f", b.getAmount()), 100));
        tv.getColumns().add(col("Tax", b -> String.format("$%.2f", b.getTax()), 80));
        tv.getColumns().add(col("Tip", b -> String.format("$%.2f", b.getTip()), 80));
        tv.getColumns().add(col("Total", b -> String.format("$%.2f", b.getTotal()), 100));

        Button addTip = new Button("Set tip");
        addTip.setOnAction(e -> {
            Bill sel = tv.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            String t = Dialogs.prompt("Tip", "Tip amount in $:", "0");
            if (t == null) return;
            try {
                billDao.updateTip(sel.getId(), Double.parseDouble(t.trim()));
                data.setAll(billDao.findUnpaid());
            } catch (NumberFormatException nfe) { Dialogs.error("Tip", "Not a number."); }
        });

        Button payCash   = payButton(tv, data, "CASH",        "btn-success");
        Button payCard   = payButton(tv, data, "CREDIT_CARD", "btn-primary");
        Button payCheck  = payButton(tv, data, "CHECK",       "btn-ghost");

        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> data.setAll(billDao.findUnpaid()));

        HBox actions = new HBox(10, refresh, addTip, payCash, payCard, payCheck);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(14, sectionLabel("Unpaid bills"), actions, tv);
        VBox.setVgrow(tv, Priority.ALWAYS);
        box.setPadding(new Insets(20, 4, 4, 4));
        return box;
    }

    private Button payButton(TableView<Bill> tv, ObservableList<Bill> data, String method, String style) {
        Button b = new Button("Pay (" + method.replace('_', ' ') + ")");
        b.getStyleClass().add(style);
        b.setOnAction(e -> {
            Bill sel = tv.getSelectionModel().getSelectedItem();
            if (sel == null) { Dialogs.info("Pay", "Select a bill first."); return; }
            if (!Dialogs.confirm("Pay", String.format("Charge $%.2f to %s?", sel.getTotal(), method))) return;
            if (billDao.pay(sel.getId(), method)) {
                Dialogs.info("Paid", "Payment recorded. Table is now free.");
                data.setAll(billDao.findUnpaid());
            } else {
                Dialogs.error("Pay", "Payment failed.");
            }
        });
        return b;
    }

    private static Label sectionLabel(String t) { Label l = new Label(t); l.getStyleClass().add("section-title"); return l; }
    private static <T> TableColumn<T, String> col(String title, java.util.function.Function<T, String> map, double width) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setCellValueFactory(d -> new SimpleStringProperty(map.apply(d.getValue()) == null ? "" : map.apply(d.getValue())));
        return c;
    }
}
