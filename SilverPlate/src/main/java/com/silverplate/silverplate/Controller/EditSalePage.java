package com.silverplate.silverplate.Controller;

import com.silverplate.silverplate.ChangeLanguage;
import com.silverplate.silverplate.Model.Inventory;
import com.silverplate.silverplate.Model.Product;
import com.silverplate.silverplate.Model.Sales;
import com.silverplate.silverplate.SQLConnect;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;
import org.controlsfx.control.SearchableComboBox;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import static java.lang.Float.parseFloat;

public class EditSalePage implements Initializable {
    @FXML
    private SearchableComboBox<Product> productsBox;
    @FXML
    private ComboBox<Integer> quantityBox;
    @FXML
    private TableView<Sales> salesTable;
    @FXML
    private TableView<Pair<Product, Integer>> productsTable;
    @FXML
    private TableColumn<Pair<Product, Integer>, String> col_product;
    @FXML
    private TableColumn<Pair<Product, Integer>, Integer> col_quantity;
    @FXML
    private Label totalLabel;
    @FXML
    private TableColumn<Sales, Integer> col_salesId;
    @FXML
    private TableColumn<Sales, String> col_saleProduct;
    @FXML
    private TableColumn<Sales, Float> col_saleTotal;
    @FXML
    private DatePicker datePicker;
    @FXML
    private DatePicker saleDatePicker;
    @FXML
    private TextField idField;

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

    // ObservableList to hold pairs of Product and Quantity
    private ObservableList<Pair<Product, Integer>> productQuantityList = FXCollections.observableArrayList();

    // SalesList to store the sales
    private ObservableList<Sales> salesList = FXCollections.observableArrayList();


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

    /*private void updateProductTable() {
        // Retrieve and display the products for the selected sale
        // GET THE PRODUCTS AND THEIR QUANTITIES
        ObservableList<Product> products = sales.getProducts();
        ObservableList<Integer> quantities = sales.getQuantities();

        // Create a new ObservableList to hold the pairs
        ObservableList<Pair<Product, Integer>> productAndQuantityList = FXCollections.observableArrayList();

        // Loop through the products and quantities, pair them, and add to the list
        for (int i = 0; i < products.size(); i++) {
            Pair<Product, Integer> productAndQuantity = new Pair<>(products.get(i), quantities.get(i));
            productAndQuantityList.add(productAndQuantity);
        }

        productsTable.setItems(productAndQuantityList);
    }
*/
    // Function to delete a sale from the database
    public void deleteSale() {
        Connection conn = SQLConnect.connectDb();

        try {
            // Start transaction
            conn.setAutoCommit(false);

            String deleteSaleProductSQL = "DELETE FROM sales_product WHERE sales_id = ?";
            PreparedStatement deleteSaleProductStmt = conn.prepareStatement(deleteSaleProductSQL);
            deleteSaleProductStmt.setInt(1, Integer.parseInt(idField.getText()));
            deleteSaleProductStmt.executeUpdate();

            String deleteSaleSQL = "DELETE FROM sales WHERE sale_id = ?";
            PreparedStatement deleteSaleStmt = conn.prepareStatement(deleteSaleSQL);
            deleteSaleStmt.setInt(1, Integer.parseInt(idField.getText()));
            deleteSaleStmt.executeUpdate();

            // Commit transaction
            conn.commit();
            // reload the table
            loadTable();
            // clear the products table
            productsTable.getItems().clear();
            datePicker.getEditor().clear();

            showAlert("Deleted", "Sale deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                // If there was an error, roll back changes
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Function to update a sale in the database
    public void updateSaleInDatabase(int saleId, LocalDate saleDate, float salePrice) {

        Connection c = SQLConnect.connectDb();

        // convert the price to a float
        String priceString = totalLabel.getText();
        // get rid of the dollar sign
        priceString = priceString.replace("$", "");
        // make it a float value
        float price = Float.parseFloat(priceString);

        try {
            // First insert the sale into the sales table
            String insertSaleQuery = "UPDATE sales SET sale_date= ?, sale_price= ? WHERE sale_id= ?";

            PreparedStatement insertSaleStmt = c.prepareStatement(insertSaleQuery);
            insertSaleStmt.setInt(1, saleId);
            insertSaleStmt.setDate(2, java.sql.Date.valueOf(saleDate));
            insertSaleStmt.setFloat(3, salePrice);

            insertSaleStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void editSale() {
        // get the sale id
        int saleId = Integer.parseInt(idField.getText());
        LocalDate saleDate = datePicker.getValue();
        float salePrice = Float.parseFloat(totalLabel.getText().replace("$", ""));

        // Update the sale in the database
        updateSaleInDatabase(saleId, saleDate, salePrice);

        // Delete the old products in the sale
        deleteProductSalesFromDB(saleId);

        // Add updated products of the sale to database
        ObservableList<Pair<Product, Integer>> tableData = productsTable.getItems();
        for (Pair<Product, Integer> row : tableData) {
            int productId = row.getKey().getProductId();
            int quantity = row.getValue();
            addProductToSaleInDB(productId, saleId, quantity);
        }
        showAlert("Success", "Sale updated successfully!");
        loadTable();
    }

    // INDIVIDUALLY ADD PRODUCTS TO SALES
    public static void addProductToSaleInDB(int productId, int saleId, int quantity) {
        Connection conn = SQLConnect.connectDb();
        // QUERY
        String query = "INSERT INTO sales_product (product_id, sales_id, sale_quantity) VALUES (?, ?, ?)";
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, productId);
            stmt.setInt(2, saleId);
            stmt.setInt(3, quantity);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE ALL CURRENT SALES FROM DB
    public static void deleteProductSalesFromDB(int sales_id) {
        Connection conn = SQLConnect.connectDb();
        // SQL query to delete all ingredients of a product from 'product_inventory' table.
        String query = "DELETE FROM sales_product WHERE sales_id = ?";

        try {
            // Prepare the statement.
            PreparedStatement stmt = conn.prepareStatement(query);

            // Set the product ID.
            stmt.setInt(1, sales_id);

            // Execute the query.
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<Pair<Product, Integer>> getUpdatedSaleData() {
        // Initialize a new ObservableList to store the updated data
        ObservableList<Pair<Product, Integer>> updatedSaleData = FXCollections.observableArrayList();

        // Get the updated data from the user interface
        // This will depend on your specific setup. The following is just an example.
        // Let's assume 'productsTable' is your TableView element which contains the updated data
        for (Pair<Product, Integer> pair : productsTable.getItems()) {
            updatedSaleData.add(pair);
        }

        return updatedSaleData;
    }

    public void addProductToSale() {
        Product selectedProduct = productsBox.getValue();
        Integer selectedQuantity = quantityBox.getValue();

        // GIVEN THE TWO VALUES, ADD THEM TO THE TABLE IN A PAIR TO ENSURE THAT THEY ARE MAPPED TO EACH OTHER
        if (selectedProduct != null && selectedQuantity != null) {
            Pair<Product, Integer> newProductPair = new Pair<>(selectedProduct, selectedQuantity);
            productsTable.getItems().add(newProductPair);
        }

        // Calculate total price of all products currently in the table
        float totalSales = 0f;
        for (Pair<Product, Integer> pair : productsTable.getItems()) {
            totalSales += pair.getKey().getProductPrice() * pair.getValue();
        }

        totalLabel.setText("$" + totalSales);
    }

    public void loadTable() {
        // Get the data from the DatePicker
        LocalDate saleDate = saleDatePicker.getValue();

        if(saleDate == null) {
            showAlert("Error", "Please select a date to view sales for.");
            return;
        }

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
        saleDatePicker.getEditor().clear();
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

    // ALERT MESSAGE
    private void showAlert(String title, String message) {
        // Create an Alert dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        // Show the dialog
        alert.showAndWait();
    }

    // CHOOSE THE SALE OBJECT
    @FXML
    private void getSelected() {
        int index = salesTable.getSelectionModel().getSelectedIndex();

        if (index <= -1) { // if index is -1, nothing has been selected.
            return;
        }

        // set text from table-view to text-fields
        idField.setText(String.valueOf(col_salesId.getCellData(index)));
        datePicker.setValue(saleDatePicker.getValue());

        salesList = getSalesData(saleDatePicker.getValue());

        // Get the selected Sale object
        Sales selectedSale = salesList.get(index);

        // Retrieve and display the products for the selected sale
        ObservableList<Pair<Product, Integer>> productQuantityPairs = FXCollections.observableArrayList();

        for (int i = 0; i < selectedSale.getProducts().size(); i++) {
            Product product = selectedSale.getProducts().get(i);
            Integer quantity = selectedSale.getQuantities().get(i);
            productQuantityPairs.add(new Pair<>(product, quantity));
        }

        // update the product and quantities table with the selected sale.
        productsTable.setItems(productQuantityPairs);
    }

    //CHANGE LANGUAGE

    // get current stage
    private Stage getStage() {
        return (Stage) productsTable.getScene().getWindow();
    }

    // change the language
    @FXML
    private void handleLanguageToggle() {
        try {
            ChangeLanguage.toggleLanguage(getStage(), "EditSalePage.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
