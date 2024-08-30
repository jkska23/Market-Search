package edu.rpi.cs.csci4963.u24.kims35.hw05.farmersmarket;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Controller for the main view of the application.
 */
public class MainController {
    @FXML
    private TextField searchField;
    @FXML
    private ListView<String> marketListView;

    private DatabaseUtils databaseUtils;
    private List<String> searchResults;

    /**
     * Initializes the controller. Sets up the database connection and populates the search table.
     */
    public void initialize() {
        // Load properties from config file
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");
        databaseUtils = new DatabaseUtils(url, user, password);

        URL csvFilePath = this.getClass().getResource("/edu/rpi/cs/csci4963/u24/kims35/hw05/farmersmarket/Export.csv");
        if (csvFilePath != null) {
            DatabaseUtils.createSearchTable(csvFilePath.getPath());
        }
        DatabaseUtils.createReviewTable();

        marketListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedMarket = marketListView.getSelectionModel().getSelectedItem();
                if (selectedMarket != null) {
                    openDetailedView(selectedMarket);
                }
            }
        });
    }

    /**
     * Opens the configuration dialog.
     */
    @FXML
    private void onShowConfig() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ConfigDialog.fxml"));
            Scene scene = new Scene(loader.load(), 400, 430);

            ConfigDialog controller = loader.getController();
            controller.setDatabaseUtils(databaseUtils);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Configuration");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the application.
     */
    @FXML
    private void onClose() {
        Platform.exit();
    }

    /**
     * Searches for markets based on the input query and updates the list view with the results.
     */
    @FXML
    protected void onSearch() {
        String query = searchField.getText();
        marketListView.getItems().clear();

        String[] parts = query.split(",");
        if (parts.length == 3) {
            try {
                double latitude = Double.parseDouble(parts[0]);
                double longitude = Double.parseDouble(parts[1]);
                double radius = Double.parseDouble(parts[2]);

                searchResults = DatabaseUtils.searchMarkets(null, latitude, longitude, radius);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else {
            searchResults = DatabaseUtils.searchMarkets(query, null, null, null);
        }

        marketListView.getItems().addAll(searchResults);
    }

    /**
     * Opens the detailed view for the selected market.
     *
     * @param marketName the name of the selected market
     */
    private void openDetailedView(String marketName) {
        try (Connection conn = DriverManager.getConnection(databaseUtils.getUrl(), databaseUtils.getUser(), databaseUtils.getPassword())) {
            // Query to get market details
            String marketDetailsQuery = "SELECT id, street, city, County, State, zip FROM farmers_market WHERE MarketName = ?";
            String address = "";
            int marketId = -1;
            try (PreparedStatement marketStmt = conn.prepareStatement(marketDetailsQuery)) {
                marketStmt.setString(1, marketName);
                try (ResultSet rs = marketStmt.executeQuery()) {
                    if (rs.next()) {
                        marketId = rs.getInt("id");
                        address = rs.getString("street") + ", " + rs.getString("city") + ", " + rs.getString("County") + ", " + rs.getString("State") + " " + rs.getString("zip");
                    }
                }
            }

            // Query to get reviews and calculate average rating
            String reviewsQuery = "SELECT name, review_text, rating FROM reviews WHERE market_id = ?";
            List<String> reviews = new ArrayList<>();
            double totalRating = 0;
            int reviewCount = 0;
            try (PreparedStatement reviewsStmt = conn.prepareStatement(reviewsQuery)) {
                reviewsStmt.setInt(1, marketId);
                try (ResultSet rs = reviewsStmt.executeQuery()) {
                    while (rs.next()) {
                        String name = rs.getString("name");
                        String reviewText = rs.getString("review_text");
                        double rating = rs.getDouble("rating");
                        reviews.add("User: " + name + "\nRating: " + rating + "\n" + reviewText);
                        totalRating += rating;
                        reviewCount++;
                    }
                }
            }
            double averageRating = reviewCount > 0 ? totalRating / reviewCount : 0;

            // Load the detailed view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DetailedView.fxml"));
            Scene scene = new Scene(loader.load());

            DetailedController controller = loader.getController();
            controller.setMarketDetails(marketName, address, averageRating, reviews);
            controller.setDatabaseUtils(databaseUtils); // Set the databaseUtils instance
            controller.setMarketId(marketId); // Set the marketId

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Market Details");
            stage.setScene(scene);
            stage.show();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}