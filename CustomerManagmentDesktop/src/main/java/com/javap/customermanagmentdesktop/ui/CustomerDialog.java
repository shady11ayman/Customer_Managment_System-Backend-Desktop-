package com.javap.customermanagmentdesktop.ui;

import com.javap.customermanagmentdesktop.model.Customer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class CustomerDialog extends Stage {

    private final TextField nameField;
    private final TextField emailField;
    private final TextField phoneField;
    private final Label errorLabel;
    private Customer result = null;

    public CustomerDialog(Stage owner, Customer existingCustomer) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle(existingCustomer == null ? "Add New Customer" : "Edit Customer");
        setResizable(false);


        nameField = createTextField("Full name", 300);
        emailField = createTextField("Email address", 300);
        phoneField = createTextField("Phone number (optional)", 300);

        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);


        if (existingCustomer != null) {
            nameField.setText(existingCustomer.getName());
            emailField.setText(existingCustomer.getEmail());
            phoneField.setText(existingCustomer.getPhone() != null ? existingCustomer.getPhone() : "");
        }


        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);
        grid.setPadding(new Insets(24, 28, 16, 28));

        addFormRow(grid, 0, "Name *", nameField);
        addFormRow(grid, 1, "Email *", emailField);
        addFormRow(grid, 2, "Phone", phoneField);


        Button saveButton = new Button(existingCustomer == null ? "Add Customer" : "Save Changes");
        saveButton.setDefaultButton(true);
        saveButton.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-padding: 8 20; -fx-cursor: hand; " +
                        "-fx-background-radius: 6;"
        );
        saveButton.setOnMouseEntered(e -> saveButton.setStyle(
                "-fx-background-color: #2980b9; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-padding: 8 20; -fx-cursor: hand; " +
                        "-fx-background-radius: 6;"
        ));
        saveButton.setOnMouseExited(e -> saveButton.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-padding: 8 20; -fx-cursor: hand; " +
                        "-fx-background-radius: 6;"
        ));

        Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        cancelButton.setStyle(
                "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; " +
                        "-fx-font-size: 13px; -fx-padding: 8 20; -fx-cursor: hand; " +
                        "-fx-background-radius: 6;"
        );

        saveButton.setOnAction(e -> handleSave());
        cancelButton.setOnAction(e -> close());

        HBox buttonBox = new HBox(10, cancelButton, saveButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 28, 20, 28));


        Label titleLabel = new Label(existingCustomer == null ? "New Customer" : "Edit Customer");
        titleLabel.setStyle(
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        );
        HBox titleBox = new HBox(titleLabel);
        titleBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 16 28;");
        titleBox.setAlignment(Pos.CENTER_LEFT);


        HBox errorBox = new HBox(errorLabel);
        errorBox.setPadding(new Insets(0, 28, 4, 28));

        VBox root = new VBox(titleBox, grid, errorBox, buttonBox);
        root.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(root, 420, 340);
        setScene(scene);
    }

    private void handleSave() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();


        if (name.isEmpty()) {
            showError("Name is required.");
            nameField.requestFocus();
            return;
        }
        if (name.length() > 100) {
            showError("Name must not exceed 100 characters.");
            nameField.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            showError("Email is required.");
            emailField.requestFocus();
            return;
        }
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showError("Please enter a valid email address.");
            emailField.requestFocus();
            return;
        }
        if (email.length() > 100) {
            showError("Email must not exceed 100 characters.");
            emailField.requestFocus();
            return;
        }
        if (phone.length() > 50) {
            showError("Phone must not exceed 50 characters.");
            phoneField.requestFocus();
            return;
        }

        result = new Customer(name, email, phone.isEmpty() ? null : phone);
        close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private TextField createTextField(String prompt, double width) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefWidth(width);
        field.setStyle(
                "-fx-padding: 8 10; -fx-background-radius: 6; " +
                        "-fx-border-color: #dce1e7; -fx-border-radius: 6; -fx-font-size: 13px;"
        );
        return field;
    }

    private void addFormRow(GridPane grid, int row, String labelText, TextField field) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #5d6d7e; -fx-font-weight: bold;");
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }


    public Customer getResult() {
        return result;
    }
}
