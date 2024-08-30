package edu.rpi.cs.csci4963.u24.kims35.hw05.farmersmarket;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;

/**
 * Controller for the detailed view of a market.
 */
public class DetailedController {
    @FXML
    private Label marketName;
    @FXML
    private Label marketAddress;
    @FXML
    private Label marketRating;
    @FXML
    private ListView<String> reviewListView;
    @FXML
    private TextField nameField;
    @FXML
    private TextField ratingField;
    @FXML
    private TextArea commentField;

    private DatabaseUtils databaseUtils;
    private int marketId;

    /**
     * Sets the market details in the detailed view.
     *
     * @param name the name of the market
     * @param address the address of the market
     * @param rating the average rating of the market
     * @param reviews the list of reviews for the market
     */
    public void setMarketDetails(String name, String address, double rating, List<String> reviews) {
        marketName.setText(name);
        marketAddress.setText(address);
        marketRating.setText("Rating: " + rating);
        reviewListView.getItems().setAll(reviews);
    }

    /**
     * Sets the database utility instance.
     *
     * @param databaseUtils the database utility instance
     */
    public void setDatabaseUtils(DatabaseUtils databaseUtils) {
        this.databaseUtils = databaseUtils;
    }

    /**
     * Sets the market ID.
     *
     * @param marketId the market ID
     */
    public void setMarketId(int marketId) {
        this.marketId = marketId;
    }

    /**
     * Handles the submission of a review.
     */
    @FXML
    private void onSubmitReview() {
        String name = nameField.getText();
        String ratingText = ratingField.getText();
        String comment = commentField.getText();

        if (name.isEmpty() || ratingText.isEmpty()) {
            // Show alert that name and rating are required
            System.out.println("Name and rating are required.");
            return;
        }

        int rating;
        try {
            rating = Integer.parseInt(ratingText);
            if (rating < 1 || rating > 5) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.out.println("Rating must be a number between 1 and 5.");
            return;
        }

        if (databaseUtils.hasUserReviewed(marketId, name)) {
            // Show alert that user has already left a review
            System.out.println("You have already left a review for this market.");
        } else {
            databaseUtils.addReview(marketId, name, rating, comment);
            // Optionally, refresh the review list view
            reviewListView.getItems().add(comment);
            // Clear the input fields
            nameField.clear();
            ratingField.clear();
            commentField.clear();
        }
    }
}