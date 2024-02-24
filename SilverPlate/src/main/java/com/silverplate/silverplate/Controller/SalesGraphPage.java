package com.silverplate.silverplate.Controller;

import com.silverplate.silverplate.ChangeLanguage;
import com.silverplate.silverplate.Model.Inventory;
import com.silverplate.silverplate.Model.Product;
import com.silverplate.silverplate.Model.Sales;
import com.silverplate.silverplate.SQLConnect;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class SalesGraphPage implements Initializable {
    @FXML
    private LineChart<String, Number> salesGraph;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        plotSalesData();
    }

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
        salesGraph.getData().clear();

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

    //CHANGE LANGUAGE

    // get current stage
    private Stage getStage() {
        return (Stage) salesGraph.getScene().getWindow();
    }

    // change the language
    @FXML
    private void handleLanguageToggle() {
        try {
            ChangeLanguage.toggleLanguage(getStage(), "SalesGraphPage.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
