package edu.rpi.cs.csci4963.u24.kims35.hw05.farmersmarket;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * Main application class for the Farmers Market application.
 */
public class FarketApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Stage loadingScreen = startLoadingScreen();

        // load scene
        FXMLLoader fxmlLoader = new FXMLLoader(FarketApplication.class.getResource("MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 700);

        // set stage
        stage.setTitle("Farmers Market");
        stage.setScene(scene);
        loadingScreen.hide();
        stage.show();
    }

    private Stage startLoadingScreen() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Loading Screen");
        stage.show();

        Label label = new Label();
        label.setText("Loading the Program...");
        label.setFont(new Font(18));
        label.textAlignmentProperty().setValue(TextAlignment.CENTER);

        VBox containerVbox = new VBox(10);
        containerVbox.setAlignment(Pos.CENTER);
        containerVbox.getChildren().add(label);

        HBox container = new HBox();
        container.setAlignment(Pos.CENTER);
        container.getChildren().add(containerVbox);

        Scene scene = new Scene(container, 300, 300);
        stage.setResizable(false);
        stage.setScene(scene);
        return stage;
    }

    /**
     * Main entry point for the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch();
    }
}