package com.silverplate.silverplate.Controller;

import com.silverplate.silverplate.ChangeLanguage;
import com.silverplate.silverplate.Main;
import com.silverplate.silverplate.Model.Staff;
import com.silverplate.silverplate.SQLConnect;
import com.silverplate.silverplate.WindowManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardPage implements Initializable {
    @FXML
            private Label revenue;
    @FXML
            private Label itemsSold;
    @FXML
            private BarChart<String, Number> salesGraph;

    ResourceBundle englishBundle = ResourceBundle.getBundle("Text_en_AU");


    // go to sales
    public void showSales() throws IOException {
        Stage salesStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("SalesPage.fxml"));
        fxmlLoader.setResources(englishBundle);
        Scene scene = new Scene(fxmlLoader.load());
        salesStage.setTitle("Sales");
        salesStage.setScene(scene);
        salesStage.show();
    }

    // show the products page
    public void showInventory() throws IOException {
        Stage inventoryStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("InventoryPage.fxml"));
        fxmlLoader.setResources(englishBundle);
        Scene scene = new Scene(fxmlLoader.load());
        inventoryStage.setTitle("Inventory");
        inventoryStage.setScene(scene);
        inventoryStage.show();
    }

    // show the products page
    public void showReports() throws IOException {
        // show the scene
        Stage reportsStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("ReportsPage.fxml"));
        fxmlLoader.setResources(englishBundle);
        Scene scene = new Scene(fxmlLoader.load());
        reportsStage.setTitle("Reports");
        reportsStage.setScene(scene);
        reportsStage.show();
    }

    // show the products page
    public void showStaff() throws IOException {
        // show the scene
        Stage staffStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("StaffPage.fxml"));
        fxmlLoader.setResources(englishBundle);
        Scene scene = new Scene(fxmlLoader.load());
        staffStage.setTitle("Staff");
        staffStage.setScene(scene);
        staffStage.show();
    }

    // show the products page
    public void showSuppliers() throws IOException {
        Stage supplierStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("SuppliersPage.fxml"));
        fxmlLoader.setResources(englishBundle);
        Scene scene = new Scene(fxmlLoader.load());
        supplierStage.setTitle("Suppliers");
        supplierStage.setScene(scene);
        supplierStage.show();
    }

    // show the products page
    public void showProducts() throws IOException {
        Stage productStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("ProductsPage.fxml"));
        fxmlLoader.setResources(englishBundle);
        Scene scene = new Scene(fxmlLoader.load());
        productStage.setTitle("Products");
        productStage.setScene(scene);
        productStage.show();
    }

    // ** QUICK SUMMARY **
    public void loadLabels() {
        // ... your other dashboard loading code ...

        int itemsSoldToday = getItemsSoldToday();
        itemsSold.setText(String.valueOf(itemsSoldToday));

        float salesRevenueToday = getSalesRevenueToday();
        revenue.setText(String.format("$%.2f", salesRevenueToday));
    }

    public int getItemsSoldToday() {
        String sql = "SELECT SUM(sale_quantity) FROM sales_product WHERE sales_id IN (SELECT sale_id FROM sales WHERE sale_date = CURDATE())";
        int itemsSoldToday = 0;

        try (
                Connection connection = SQLConnect.connectDb();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery();
        ) {
            if (resultSet.next()) {
                itemsSoldToday = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return itemsSoldToday;
    }

    public float getSalesRevenueToday() {
        String sql = "SELECT SUM(sale_price) FROM sales WHERE sale_date = CURDATE()";
        float salesRevenueToday = 0;

        try (
                Connection connection = SQLConnect.connectDb();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery();
        ) {
            if (resultSet.next()) {
                salesRevenueToday = resultSet.getFloat(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return salesRevenueToday;
    }

    // ** SALES GRAPH **
    private Map<LocalDate, Float> getSalesData() {
        Map<LocalDate, Float> salesData = new HashMap<>();

        Connection conn = SQLConnect.connectDb();

        String salesQuery = "SELECT sale_date, SUM(sale_price) as total_sales FROM sales " +
                "WHERE sale_date >= CURDATE() - INTERVAL 7 DAY " +
                "GROUP BY sale_date " +
                "ORDER BY sale_date ASC";
        try {
            PreparedStatement salesPreparedStatement = conn.prepareStatement(salesQuery);
            ResultSet salesResultSet = salesPreparedStatement.executeQuery();

            while (salesResultSet.next()) {
                Date saleDate = salesResultSet.getDate("sale_date");
                float totalSales = salesResultSet.getFloat("total_sales");

                salesData.put(saleDate.toLocalDate(), totalSales);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salesData;
    }

    public void plotSalesData() {
        Map<LocalDate, Float> salesData = getSalesData();

        // Create a series to plot the data
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (Map.Entry<LocalDate, Float> entry : salesData.entrySet()) {
            String date = entry.getKey().toString();
            Float totalSales = entry.getValue();

            // The X value is the date, the Y value is the total sales
            series.getData().add(new XYChart.Data<>(date.toString(), totalSales));
        }

        // Add the series to the line chart
        salesGraph.getData().add(series);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadLabels();
        plotSalesData();
    }

    //CHANGE LANGUAGE

    // get current stage
    private Stage getStage() {
        return (Stage) revenue.getScene().getWindow();
    }

    // change the language
    @FXML
    private void handleLanguageToggle() {
        try {
            ChangeLanguage.toggleLanguage(getStage(), "DashboardPage.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}