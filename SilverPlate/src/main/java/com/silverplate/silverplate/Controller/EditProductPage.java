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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class EditProductPage implements Initializable {
    @FXML
    private TextField productNameField;

    @FXML
    private TextField priceTextField;

    @FXML
    private ComboBox<String> categoryBox;

    @FXML
    private SearchableComboBox<Inventory> ingredientsBox;

    @FXML
    private ComboBox<Integer> quantityBox;

    @FXML
    private TableView<javafx.util.Pair<Inventory, Integer>> ingredientsTable;

    @FXML
    private TableColumn<javafx.util.Pair<Inventory, Integer>, String> col_ingredient;

    @FXML
    private TableColumn<Pair<Inventory, Integer>, Integer> col_quantity;

    @FXML
    private SearchableComboBox<String> productSearchableComboBox;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadProductNames(); // load product names into the combo box

        // Add a listener to the combo box
        productSearchableComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                loadProductDetails(String.valueOf(newValue));
            }
        });

        // Placing the product categories into the comboBox
        categoryBox.setItems(FXCollections.observableArrayList("Meal", "Meal Deal", "Drink"));

        // Placing the inventory quantities into the comboBox
        quantityBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

        // Set up the cell factory for the inventoryComboBox
        ingredientsBox.setCellFactory(new Callback<ListView<Inventory>, ListCell<Inventory>>() {
            @Override
            public ListCell<Inventory> call(ListView<Inventory> listView) {
                return new ListCell<Inventory>() {
                    @Override
                    protected void updateItem(Inventory inventory, boolean empty) {
                        super.updateItem(inventory, empty);

                        if (inventory == null || empty) {
                            setText(null);
                        } else {
                            setText(inventory.getInventoryName());
                        }
                    }
                };
            }
        });

        // The same cell factory is used for the ComboBox's button when an item is selected
        ingredientsBox.setButtonCell(ingredientsBox.getCellFactory().call(null));

        // Load inventory items into the ComboBox
        loadInventoryItems();

        // Set up the columns for the ingredientsTable
        // Define the cell value factories
        col_ingredient.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getKey().getInventoryName())
        );
        col_quantity.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getValue())
        );
    }

    // LOAD THE INVENTORY
    private void loadInventoryItems() {
        // Initialize an ObservableList to store the Inventory objects
        ObservableList<Inventory> inventoryItems = FXCollections.observableArrayList();

        // Connect to the database and fetch the inventory items
        Connection c = SQLConnect.connectDb();
        String sql = "SELECT * FROM inventory";

        try (
                PreparedStatement stmt = c.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int inventoryId = rs.getInt("inventory_id");
                String inventoryName = rs.getString("ingredient_name");
                int supplierId = rs.getInt("supplier_id");
                String supplierName = findSupplierName(supplierId);
                int inventoryQuantity = rs.getInt("inventory_quantity");
                LocalDate dateReceived = rs.getDate("date_received").toLocalDate();
                String inventoryCategory = rs.getString("inventory_category");

                Inventory inventory = new Inventory(inventoryId, inventoryName, supplierId, supplierName, inventoryQuantity, dateReceived, inventoryCategory);
                inventoryItems.add(inventory);
            }

            ingredientsBox.setItems(inventoryItems);
        } catch (SQLException e) {
            // Handle any SQL exceptions
            e.printStackTrace();
        }
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


    private void loadProductNames() {
        List<String> productNames = getProductNamesFromDB(); // assuming you have this method to get product names from database
        productSearchableComboBox.getItems().setAll(productNames);
    }

    private void loadProductDetails(String productName) {
        Product product = getProductByNameFromDB(productName); // assuming you have this method to get product by name from database

        productNameField.setText(product.getProductName());
        priceTextField.setText(String.valueOf(product.getProductPrice()));
        categoryBox.setValue(String.valueOf(product.getProductCategory()));

        ObservableList<Pair<Inventory, Integer>> ingredientData = FXCollections.observableArrayList();

        for (int i = 0; i < product.getIngredients().size(); i++) {
            ingredientData.add(new Pair<>(product.getIngredients().get(i), product.getIngredientQuantity().get(i)));
        }

        ingredientsTable.setItems(ingredientData);
    }

    @FXML
    private void editProduct() throws SQLException {
        // Get product name, category, and price from text fields
        String productName = productNameField.getText();
        String productCategory = categoryBox.getValue();
        float productPrice = Float.parseFloat(priceTextField.getText());

        // Get current selected product ID
        int productId = findProductId(productName);

        // Update product details in database
        updateProductInDB(productId, productName, productCategory, productPrice);

        // Delete old ingredients of the product from database
        deleteProductIngredientsFromDB(productId);

        // Add updated ingredients of the product to database
        ObservableList<Pair<Inventory, Integer>> tableData = ingredientsTable.getItems();
        for (Pair<Inventory, Integer> row : tableData) {
            int inventoryId = row.getKey().getInventoryId();
            int quantity = row.getValue();
            addIngredientToProductInDB(productId, inventoryId, quantity);
        }

        showAlert("Product updated successfully!");
    }

    // find the product id
    public int findProductId(String productName) throws SQLException {
        // FIND THE ASSIGNED Product ID FOR THE ACCOMPANYING Product NAME
        Connection c = SQLConnect.connectDb();
        String findProductId = "SELECT product_id FROM `product` WHERE product_name = ?"; // QUERY TO SEARCH FOR ID BASED ON NAME
        PreparedStatement findingId = c.prepareStatement(findProductId);
        int supplierId = -1;
        try {
            findingId.setString(1, productName); // SET ? IN QUERY TO NAME
            ResultSet RSProdId = findingId.executeQuery(); // EXECUTE THE QUERY ITSELF
            if(RSProdId.next()){
                supplierId = RSProdId.getInt("product_id");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return supplierId;
    }

    // INDIVIDUALLY ADD INGREDIENTS
    public static void addIngredientToProductInDB(int productId, int inventoryId, int quantity) {
        Connection conn = SQLConnect.connectDb();
        // QUERY
        String query = "INSERT INTO product_inventory (product_id, inventory_id, amount_used) VALUES (?, ?, ?)";
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, productId);
            stmt.setInt(2, inventoryId);
            stmt.setInt(3, quantity);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE ALL CURRENT INGREDIENTS FROM DB
    public static void deleteProductIngredientsFromDB(int productId) {
        Connection conn = SQLConnect.connectDb();
        // SQL query to delete all ingredients of a product from 'product_inventory' table.
        String query = "DELETE FROM product_inventory WHERE product_id = ?";

        try {
            // Prepare the statement.
            PreparedStatement stmt = conn.prepareStatement(query);

            // Set the product ID.
            stmt.setInt(1, productId);

            // Execute the query.
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE THE PRODUCT FROM THE DB
    @FXML
    private void deleteProduct() {
        String selectedProductName = String.valueOf(productSearchableComboBox.getValue());
        if (selectedProductName != null) {
            deleteProductFromDB(selectedProductName); // delete the product from the database
            loadProductNames(); // refresh the combo box
            showAlert("Product deleted successfully!");

            // Clear the TableView and text fields after adding the product
            ingredientsTable.getItems().clear();
            productNameField.clear();
            categoryBox.setValue("");
            priceTextField.clear();
        } else {
            showAlert("No product selected!");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public ObservableList<String> getProductNamesFromDB() {
        // get the product names from the database
        ObservableList<String> productNames = FXCollections.observableArrayList();
        Connection conn = SQLConnect.connectDb();
        // QUERY
        String sql = "SELECT product_name FROM product";

        try {PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                productNames.add(rs.getString("product_name"));
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return productNames;
    }

    // UPDATING THE PRODUCT
    public static void updateProductInDB(int productId, String productName, String productCategory, float productPrice) {
        Connection conn = SQLConnect.connectDb();

        // QUERY
        String query = "UPDATE product SET product_name = ?, product_category = ?, product_price = ? WHERE product_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Set the corresponding parameters in the prepared statement
            pstmt.setString(1, productName);
            pstmt.setString(2, productCategory);
            pstmt.setFloat(3, productPrice);
            pstmt.setInt(4, productId);

            pstmt.executeUpdate();  // Execute the SQL statement

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // GETTING THE PRODUCT FROM THE DB BY THE NAME
    private Product getProductByNameFromDB(String name) {
        Connection conn = SQLConnect.connectDb();

        // the SQL query to fetch the product
        String sql = "SELECT * FROM product WHERE product_name = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int productId = rs.getInt("product_id");
                ObservableList<Inventory> ingredients = FXCollections.observableArrayList();
                ObservableList<Integer> quantities = FXCollections.observableArrayList();

                // second SQL query to fetch the product's ingredients and quantities
                sql = "SELECT * FROM product_inventory WHERE product_id = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, productId);
                ResultSet rs2 = pstmt.executeQuery();

                while(rs2.next()) {
                    int inventoryId = rs2.getInt("inventory_id");
                    int quantity = rs2.getInt("amount_used");

                    // query to fetch the inventory item by its id
                    String sql2 = "SELECT * FROM inventory WHERE inventory_id = ?";
                    PreparedStatement pstmt2 = conn.prepareStatement(sql2);
                    pstmt2.setInt(1, inventoryId);
                    ResultSet rs3 = pstmt2.executeQuery();

                    if(rs3.next()) {
                        int supplierId = rs3.getInt("supplier_id");
                        Inventory inventory = new Inventory(rs3.getInt("inventory_id"), rs3.getString("ingredient_name"), supplierId, findSupplierName(supplierId), rs3.getInt("inventory_quantity"), rs3.getDate("date_received").toLocalDate(), rs3.getString("inventory_category"));
                        ingredients.add(inventory);
                    }
                    quantities.add(quantity);
                }

                return new Product(productId, rs.getString("product_category"), rs.getString("product_name"), rs.getFloat("product_price"), ingredients, quantities);
            } else {
                System.out.println("No product found with name: " + name);
            }
        } catch (SQLException e) {
            System.out.println("Error querying for product: " + name);
            e.printStackTrace();
        }

        return null;
    }

    private void deleteProductFromDB(String name) {
        Connection conn = SQLConnect.connectDb();
        try {
            // first, delete from the product_inventory table
            String sql = "DELETE FROM product_inventory WHERE product_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, getProductByNameFromDB(name).getProductId());
            pstmt.executeUpdate();

            // then, delete from the product table
            sql = "DELETE FROM product WHERE product_name = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addIngredient() {
        // Get the selected inventory item and quantity
        Inventory selectedInventory = ingredientsBox.getValue();
        Integer selectedQuantity = quantityBox.getValue();

        if (selectedInventory != null && selectedQuantity != null) {
            // Create a new pair with the selected inventory item and quantity
            Pair<Inventory, Integer> ingredientPair = new Pair<>(selectedInventory, selectedQuantity);

            // Get the items currently in the TableView
            ObservableList<Pair<Inventory, Integer>> items = ingredientsTable.getItems();

            // Add the new pair to the list
            items.add(ingredientPair);
        }
    }

    // REMOVE THE INGREDIENT FROM THE TABLE
    public void removeIngredient() {
        // Get items currently in the TableView
        ObservableList<Pair<Inventory, Integer>> items = ingredientsTable.getItems();

        // Make sure there's at least one item to remove
        if (!items.isEmpty()) {
            // Remove the last item
            items.remove(items.size() - 1);
        }
    }

    //CHANGE LANGUAGE

    // get current stage
    private Stage getStage() {
        return (Stage) ingredientsTable.getScene().getWindow();
    }

    // change the language
    @FXML
    private void handleLanguageToggle() {
        try {
            ChangeLanguage.toggleLanguage(getStage(), "EditProductPage.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
