package edu.rpi.cs.csci4963.u24.kims35.hw05.farmersmarket;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Represents a configuration dialog for the application.
 */
public class ConfigDialog {
    @FXML
    private TextField urlField;
    @FXML
    private TextField userField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField distanceField;

    private DatabaseUtils databaseUtils;

    /**
     * Sets the database utility instance and initializes the text fields with the current database configuration.
     *
     * @param databaseUtils the database utility instance
     */
    public void setDatabaseUtils(DatabaseUtils databaseUtils) {
        this.databaseUtils = databaseUtils;
        urlField.setText(databaseUtils.getUrl());
        userField.setText(databaseUtils.getUser());
        passwordField.setText(databaseUtils.getPassword());
        distanceField.setText(String.valueOf(databaseUtils.getDistance()));
    }

    /**
     * Handles the save action. Updates the database configuration with the values from the text fields.
     */
    @FXML
    private void onSave() {
        databaseUtils.setUrl(urlField.getText());
        databaseUtils.setUser(userField.getText());
        databaseUtils.setPassword(passwordField.getText());
        databaseUtils.setDistance(Integer.parseInt(distanceField.getText()));
        close();
    }

    /**
     * Handles the cancel action. Closes the configuration dialog without saving changes.
     */
    @FXML
    private void onCancel() {
        close();
    }

    /**
     * Closes the configuration dialog.
     */
    private void close() {
        Stage stage = (Stage) urlField.getScene().getWindow();
        stage.close();
    }
}