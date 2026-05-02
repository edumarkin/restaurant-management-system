package com.restaurant.ui;

import com.restaurant.dao.*;
import com.restaurant.model.Branch;
import com.restaurant.model.Employee;
import com.restaurant.model.MenuItem;
import com.restaurant.model.MenuSection;
import com.restaurant.model.Reservation;
import com.restaurant.model.RestaurantTable;
import com.restaurant.util.Dialogs;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

import java.time.LocalDate;

public class ManagerDashboard {

    private final EmployeeDAO empDao = new EmployeeDAO();
    private final MenuDAO menuDao = new MenuDAO();
    private final TableDAO tableDao = new TableDAO();
    private final BranchDAO branchDao = new BranchDAO();
    private final OrderDAO orderDao = new OrderDAO();
    private final ReservationDAO resDao = new ReservationDAO();

    public Node build() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabs.getTabs().addAll(
            new Tab("Overview", buildOverview()),
            new Tab("Employees", buildEmployees()),
            new Tab("Menu", buildMenu()),
            new Tab("Tables", buildTables())
        );
        VBox wrap = new VBox(tabs);
        wrap.setPadding(new Insets(20));
        VBox.setVgrow(tabs, Priority.ALWAYS);
        return wrap;
    }
    private Node buildOverview() {
        Label title = new Label("Overview");
        title.getStyleClass().add("section-title");

        HBox tiles = new HBox(16);
        int empCount = empDao.findAll().size();
        int activeOrders = orderDao.findActive().size();
        int upcomingRes = resDao.findUpcoming().size();
        int tableCount = tableDao.findAll().size();

        tiles.getChildren().addAll(
            statTile("Employees", String.valueOf(empCount)),
            statTile("Tables", String.valueOf(tableCount)),
            statTile("Active orders", String.valueOf(activeOrders)),
            statTile("Upcoming reservations", String.valueOf(upcomingRes))
        );
        Label resTitle = new Label("Upcoming reservations");
        resTitle.getStyleClass().add("card-title");
        TableView<Reservation> resTable = new TableView<>();
        resTable.setItems(FXCollections.observableArrayList(resDao.findUpcoming()));
        resTable.getColumns().add(col("ID", r -> "#" + r.getId(), 60));
        resTable.getColumns().add(col("Customer", Reservation::getCustomerName, 180));
        resTable.getColumns().add(col("Table", r -> "Table " + r.getTableNumber(), 100));
        resTable.getColumns().add(col("People", r -> String.valueOf(r.getPeopleCount()), 80));
        resTable.getColumns().add(col("When", r -> r.getReservationTime().toString().replace('T', ' '), 180));
        resTable.getColumns().add(col("Status", r -> r.getStatus().name(), 110));

        VBox card = new VBox(12, resTitle, resTable);
        card.getStyleClass().add("card");
        VBox.setVgrow(resTable, Priority.ALWAYS);

        VBox box = new VBox(16, title, tiles, card);
        box.setPadding(new Insets(20, 4, 4, 4));
        VBox.setVgrow(card, Priority.ALWAYS);
        return box;
    }

    private VBox statTile(String label, String value) {
        Label v = new Label(value); v.getStyleClass().add("stat-value");
        Label l = new Label(label); l.getStyleClass().add("stat-label");
        VBox b = new VBox(4, v, l);
        b.getStyleClass().add("stat-tile");
        HBox.setHgrow(b, Priority.ALWAYS);
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    private Node buildEmployees() {
        TableView<Employee> tv = new TableView<>();
        ObservableList<Employee> data = FXCollections.observableArrayList(empDao.findAll());
        tv.setItems(data);

        tv.getColumns().add(col("ID", e -> String.valueOf(e.getId()), 60));
        tv.getColumns().add(col("Username", Employee::getUsername, 130));
        tv.getColumns().add(col("Full name", Employee::getFullName, 200));
        tv.getColumns().add(col("Email", Employee::getEmail, 200));
        tv.getColumns().add(col("Phone", Employee::getPhone, 140));
        tv.getColumns().add(col("Role", e -> e.getRole().name(), 130));
        tv.getColumns().add(col("Status", Employee::getStatus, 100));

        Button addBtn = new Button("+ Add Employee");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> {
            Employee n = openEmployeeForm(null);
            if (n != null) { empDao.create(n); data.setAll(empDao.findAll()); }
        });

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> {
            Employee sel = tv.getSelectionModel().getSelectedItem();
            if (sel == null) { Dialogs.info("Edit", "Select an employee first."); return; }
            Employee updated = openEmployeeForm(sel);
            if (updated != null) { empDao.update(updated); data.setAll(empDao.findAll()); }
        });
        Button delBtn = new Button("Delete");
        delBtn.getStyleClass().add("btn-danger");
        delBtn.setOnAction(e -> {
            Employee sel = tv.getSelectionModel().getSelectedItem();
            if (sel == null) { Dialogs.info("Delete", "Select an employee first."); return; }
            if (!Dialogs.confirm("Delete", "Delete employee '" + sel.getFullName() + "'?")) return;
            empDao.delete(sel.getId());
            data.setAll(empDao.findAll());
        });

        HBox actions = new HBox(10, addBtn, editBtn, delBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(14, sectionLabel("Employees"), actions, tv);
        VBox.setVgrow(tv, Priority.ALWAYS);
        box.setPadding(new Insets(20, 4, 4, 4));
        return box;
    }
    private Employee openEmployeeForm(Employee existing) {
        Dialog<Employee> dlg = new Dialog<>();
        dlg.setTitle(existing == null ? "Add Employee" : "Edit Employee");
        dlg.getDialogPane().getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        dlg.getDialogPane().getStyleClass().add("root");
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextField user = new TextField();      user.setPromptText("username");
        TextField pass = new TextField();      pass.setPromptText("password");
        TextField name = new TextField();      name.setPromptText("Full name");
        TextField email = new TextField();     email.setPromptText("email");
        TextField phone = new TextField();     phone.setPromptText("phone");
        ComboBox<Employee.Role> role = new ComboBox<>(FXCollections.observableArrayList(Employee.Role.values()));
        role.setValue(Employee.Role.WAITER);
        ComboBox<Branch> branch = new ComboBox<>(FXCollections.observableArrayList(branchDao.findAll()));
        if (!branch.getItems().isEmpty()) branch.setValue(branch.getItems().get(0));

        if (existing != null) {
            user.setText(existing.getUsername());
            pass.setText(existing.getPassword());
            name.setText(existing.getFullName());
            email.setText(existing.getEmail());
            phone.setText(existing.getPhone());
            role.setValue(existing.getRole());
            for (Branch b : branch.getItems())
                if (existing.getBranchId() != null && b.getId() == existing.getBranchId()) branch.setValue(b);
        }
        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(10));
        int r = 0;
        g.add(new Label("Username"), 0, r); g.add(user, 1, r++);
        g.add(new Label("Password"), 0, r); g.add(pass, 1, r++);
        g.add(new Label("Full name"), 0, r); g.add(name, 1, r++);
        g.add(new Label("Email"), 0, r); g.add(email, 1, r++);
        g.add(new Label("Phone"), 0, r); g.add(phone, 1, r++);
        g.add(new Label("Role"), 0, r); g.add(role, 1, r++);
        g.add(new Label("Branch"), 0, r); g.add(branch, 1, r++);
        dlg.getDialogPane().setContent(g);

        dlg.setResultConverter(bt -> {
            if (bt != saveBtn) return null;
            Employee e = existing == null ? new Employee() : existing;
            e.setUsername(user.getText().trim());
            e.setPassword(pass.getText());
            e.setFullName(name.getText().trim());
            e.setEmail(email.getText().trim());
            e.setPhone(phone.getText().trim());
            e.setRole(role.getValue());
            if (branch.getValue() != null) e.setBranchId(branch.getValue().getId());
            if (e.getDateJoined() == null) e.setDateJoined(LocalDate.now());
            if (e.getStatus() == null) e.setStatus("ACTIVE");
            return e;
        });
        return dlg.showAndWait().orElse(null);
    }

    private Node buildMenu() {
        TableView<MenuItem> tv = new TableView<>();
        ObservableList<MenuItem> data = FXCollections.observableArrayList(menuDao.findAllItems());
        tv.setItems(data);

        tv.getColumns().add(col("ID", mi -> String.valueOf(mi.getId()), 60));
        tv.getColumns().add(col("Section", MenuItem::getSectionTitle, 130));
        tv.getColumns().add(col("Title", MenuItem::getTitle, 200));
        tv.getColumns().add(col("Description", MenuItem::getDescription, 280));
        tv.getColumns().add(col("Price", mi -> String.format("$%.2f", mi.getPrice()), 90));

        Button addBtn = new Button("+ Add Item");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> {
            MenuItem n = openMenuItemForm(null);
            if (n != null) { menuDao.createItem(n); data.setAll(menuDao.findAllItems()); }
        });

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> {
            MenuItem sel = tv.getSelectionModel().getSelectedItem();
            if (sel == null) { Dialogs.info("Edit", "Select an item first."); return; }
            MenuItem updated = openMenuItemForm(sel);
            if (updated != null) { menuDao.updateItem(updated); data.setAll(menuDao.findAllItems()); }
        });
        Button delBtn = new Button("Delete");
        delBtn.getStyleClass().add("btn-danger");
        delBtn.setOnAction(e -> {
            MenuItem sel = tv.getSelectionModel().getSelectedItem();
            if (sel == null) { Dialogs.info("Delete", "Select an item first."); return; }
            if (!Dialogs.confirm("Delete", "Delete '" + sel.getTitle() + "'?")) return;
            menuDao.deleteItem(sel.getId());
            data.setAll(menuDao.findAllItems());
        });
        HBox actions = new HBox(10, addBtn, editBtn, delBtn);
        VBox box = new VBox(14, sectionLabel("Menu Items"), actions, tv);
        VBox.setVgrow(tv, Priority.ALWAYS);
        box.setPadding(new Insets(20, 4, 4, 4));
        return box;
    }
    private MenuItem openMenuItemForm(MenuItem existing) {
        Dialog<MenuItem> dlg = new Dialog<>();
        dlg.setTitle(existing == null ? "Add Menu Item" : "Edit Menu Item");
        dlg.getDialogPane().getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        dlg.getDialogPane().getStyleClass().add("root");
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextField title = new TextField();
        TextField desc = new TextField();
        TextField price = new TextField();
        ComboBox<MenuSection> sec = new ComboBox<>(FXCollections.observableArrayList(menuDao.findSections()));
        if (!sec.getItems().isEmpty()) sec.setValue(sec.getItems().get(0));

        if (existing != null) {
            title.setText(existing.getTitle());
            desc.setText(existing.getDescription());
            price.setText(String.valueOf(existing.getPrice()));
            for (MenuSection s : sec.getItems()) if (s.getId() == existing.getSectionId()) sec.setValue(s);
        }

        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(10));
        g.add(new Label("Section"), 0, 0); g.add(sec, 1, 0);
        g.add(new Label("Title"), 0, 1); g.add(title, 1, 1);
        g.add(new Label("Description"), 0, 2); g.add(desc, 1, 2);
        g.add(new Label("Price ($)"), 0, 3); g.add(price, 1, 3);
        dlg.getDialogPane().setContent(g);

        dlg.setResultConverter(bt -> {
            if (bt != saveBtn) return null;
            MenuItem mi = existing == null ? new MenuItem() : existing;
            mi.setTitle(title.getText().trim());
            mi.setDescription(desc.getText().trim());
            try { mi.setPrice(Double.parseDouble(price.getText().trim())); }
            catch (NumberFormatException nfe) { mi.setPrice(0); }
            if (sec.getValue() != null) mi.setSectionId(sec.getValue().getId());
            return mi;
        });
        return dlg.showAndWait().orElse(null);
    }

    private Node buildTables() {
        TableView<RestaurantTable> tv = new TableView<>();
        ObservableList<RestaurantTable> data = FXCollections.observableArrayList(tableDao.findAll());
        tv.setItems(data);

        tv.getColumns().add(col("ID", t -> String.valueOf(t.getId()), 60));
        tv.getColumns().add(col("Number", t -> "Table " + t.getTableNumber(), 100));
        tv.getColumns().add(col("Capacity", t -> String.valueOf(t.getMaxCapacity()), 90));
        tv.getColumns().add(col("Location", RestaurantTable::getLocationIdentifier, 120));
        tv.getColumns().add(col("Status", t -> t.getStatus().name(), 110));

        Button addBtn = new Button("+ Add Table");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> {
            RestaurantTable t = openTableForm(null);
            if (t != null) { tableDao.create(t); data.setAll(tableDao.findAll()); }
        });

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> {
            RestaurantTable sel = tv.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            RestaurantTable upd = openTableForm(sel);
            if (upd != null) { tableDao.update(upd); data.setAll(tableDao.findAll()); }
        });

        Button delBtn = new Button("Delete");
        delBtn.getStyleClass().add("btn-danger");
        delBtn.setOnAction(e -> {
            RestaurantTable sel = tv.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            if (!Dialogs.confirm("Delete", "Delete Table " + sel.getTableNumber() + "?")) return;
            tableDao.delete(sel.getId());
            data.setAll(tableDao.findAll());
        });

        HBox actions = new HBox(10, addBtn, editBtn, delBtn);
        VBox box = new VBox(14, sectionLabel("Tables / Layout"), actions, tv);
        VBox.setVgrow(tv, Priority.ALWAYS);
        box.setPadding(new Insets(20, 4, 4, 4));
        return box;
    }

    private RestaurantTable openTableForm(RestaurantTable existing) {
        Dialog<RestaurantTable> dlg = new Dialog<>();
        dlg.setTitle(existing == null ? "Add Table" : "Edit Table");
        dlg.getDialogPane().getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        dlg.getDialogPane().getStyleClass().add("root");
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        Spinner<Integer> num = new Spinner<>(1, 999, 1);
        Spinner<Integer> cap = new Spinner<>(1, 30, 4);
        num.setEditable(true); cap.setEditable(true);
        TextField loc = new TextField(); loc.setPromptText("e.g. Window, VIP");
        ComboBox<RestaurantTable.Status> status = new ComboBox<>(FXCollections.observableArrayList(RestaurantTable.Status.values()));
        status.setValue(RestaurantTable.Status.FREE);
        ComboBox<Branch> branch = new ComboBox<>(FXCollections.observableArrayList(branchDao.findAll()));
        if (!branch.getItems().isEmpty()) branch.setValue(branch.getItems().get(0));

        if (existing != null) {
            num.getValueFactory().setValue(existing.getTableNumber());
            cap.getValueFactory().setValue(existing.getMaxCapacity());
            loc.setText(existing.getLocationIdentifier());
            status.setValue(existing.getStatus());
            for (Branch b : branch.getItems()) if (b.getId() == existing.getBranchId()) branch.setValue(b);
        }

        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(10));
        int r = 0;
        g.add(new Label("Branch"), 0, r); g.add(branch, 1, r++);
        g.add(new Label("Number"), 0, r); g.add(num, 1, r++);
        g.add(new Label("Capacity"), 0, r); g.add(cap, 1, r++);
        g.add(new Label("Location tag"), 0, r); g.add(loc, 1, r++);
        g.add(new Label("Status"), 0, r); g.add(status, 1, r++);
        dlg.getDialogPane().setContent(g);

        dlg.setResultConverter(bt -> {
            if (bt != saveBtn) return null;
            RestaurantTable t = existing == null ? new RestaurantTable() : existing;
            t.setTableNumber(num.getValue());
            t.setMaxCapacity(cap.getValue());
            t.setLocationIdentifier(loc.getText().trim());
            t.setStatus(status.getValue());
            if (branch.getValue() != null) t.setBranchId(branch.getValue().getId());
            return t;
        });
        return dlg.showAndWait().orElse(null);
    }

    private static <T> TableColumn<T, String> col(String title, java.util.function.Function<T, String> map, double width) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setCellValueFactory(d -> {
            String v = map.apply(d.getValue());
            return new SimpleStringProperty(v == null ? "" : v);
        });
        return c;
    }

    private static Label sectionLabel(String txt) {
        Label l = new Label(txt);
        l.getStyleClass().add("section-title");
        return l;
    }
}
