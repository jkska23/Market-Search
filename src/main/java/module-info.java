/**
 * Module definition for the Farmers Market application.
 */
module edu.rpi.cs.csci4963.u24.kims35.hw05.farmersmarket.farmersmarket {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires com.opencsv;


    opens edu.rpi.cs.csci4963.u24.kims35.hw05.farmersmarket to javafx.fxml;
    exports edu.rpi.cs.csci4963.u24.kims35.hw05.farmersmarket;
}