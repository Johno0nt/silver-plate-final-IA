package com.silverplate.silverplate.Controller;

import com.silverplate.silverplate.ChangeLanguage;
import com.silverplate.silverplate.Model.Inventory;
import com.silverplate.silverplate.Model.Product;
import com.silverplate.silverplate.SQLConnect;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;
import org.controlsfx.control.SearchableComboBox;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

import static java.lang.Float.parseFloat;

public class AddSalePage implements Initializable {
    @FXML
    private DatePicker datePicker;
    @FXML
    private SearchableComboBox<Product> productsBox;
    @FXML
    private ComboBox<Integer> quantityBox;
    @FXML
    private TableView<Pair<Product, Integer>> productsTable;
    @FXML
    private TableColumn<Pair<Product, Integer>, String> col_product;
    @FXML
    private TableColumn<Pair<Product, Integer>, Integer> col_quantity;
    @FXML
    private Label totalLabel;

    // ObservableList to hold pairs of Product and Quantity
    private ObservableList<Pair<Product, Integer>> productQuantityList = FXCollections.observableArrayList();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Placing the inventory quantities into the comboBox
        quantityBox.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10));

        // Set up the cell factory for the inventoryComboBox
        productsBox.setCellFactory(new Callback<ListView<Product>, ListCell<Product>>() {
            @Override
            public ListCell<Product> call(ListView<Product> listView) {
                return new ListCell<Product>() {
                    @Override
                    protected void updateItem(Product product, boolean empty) {
                        super.updateItem(product, empty);

                        if (product == null || empty) {
                            setText(null);
                        } else {
                            setText(product.getProductName());
                        }
                    }
                };
            }
        });

        // The same cell factory is used for the ComboBox's button when an item is selected
        productsBox.setButtonCell((ListCell) productsBox.getCellFactory().call(null));

        // Set up the columns for the productTable
        loadProductNames();

        // Define the cell value factories
        col_product.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getKey().getProductName())
        );
        col_quantity.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getValue())
        );
    }

    // LOAD THE PRODUCT NAMES
    private void loadProductNames() {
        // Initialize an ObservableList to store the Inventory objects
        ObservableList<Product> products = FXCollections.observableArrayList();

        // Connect to the database and fetch the inventory items
        Connection c = SQLConnect.connectDb();
        String sql = "SELECT * FROM product";

        try (
                PreparedStatement stmt = c.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // INSERT THE CODE HERE TO ADD THE PRODUCT TO THE LIST
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                String productCategory = rs.getString("product_category");
                float productPrice = rs.getFloat("product_price");

                // Fetch the product ingredients and quantities from the database
                ObservableList<Inventory> ingredients = getProductIngredients(productId);
                ObservableList<Integer> ingredientQuantities = getProductQuantities(productId);

                // Create a new Product object with these fields
                Product product = new Product(productId, productCategory, productName, productPrice, ingredients, ingredientQuantities);
                products.add(product);
            }

            productsBox.setItems(products);
        } catch (SQLException e) {
            // Handle any SQL exceptions
            e.printStackTrace();
        }
    }

    // GET THE PRODUCT INGREDIENTS
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

    // GET THE QUANTITIES OF THE INGREDIENTS USED IN THE PRODUCT
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

    private float totalSales = 0;

    public void addProductToSale() {
        Product selectedProduct = productsBox.getValue();
        Integer selectedQuantity = quantityBox.getValue();

        // GIVEN THE TWO VALUES, ADD THEM TO THE TABLE IN A PAIR TO ENSURE THAT THEY ARE MAPPED TO EACH OTHER
        if (selectedProduct != null && selectedQuantity != null) {
            productQuantityList.add(new Pair<>(selectedProduct, selectedQuantity));
            productsTable.setItems(productQuantityList);
        }

        // Calculate total price of all products currently in the list
        float totalSales = 0f;
        for (Pair<Product, Integer> pair : productQuantityList) {
            totalSales += pair.getKey().getProductPrice() * pair.getValue();
        }

        totalLabel.setText("$" + totalSales);
    }


    // CALCULATE THE SALE PRICE OF THE PRODUCTS IN THE TABLE
    public float calculateSalePrice() {
        float totalSalePrice = 0;
        for (Pair<Product, Integer> pair : productQuantityList) {
            totalSalePrice += pair.getKey().getProductPrice() * pair.getValue();
        }
        return totalSalePrice;
    }

    // REMOVE THE PRODUCT FROM THE TABLE
    public void removeProduct() {
        // Get items currently in the TableView
        ObservableList<Pair<Product, Integer>> items = productsTable.getItems();

        // Make sure there's at least one item to remove
        if (!items.isEmpty()) {
            // Get the last item
            items.remove(items.size() - 1);

            // Calculate total price of all products currently in the list
            float totalSales = 0f;
            for (Pair<Product, Integer> pair : items) {
                totalSales += pair.getKey().getProductPrice() * pair.getValue();
            }

            totalLabel.setText("$" + totalSales);
        }
    }

    public void addSaleToDatabase() {
        // Get sale date
        LocalDate saleDate = datePicker.getValue();

        // Calculate sale price
        float salePrice = calculateSalePrice();

        Connection conn = SQLConnect.connectDb();
        String insertSaleQuery = "INSERT INTO sales (sale_date, sale_price) VALUES (?, ?)";
        try {
            // Insert sale into sales table
            PreparedStatement salesPreparedStatement = conn.prepareStatement(insertSaleQuery, Statement.RETURN_GENERATED_KEYS);
            salesPreparedStatement.setDate(1, java.sql.Date.valueOf(saleDate));
            salesPreparedStatement.setFloat(2, salePrice);
            int affectedRows = salesPreparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating sale failed, no rows affected.");
            }

            try (ResultSet generatedKeys = salesPreparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int saleId = generatedKeys.getInt(1);

                    // Insert products into sales_product table
                    String insertSalesProductQuery = "INSERT INTO sales_product (sales_id, product_id, sale_quantity) VALUES (?, ?, ?)";
                    for (Pair<Product, Integer> pair : productQuantityList) {
                        PreparedStatement salesProductPreparedStatement = conn.prepareStatement(insertSalesProductQuery);
                        salesProductPreparedStatement.setInt(1, saleId);
                        salesProductPreparedStatement.setInt(2, pair.getKey().getProductId());
                        salesProductPreparedStatement.setInt(3, pair.getValue());
                        salesProductPreparedStatement.executeUpdate();
                    }
                } else {
                    throw new SQLException("Creating sale failed, no ID obtained.");
                }
                // Clear the TableView and text fields after adding the product
                productsTable.getItems().clear();
                quantityBox.getEditor().clear();
                productsBox.getEditor().clear();
                datePicker.getEditor().clear();
                showAlert("Success", "Product added successfully.");
                totalSales = 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        return (Stage) quantityBox.getScene().getWindow();
    }

    // change the language
    @FXML
    private void handleLanguageToggle() {
        try {
            ChangeLanguage.toggleLanguage(getStage(), "AddSalePage.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
