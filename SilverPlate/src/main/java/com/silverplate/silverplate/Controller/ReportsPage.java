package com.silverplate.silverplate.Controller;

import com.silverplate.silverplate.ChangeLanguage;
import com.silverplate.silverplate.Model.Inventory;
import com.silverplate.silverplate.Model.Product;
import com.silverplate.silverplate.SQLConnect;
import javafx.application.Application;

import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;


public class ReportsPage implements Initializable {
        @FXML
        private ComboBox timeCombo;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        timeCombo.setItems(FXCollections.observableArrayList("Daily", "Weekly"));
    }

    // HANDLE THE GENERATE REQUEST
    public void handleGenerate() {
        String reportType = timeCombo.getValue().toString();

        if (reportType == null) {
            showAlert("Error","Please select a report type.");
        }

        createReport(reportType);
        showAlert("Success","Report generated successfully.");
    }

    public void createReport(String reportType) {
        // Prepare the data for the report
        List<Inventory> inventoryData = getDataInventory();
        Map<LocalDate, Map<Product, Integer>> salesDailyData;
        Map<LocalDate, Float> salesWeeklyData;

        // Create a new PDF document
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Write the report title
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.beginText();
                contentStream.newLineAtOffset(20, page.getMediaBox().getHeight() - 50);
                contentStream.showText(reportType + " Sales and Inventory Report for " + LocalDate.now());
                contentStream.endText();

                // Depending on the report type, call the appropriate method
                if (reportType.equals("Daily")) {
                    salesDailyData = getSalesData(LocalDate.now());
                    createDailyReport(contentStream, inventoryData, salesDailyData);
                } else if (reportType.equals("Weekly")) {
                    salesWeeklyData = getWeeklySalesData();
                    createWeeklyReport(contentStream, inventoryData, salesWeeklyData);
                }
            }

            // Save the document
            document.save("Reports/" + reportType + "Report-" + LocalDate.now() + ".pdf");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // CREATE THE WEEKLY REPORT
    private void createWeeklyReport(PDPageContentStream contentStream, List<Inventory> inventoryData, Map<LocalDate, Float> salesData) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(20, 750);

        // For simplicity, let's just write the total sales for each day in the past week and the inventory data as strings
        for (LocalDate date = LocalDate.now().minusDays(7); !date.isAfter(LocalDate.now()); date = date.plusDays(1)) {
            if (salesData.containsKey(date)) {
                contentStream.showText("Total sales for " + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ": $" + salesData.get(date));
                contentStream.newLineAtOffset(0, -15);
            }
        }
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText("-- Inventory --");
        contentStream.newLineAtOffset(0, -15);
        // Iterate over the Inventory objects in inventoryData
        for (Inventory inventoryItem : inventoryData) {
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);

            contentStream.newLine();
            // Convert the Inventory object to a String for this example
            String inventoryString = inventoryItem.toString();

            // Write the text
            contentStream.showText(inventoryString);
            contentStream.newLineAtOffset(0, -15);
        }

        contentStream.endText();
    }


    // CREATE THE DAILY REPORT
    private void createDailyReport(PDPageContentStream contentStream, List<Inventory> inventoryData, Map<LocalDate, Map<Product, Integer>> salesData) throws IOException {
        // Set the font and size for the text in the PDF document
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);

        // Begin writing text to the PDF document
        contentStream.beginText();
        // Move the cursor to the starting point for the text
        contentStream.newLineAtOffset(20, 750);

        // Write the date of the sales data to the PDF document
        contentStream.showText("Sales for " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ":");
        contentStream.newLineAtOffset(0, -15);

        // Move to the next line
        float totalSalesPrice = 0f;

        for (Map.Entry<LocalDate, Map<Product, Integer>> dateEntry : salesData.entrySet()) {
            LocalDate date = dateEntry.getKey();
            Map<Product, Integer> productQuantityMap = dateEntry.getValue();

            for (Map.Entry<Product, Integer> pair :  productQuantityMap.entrySet()) {
                contentStream.newLine();

                Product product = pair.getKey();
                int quantity = pair.getValue();

                String saleLine = String.format("Product: %s, Quantity: %d, Sale Price: $%.2f",
                        product.getProductName(),
                        quantity,
                        product.getProductPrice() * quantity);

                totalSalesPrice += product.getProductPrice() * quantity;
                contentStream.newLineAtOffset(0, -10);

                contentStream.showText(saleLine);
                contentStream.newLineAtOffset(0, -10);
            }

            contentStream.newLine();
            contentStream.newLineAtOffset(0, -30);
            contentStream.showText("Total Sales Price for the Day: $" + totalSalesPrice);
            contentStream.newLineAtOffset(0, -40);

            // TITLE FOR INVENTORY
            contentStream.showText("--- Inventory ---");
            contentStream.newLineAtOffset(0, -20);
        }

        // Iterate over the Inventory objects in inventoryData
        for (Inventory inventoryItem : inventoryData) {
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);

            contentStream.newLine();
            // Convert the Inventory object to a String for this example
            String inventoryString = inventoryItem.toString();

            // Write the text
            contentStream.showText(inventoryString);
            contentStream.newLineAtOffset(0, -15);
        }
        // End the text object
        contentStream.endText();
    }


    // SALES DATA
    // WEEKLY
    private Map<LocalDate, Float> getWeeklySalesData() {
        Map<LocalDate, Float> salesData = new HashMap<>();

        String salesQuery = "SELECT sale_date, SUM(sale_price) as total_sales FROM sales WHERE sale_date >= CURDATE() - INTERVAL 7 DAY GROUP BY sale_date ORDER BY sale_date ASC";
        try (Connection c = SQLConnect.connectDb();
             PreparedStatement pstmt = c.prepareStatement(salesQuery)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    salesData.put(rs.getDate("sale_date").toLocalDate(), rs.getFloat("total_sales"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return salesData;
    }

    // DAILY
    private Map<LocalDate, Map<Product, Integer>> getSalesData(LocalDate date) {
        Map<LocalDate, Map<Product, Integer>> salesData = new HashMap<>();

        String salesQuery = "SELECT sales.sale_date, sales_product.product_id, sales_product.sale_quantity " +
                "FROM sales " +
                "JOIN sales_product ON sales.sale_id = sales_product.sales_id " +
                "WHERE DATE(sales.sale_date) = ?";
        try (Connection c = SQLConnect.connectDb();
             PreparedStatement pstmt = c.prepareStatement(salesQuery)) {
            pstmt.setDate(1, java.sql.Date.valueOf(date));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Assume getProductById() is a method that retrieves a Product object given its ID
                    Product product = getProductById(rs.getInt("product_id"));
                    int quantity = rs.getInt("sale_quantity");

                    if (!salesData.containsKey(date)) {
                        salesData.put(date, new HashMap<>());
                    }
                    salesData.get(date).put(product, quantity);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return salesData;
    }

    // get the inventory data
    // Get the data from the SQL table of inventory.
    public static ObservableList<Inventory> getDataInventory() {
        // connect to the db.
        Connection conn = SQLConnect.connectDb();
        // initialize an arraylist to store the inventoryList.
        ObservableList<Inventory> inventoryList = FXCollections.observableArrayList();
        try {
            // query the db, joining the supplier table to get the supplier name.
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM `inventory` INNER JOIN `supplier` ON inventory.supplier_id = supplier.supplier_id");
            ResultSet rs = ps.executeQuery();

            // fill out the inventory arrayList while query is active.
            while (rs.next()) {
                inventoryList.add(new Inventory(Integer.parseInt(rs.getString("inventory_id")),
                        rs.getString("ingredient_name"),
                        Integer.parseInt(rs.getString("supplier_id")),
                        rs.getString("supplier_name"),
                        Integer.parseInt(rs.getString("inventory_quantity")),
                        rs.getDate("date_received").toLocalDate(),
                        rs.getString("inventory_category")));
            }
        } catch (Exception e) {

        }
        return inventoryList;
    }

    public String inventoryToString(int inventory_id, String ingredient_name, int inventory_quantity, LocalDate date_received, String inventory_category) {
        return "Inventory ID: " + inventory_id +
                ", Ingredient Name: " + ingredient_name +
                ", Inventory Quantity: " + inventory_quantity +
                ", Date Received: " + date_received +
                ", Inventory Category: " + inventory_category;
    }

    // GET THE PRODUCT INGREDIENTS
    private static ObservableList<Inventory> getProductIngredients(int productId) {
        Connection conn = SQLConnect.connectDb();
        String query = "SELECT inventory.* FROM inventory JOIN product_inventory ON inventory.inventory_id = product_inventory.inventory_id WHERE product_inventory.product_id = ?";
        ObservableList<Inventory> ingredients = FXCollections.observableArrayList();

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, productId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("inventory_id");
                String ingredientName = resultSet.getString("ingredient_name");
                int inventoryQuantity = resultSet.getInt("inventory_quantity");
                String inventoryCategory = resultSet.getString("inventory_category");
                LocalDate dateReceived = resultSet.getDate("date_received").toLocalDate();
                int supplierId = resultSet.getInt("supplier_id");
                String supplierName = findSupplierName(supplierId);

                Inventory inventory = new Inventory(id, ingredientName, supplierId, supplierName, inventoryQuantity, dateReceived, inventoryCategory);
                ingredients.add(inventory);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ingredients;
    }

    // GET THE QUANTITIES OF THE INGREDIENTS USED IN THE PRODUCT
    private static ObservableList<Integer> getProductQuantities(int productId) {
        Connection conn = SQLConnect.connectDb();
        String query = "SELECT amount_used FROM product_inventory WHERE product_id = ?";
        ObservableList<Integer> quantities = FXCollections.observableArrayList();

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, productId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int quantity = resultSet.getInt("amount_used");
                quantities.add(quantity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return quantities;
    }

    public static String getDailySalesDataAsString() {
        // Connect to the database
        Connection conn = SQLConnect.connectDb();

        StringBuilder result = new StringBuilder();

        try {
            String query = "SELECT sales.sale_date, sales_product.product_id, sales_product.quantity_sold, sales.sale_price FROM sales_product JOIN sales ON sales_product.sale_id = sales.sale_id WHERE sales.sale_date = CURDATE()";
            PreparedStatement ps = conn.prepareStatement(query);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Product product = getProductById(rs.getInt("sale_id"));  // Assuming getProductById is a method that retrieves a Product by id
                String saleDate = rs.getDate("sale_date").toString();
                int quantitySold = rs.getInt("quantity_sold");
                float salePrice = rs.getFloat("sale_price");

                // Append each sale data to the StringBuilder
                result.append("Date: ").append(saleDate)
                        .append(", Product: ").append(product.getProductName())  // Assuming Product has a method getProductName
                        .append(", Quantity Sold: ").append(quantitySold)
                        .append(", Sale Price: ").append(salePrice).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    // GETTING THE PRODUCT FROM THE DB BY THE NAME
    private static Product getProductById(int saleId) {
        Connection conn = SQLConnect.connectDb();
        String query = "SELECT * FROM product WHERE product_id = ?";
        Product product = null;

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, saleId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String productName = resultSet.getString("product_name");
                String productCategory = resultSet.getString("product_category");
                float productPrice = resultSet.getFloat("product_price");

                ObservableList<Inventory> ingredients = getProductIngredients(saleId);
                ObservableList<Integer> quantities = getProductQuantities(saleId);

                product = new Product(saleId, productCategory, productName, productPrice, ingredients, quantities);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return product;
    }

    // FIND THE NAME OF THE SUPPLIER BASED ON THE ID FOR THE FETCHING OF THE DATA
    public static String findSupplierName(int supplierId) throws SQLException {
        // FIND THE ASSIGNED SUPPLIER ID FOR THE ACCOMPANYING SUPPLIER NAME
        Connection c = SQLConnect.connectDb();
        String findSupplierId = "SELECT supplier_name FROM `supplier` WHERE supplier_id = ?"; // QUERY TO SEARCH FOR ID BASED ON NAME
        PreparedStatement findingId = c.prepareStatement(findSupplierId);
        String supplierName = null;
        try {
            findingId.setString(1, String.valueOf(supplierId)); // SET ? IN QUERY TO NAME
            ResultSet RSSupId = findingId.executeQuery(); // EXECUTE THE QUERY ITSELF
            if(RSSupId.next()){
                supplierName = RSSupId.getString("supplier_name");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return supplierName;
    }

    // ALERT MESSAGE
    private void showAlert(String title, String message) {
        // Create an Alert dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        // Show the dialog
        alert.showAndWait();
    }


    //CHANGE LANGUAGE

    // get current stage
    private Stage getStage() {
        return (Stage) timeCombo.getScene().getWindow();
    }

    // change the language
    @FXML
    private void handleLanguageToggle() {
        try {
            ChangeLanguage.toggleLanguage(getStage(), "ReportsPage.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
