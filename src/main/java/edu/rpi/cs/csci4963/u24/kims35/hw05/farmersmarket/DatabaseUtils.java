package edu.rpi.cs.csci4963.u24.kims35.hw05.farmersmarket;

import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

/**
 * Utility class for database operations.
 */
public class DatabaseUtils {
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    private static int distance = 30;

    /**
     * Constructs a new DatabaseUtils instance.
     *
     * @param url the database URL
     * @param user the database user
     * @param password the database password
     */
    public DatabaseUtils(String url, String user, String password) {
        this.URL = url;
        this.USER = user;
        this.PASSWORD = password;
    }

    /**
     * Gets the database URL.
     *
     * @return the database URL
     */
    public String getUrl() {
        return URL;
    }
    /**
     * Gets the database user.
     *
     * @return the database user
     */
    public String getUser() {
        return USER;
    }
    /**
     * Gets the database password.
     *
     * @return the database password
     */
    public String getPassword() {
        return PASSWORD;
    }
    /**
     * Gets the search distance.
     *
     * @return the search distance
     */
    public int getDistance() {
        return distance;
    }

    /**
     * Sets the database URL.
     *
     * @param url the new database URL
     */
    public void setUrl(String url) {
        this.URL = url;
    }
    /**
     * Sets the database user.
     *
     * @param user the new database user
     */
    public void setUser(String user) {
        this.USER = user;
    }
    /**
     * Sets the database password.
     *
     * @param password the new database password
     */
    public void setPassword(String password) {
        this.PASSWORD = password;
    }
    /**
     * Sets the search distance.
     *
     * @param distance the new search distance
     */
    public void setDistance(int distance) {
        this.distance = distance;
    }

    private static String escapeSQL(String value) {
        return value.replace("'", "''");
    }

    /**
     * Reads a CSV file and returns the data as a list of string arrays.
     *
     * @param csvfile the path to the CSV file
     * @return a list of string arrays containing the CSV data
     */
    public static List<String[]> readCSV(String csvfile) {
        List<String[]> data = new ArrayList<>();
        String[] headers = null;

        try (CSVReader csvReader = new CSVReader(new FileReader(csvfile))) {
            String[] values;
            boolean isFirstLine = true;

            while ((values = csvReader.readNext()) != null) {
                // Escape single quotes to avoid SQL syntax errors
                for (int i = 0; i < values.length; i++) {
                    values[i] = escapeSQL(values[i]);
                }
                // Skip first line and save it as headers
                if (isFirstLine) {
                    headers = values;
                    isFirstLine = false;
                } else {
                    data.add(values);
                }
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }

        if (headers != null) {
            data.add(0, headers); // Add headers as the first element
        }

        return data;
    }

    /**
     * Creates the search table in the database using data from a CSV file.
     *
     * @param csvfile the path to the CSV file
     */
    public static void createSearchTable(String csvfile) {
        String tableName = "farmers_market";
        List<String[]> data = readCSV(csvfile);
        if (data.isEmpty()) {
            return;
        }
        String[] headers = data.remove(0);

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Drop reviews table first to remove foreign key constraint
            String dropReviewsTableSQL = "DROP TABLE IF EXISTS reviews;";
            stmt.executeUpdate(dropReviewsTableSQL);

            // Drop farmers_market table
            String dropTableSQL = "DROP TABLE IF EXISTS " + tableName + ";";
            stmt.executeUpdate(dropTableSQL);

            // Create farmers_market table with primary key 'id'
            StringBuilder createTableSQL = new StringBuilder("CREATE TABLE " + tableName + " (");
            createTableSQL.append("id INT AUTO_INCREMENT PRIMARY KEY,");
            for (String header : headers) {
                createTableSQL.append(header).append(" TEXT,");
            }
            createTableSQL.setLength(createTableSQL.length() - 1);
            createTableSQL.append(");");

            stmt.executeUpdate(createTableSQL.toString());

            for (String[] row : data) {
                if (row.length != headers.length) {
                    System.err.println("Row length does not match header length. Skipping row: " + String.join(",", row));
                    continue;
                }

                StringBuilder insertSQL = new StringBuilder("INSERT INTO " + tableName + " VALUES (NULL,");
                for (String value : row) {
                    insertSQL.append("'").append(value).append("',");
                }

                insertSQL.setLength(insertSQL.length() - 1);
                insertSQL.append(");");
                stmt.executeUpdate(insertSQL.toString());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the review table in the database if it does not already exist.
     */
    public static void createReviewTable() {
        String tableName = "reviews";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Create table if it does not exist
            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    "review_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "market_id INT," +
                    "name VARCHAR(255)," +
                    "review_text TEXT," +
                    "rating DOUBLE," +
                    "FOREIGN KEY (market_id) REFERENCES farmers_market(id)" +
                    ");";
            stmt.executeUpdate(createTableSQL);
            System.out.println("Table " + tableName + " created successfully or already exists.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Searches for markets based on the query and returns a list of results.
     *
     * @param query the search query
     * @param latitude the latitude for distance-based search
     * @param longitude the longitude for distance-based search
     * @param radius the radius for distance-based search
     * @return a list of search results
     */
    public static List<String> searchMarkets(String query, Double latitude, Double longitude, Double radius) {
        List<String> results = new ArrayList<>();
        String sql;

        if (latitude != null && longitude != null && radius != null) {
            sql = "SELECT MarketName, " +
                    "(6371 * ACOS(COS(RADIANS(?)) * COS(RADIANS(latitude)) * COS(RADIANS(longitude) - RADIANS(?)) + SIN(RADIANS(?)) * SIN(RADIANS(latitude)))) * 0.621371 AS distance " +
                    "FROM farmers_market " +
                    "HAVING distance < ? " +
                    "ORDER BY distance";
        } else {
            sql = "SELECT fm.MarketName, fm.city, fm.State, r.rating " +
                    "FROM farmers_market fm " +
                    "LEFT JOIN reviews r ON fm.id = r.market_id " +
                    "WHERE fm.city LIKE ? OR fm.State LIKE ? OR fm.zip LIKE ?";
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (latitude != null && longitude != null && radius != null) {
                stmt.setDouble(1, latitude);
                stmt.setDouble(2, longitude);
                stmt.setDouble(3, latitude);
                stmt.setDouble(4, radius);
            } else {
                String searchPattern = "%" + query + "%";
                stmt.setString(1, searchPattern);
                stmt.setString(2, searchPattern);
                stmt.setString(3, searchPattern);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString("MarketName"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Checks if a user has already reviewed a specific market.
     *
     * @param marketId the ID of the market
     * @param name the name of the user
     * @return true if the user has already reviewed the market, false otherwise
     */
    public boolean hasUserReviewed(int marketId, String name) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE market_id = ? AND name = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, marketId);
            stmt.setString(2, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Adds a new review to the database.
     *
     * @param marketId the ID of the market
     * @param name the name of the user
     * @param rating the rating given by the user
     * @param comment the comment left by the user
     */
    public void addReview(int marketId, String name, int rating, String comment) {
        String sql = "INSERT INTO reviews (market_id, name, review_text, rating) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, marketId);
            stmt.setString(2, name);
            stmt.setString(3, comment);
            stmt.setInt(4, rating);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}