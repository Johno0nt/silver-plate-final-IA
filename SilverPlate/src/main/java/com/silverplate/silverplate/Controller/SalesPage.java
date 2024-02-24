package com.silverplate.silverplate.Controller;

import com.silverplate.silverplate.ChangeLanguage;
import com.silverplate.silverplate.Main;
import com.silverplate.silverplate.Model.Inventory;
import com.silverplate.silverplate.Model.Product;
import com.silverplate.silverplate.Model.Sales;
import com.silverplate.silverplate.SQLConnect;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SalesPage {
    @FXML
    private TableView<Sales> salesTable;
    @FXML
    private TableColumn<Sales, Integer> col_salesId;
    @FXML
    private TableColumn<Sales, String> col_saleProduct;
    @FXML
    private TableColumn<Sales, Float> col_saleTotal;
    @FXML
    private DatePicker saleDatePicker;
    @FXML
            private Label totalLabel;

    ResourceBundle englishBundle = ResourceBundle.getBundle("Text_en_AU");

    public void loadTable() {
        // Get the data from the DatePicker
        LocalDate saleDate = saleDatePicker.getValue();

        // Clear the existing content of the table
        salesTable.getItems().clear();



        // Set the cell value factories for the TableView columns
        col_salesId.setCellValueFactory(new PropertyValueFactory<Sales, Integer>("saleId"));
        col_saleTotal.setCellValueFactory(new PropertyValueFactory<Sales, Float>("salePrice"));

        col_saleProduct.setCellValueFactory(cellData -> {
            Sales sale = cellData.getValue();
            List<String> productDisplay = new ArrayList<>();
            for (int i = 0; i < sale.getProducts().size(); i++) {
                String display = sale.getQuantities().get(i) + "x " + sale.getProducts().get(i).getProductName();
                productDisplay.add(display);
            }
            return new SimpleStringProperty(String.join(", ", productDisplay));
        });

        // Get the sales data from the database
        ObservableList<Sales> salesData = getSalesData(saleDate);
        ObservableList<Sales> products = FXCollections.observableArrayList(salesData);
        salesTable.setItems(products);
        updateTotalSalesLabel(saleDate);
    }

    private ObservableList getSalesData(LocalDate saleDate) {
        ObservableList<Sales> salesList = FXCollections.observableArrayList();

        Connection conn = SQLConnect.connectDb();

        String salesQuery = "SELECT * FROM sales WHERE sale_date = ?";
        try {
            PreparedStatement salesPreparedStatement = conn.prepareStatement(salesQuery);
            salesPreparedStatement.setDate(1, java.sql.Date.valueOf(saleDate));
            ResultSet salesResultSet = salesPreparedStatement.executeQuery();

            while (salesResultSet.next()) {
                int saleId = salesResultSet.getInt("sale_id");
                float salePrice = salesResultSet.getFloat("sale_price");
                // Fetch sales products and quantities separately
                ObservableList<Product> salesProductList = getSalesProductsFromDB(saleId);
                ObservableList<Integer> salesQuantitiesList = getSalesQuantitiesFromDB(saleId);

                salesList.add(new Sales(saleId, saleDate, salePrice, salesProductList, salesQuantitiesList));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salesList;
    }

    // GETTING THE PRODUCT FROM THE DB BY THE NAME
    private Product getProductById(int id) {
        Connection conn = SQLConnect.connectDb();
        String query = "SELECT * FROM product WHERE product_id = ?";
        Product product = null;

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String productName = resultSet.getString("product_name");
                String productCategory = resultSet.getString("product_category");
                float productPrice = resultSet.getFloat("product_price");

                ObservableList<Inventory> ingredients = getProductIngredients(id);
                ObservableList<Integer> quantities = getProductQuantities(id);

                product = new Product(id, productCategory, productName, productPrice, ingredients, quantities);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return product;
    }

    private ObservableList<Inventory> getProductIngredients(int productId) {
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

    // FIND THE SUPPLIER BY THE ID
    public String findSupplierName(int supplierId) throws SQLException {
        // FIND THE ASSIGNED SUPPLIER ID FOR THE ACCOMPANYING SUPPLIER NAME
        Connection c = SQLConnect.connectDb();
        String findSupplierId = "SELECT supplier_name FROM `supplier` WHERE supplier_id = ?"; // QUERY TO SEARCH FOR ID BASED ON NAME
        PreparedStatement findingId = c.prepareStatement(findSupplierId);
        String supplierName = null;
        try {
            findingId.setString(1, String.valueOf(supplierId)); // SET ? IN QUERY TO NAME
            ResultSet RSSupId = findingId.executeQuery(); // EXECUTE THE QUERY ITSELF
            if (RSSupId.next()) {
                supplierName = RSSupId.getString("supplier_name");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return supplierName;
    }

    // Method to fetch products related to the sale
    private ObservableList<Product> getSalesProductsFromDB(int saleId) {
        ObservableList<Product> productList = FXCollections.observableArrayList();

        String query = "SELECT product_id FROM sales_product WHERE sales_id = ?";
        try (Connection conn = SQLConnect.connectDb();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {

            preparedStatement.setInt(1, saleId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int productId = resultSet.getInt("product_id");
                Product product = getProductById(productId); // This method should return a Product object from the DB
                productList.add(product);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productList;
    }

    // Method to fetch quantities related to the sale
    private ObservableList<Integer> getSalesQuantitiesFromDB(int saleId) {
        ObservableList<Integer> quantityList = FXCollections.observableArrayList();

        String query = "SELECT sale_quantity FROM sales_product WHERE sales_id = ?";
        try (Connection conn = SQLConnect.connectDb();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {

            preparedStatement.setInt(1, saleId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int quantity = resultSet.getInt("sale_quantity");
                quantityList.add(quantity);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return quantityList;
    }

    private ObservableList<Integer> getProductQuantities(int productId) {
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

    // go to the sales graph page.
    public void showSalesGraphPage() throws IOException {
        Stage addProductStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("WeekSalesGraphPage.fxml"));
        fxmlLoader.setResources(englishBundle);
        Scene scene = new Scene(fxmlLoader.load());
        addProductStage.setTitle("Sales Graph");
        addProductStage.setScene(scene);
        addProductStage.show();
    }

    // go to the add sales page.
    public void showAddSalesPage() throws IOException {
        Stage addProductStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("AddSalePage.fxml"));
        fxmlLoader.setResources(englishBundle);
        Scene scene = new Scene(fxmlLoader.load());
        addProductStage.setTitle("Add Sales");
        addProductStage.setScene(scene);
        addProductStage.show();
    }

    // go to the edit sales page.
    public void showEditSalesPage() throws IOException {
        Stage addProductStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("EditSalePage.fxml"));
        fxmlLoader.setResources(englishBundle);
        Scene scene = new Scene(fxmlLoader.load());
        addProductStage.setTitle("Edit Sales");
        addProductStage.setScene(scene);
        addProductStage.show();
    }

    public void updateTotalSalesLabel(LocalDate date) {
        Connection conn = SQLConnect.connectDb();
        String salesQuery = "SELECT SUM(sale_price) as total_sales FROM sales WHERE sale_date = ?";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(salesQuery);
            preparedStatement.setDate(1, java.sql.Date.valueOf(date));
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                float totalSales = resultSet.getFloat("total_sales");
                totalLabel.setText("$" + totalSales);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //CHANGE LANGUAGE

    // get current stage
    private Stage getStage() {
        return (Stage) totalLabel.getScene().getWindow();
    }

    // get ResourceBundle
    public ResourceBundle getResourceBundle() {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("SalesPage.fxml");
        return resourceBundle;
    }

    // change the language
    @FXML
    private void handleLanguageToggle() {
        try {
            ChangeLanguage.toggleLanguage(getStage(), "SalesPage.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
