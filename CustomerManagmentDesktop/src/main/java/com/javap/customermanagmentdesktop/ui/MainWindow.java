package com.javap.customermanagmentdesktop.ui;

import com.javap.customermanagmentdesktop.model.ApiResponse;
import com.javap.customermanagmentdesktop.model.Customer;
import com.javap.customermanagmentdesktop.service.CustomerApiService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class    MainWindow {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Stage stage;
    private final CustomerApiService apiService;
    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    // UI components
    private TableView<Customer> tableView;
    private TextField searchField;
    private Label statusLabel;
    private ProgressIndicator loadingIndicator;
    private Button addButton;
    private Button editButton;
    private Button deleteButton;
    private Button refreshButton;

    public MainWindow(Stage stage) {
        this.stage = stage;
        this.apiService = new CustomerApiService();
    }

    public void show() {
        stage.setTitle("Customer Management — v1.0");
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        VBox root = new VBox();
        root.setStyle("-fx-background-color: #f0f2f5;");


        HBox header = buildHeader();

        HBox toolbar = buildToolbar();


        tableView = buildTable();
        VBox.setVgrow(tableView, Priority.ALWAYS);

        HBox statusBar = buildStatusBar();

        root.getChildren().addAll(header, toolbar, tableView, statusBar);

        Scene scene = new Scene(root, 1000, 680);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> executor.shutdown());
        stage.show();

        loadCustomers();
    }


    private HBox buildHeader() {
        Label titleLabel = new Label("Customer Management");
        titleLabel.setStyle(
                "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;"
        );
        Label subLabel = new Label("Spring Boot + JavaFX");
        subLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #bdc3c7;");

        VBox titleBox = new VBox(2, titleLabel, subLabel);

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(28, 28);
        loadingIndicator.setVisible(false);
        loadingIndicator.setStyle("-fx-accent: white;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setStyle("-fx-background-color: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titleBox, spacer, loadingIndicator);
        return header;
    }

    private HBox buildToolbar() {
        searchField = new TextField();
        searchField.setPromptText("Search by name, email, or phone...");
        searchField.setPrefWidth(300);
        searchField.setStyle(
                "-fx-padding: 7 12; -fx-background-radius: 6; " +
                        "-fx-border-color: #dce1e7; -fx-border-radius: 6; -fx-font-size: 13px;"
        );

        Button searchButton = createButton("Search", "#27ae60", "#1e8449");
        searchButton.setOnAction(e -> performSearch());
        searchField.setOnAction(e -> performSearch());

        Button clearButton = createButton("Clear", "#95a5a6", "#7f8c8d");
        clearButton.setOnAction(e -> {
            searchField.clear();
            loadCustomers();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        addButton = createButton("+ Add Customer", "#3498db", "#2980b9");
        editButton = createButton("Edit", "#f39c12", "#d68910");
        deleteButton = createButton("Delete", "#e74c3c", "#c0392b");
        refreshButton = createButton("↻ Refresh", "#8e44ad", "#7d3c98");

        editButton.setDisable(true);
        deleteButton.setDisable(true);

        addButton.setOnAction(e -> handleAdd());
        editButton.setOnAction(e -> handleEdit());
        deleteButton.setOnAction(e -> handleDelete());
        refreshButton.setOnAction(e -> {
            searchField.clear();
            loadCustomers();
        });

        HBox toolbar = new HBox(10,
                searchField, searchButton, clearButton,
                spacer,
                refreshButton, addButton, editButton, deleteButton
        );
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(12, 20, 12, 20));
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        return toolbar;
    }

    @SuppressWarnings("unchecked")
    private TableView<Customer> buildTable() {
        TableView<Customer> table = new TableView<>(customerList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-size: 13px;");
        table.setPlaceholder(new Label("No customers found. Click '+ Add Customer' to get started."));

        TableColumn<Customer, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);
        idCol.setMinWidth(50);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Customer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setMinWidth(160);

        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setMinWidth(200);

        TableColumn<Customer, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setMinWidth(130);

        TableColumn<Customer, String> createdAtCol = new TableColumn<>("Created At");
        createdAtCol.setCellValueFactory(data -> {
            if (data.getValue().getCreatedAt() != null) {
                return new SimpleStringProperty(
                        data.getValue().getCreatedAt().format(DATE_FORMATTER)
                );
            }
            return new SimpleStringProperty("—");
        });
        createdAtCol.setMinWidth(140);

        table.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, createdAtCol);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            boolean hasSelection = selected != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });

        table.setRowFactory(tv -> {
            TableRow<Customer> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEdit();
                }
            });
            return row;
        });

        return table;
    }

    private HBox buildStatusBar() {
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #5d6d7e;");

        HBox statusBar = new HBox(statusLabel);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(6, 16, 6, 16));
        statusBar.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #d5d8dc; -fx-border-width: 1 0 0 0;");
        return statusBar;
    }


    private void loadCustomers() {
        setLoading(true);
        setStatus("Loading customers...");
        executor.submit(() -> {
            ApiResponse<List<Customer>> response = apiService.getAllCustomers();
            Platform.runLater(() -> {
                setLoading(false);
                if (response.isSuccess()) {
                    customerList.setAll(response.getData());
                    setStatus("Loaded " + response.getData().size() + " customer(s).");
                } else {
                    showError("Failed to load customers", response.getErrorMessage());
                    setStatus("Error: " + response.getErrorMessage());
                }
            });
        });
    }

    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadCustomers();
            return;
        }
        setLoading(true);
        setStatus("Searching for \"" + query + "\"...");
        executor.submit(() -> {
            ApiResponse<List<Customer>> response = apiService.searchCustomers(query);
            Platform.runLater(() -> {
                setLoading(false);
                if (response.isSuccess()) {
                    customerList.setAll(response.getData());
                    setStatus("Found " + response.getData().size() + " result(s) for \"" + query + "\".");
                } else {
                    showError("Search failed", response.getErrorMessage());
                    setStatus("Search error: " + response.getErrorMessage());
                }
            });
        });
    }

    private void handleAdd() {
        CustomerDialog dialog = new CustomerDialog(stage, null);
        dialog.showAndWait();
        Customer newCustomer = dialog.getResult();
        if (newCustomer == null) return;

        setLoading(true);
        setStatus("Adding customer...");
        executor.submit(() -> {
            ApiResponse<Customer> response = apiService.createCustomer(newCustomer);
            Platform.runLater(() -> {
                setLoading(false);
                if (response.isSuccess()) {
                    customerList.add(0, response.getData());
                    tableView.getSelectionModel().select(response.getData());
                    setStatus("Customer \"" + response.getData().getName() + "\" added successfully.");
                } else {
                    showError("Failed to add customer", response.getErrorMessage());
                    setStatus("Error adding customer.");
                }
            });
        });
    }

    private void handleEdit() {
        Customer selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        CustomerDialog dialog = new CustomerDialog(stage, selected);
        dialog.showAndWait();
        Customer updated = dialog.getResult();
        if (updated == null) return;

        setLoading(true);
        setStatus("Updating customer...");
        executor.submit(() -> {
            ApiResponse<Customer> response = apiService.updateCustomer(selected.getId(), updated);
            Platform.runLater(() -> {
                setLoading(false);
                if (response.isSuccess()) {
                    int idx = customerList.indexOf(selected);
                    if (idx >= 0) {
                        customerList.set(idx, response.getData());
                        tableView.getSelectionModel().select(idx);
                    }
                    setStatus("Customer \"" + response.getData().getName() + "\" updated successfully.");
                } else {
                    showError("Failed to update customer", response.getErrorMessage());
                    setStatus("Error updating customer.");
                }
            });
        });
    }

    private void handleDelete() {
        Customer selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete customer?");
        confirm.setContentText(
                "Are you sure you want to delete:\n\n" +
                        "Name: " + selected.getName() + "\n" +
                        "Email: " + selected.getEmail() + "\n\n" +
                        "This action cannot be undone."
        );

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        setLoading(true);
        setStatus("Deleting customer...");
        executor.submit(() -> {
            ApiResponse<Void> response = apiService.deleteCustomer(selected.getId());
            Platform.runLater(() -> {
                setLoading(false);
                if (response.isSuccess()) {
                    customerList.remove(selected);
                    editButton.setDisable(true);
                    deleteButton.setDisable(true);
                    setStatus("Customer \"" + selected.getName() + "\" deleted successfully.");
                } else {
                    showError("Failed to delete customer", response.getErrorMessage());
                    setStatus("Error deleting customer.");
                }
            });
        });
    }


    private Button createButton(String text, String normalColor, String hoverColor) {
        Button button = new Button(text);
        String baseStyle = "-fx-background-color: %s; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-padding: 7 14; -fx-cursor: hand; " +
                "-fx-background-radius: 6;";
        button.setStyle(String.format(baseStyle, normalColor));
        button.setOnMouseEntered(e -> button.setStyle(String.format(baseStyle, hoverColor)));
        button.setOnMouseExited(e -> button.setStyle(String.format(baseStyle, normalColor)));
        return button;
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        addButton.setDisable(loading);
        refreshButton.setDisable(loading);
        searchField.setDisable(loading);
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(stage);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
