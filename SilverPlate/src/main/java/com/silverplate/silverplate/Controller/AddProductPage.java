package com.silverplate.silverplate.Controller;

import com.silverplate.silverplate.ChangeLanguage;
import com.silverplate.silverplate.Model.Inventory;
import com.silverplate.silverplate.Model.Staff;
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

public class AddProductPage implements Initializable {

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
    private TableView<Pair<Inventory,Integer>> ingredientsTable;

    @FXML
    private TableColumn<Pair<Inventory, Integer>, String> col_ingredient;

    @FXML
    private TableColumn<Pair<Inventory, Integer>, Integer> col_quantity;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Placing the product categories into the comboBox
        categoryBox.setItems(FXCollections.observableArrayList("Meal","Meal Deal", "Drink"));

        // Placing the inventory quantities into the comboBox
        quantityBox.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10));

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

    // connect to DB and inventory to place into searchable combo box.
    public static ObservableList<String> getInventoryNames() {
        // connect to the db.
        Connection conn = SQLConnect.connectDb();
        // initialize an arraylist to store the inventory.
        ObservableList<String> ingredientList = FXCollections.observableArrayList();
        try {
            // query the db.
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM `inventory`");
            ResultSet rs = ps.executeQuery();

            // fill out the ingredients arrayList while query is active.
            while (rs.next()) {
                ingredientList.add(rs.getString("ingredient_name"));
            }
        } catch (Exception e) {

        }
        return ingredientList;
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

    // ADD PRODUCT HANDLER
    private int addProduct(String name, String category, float price) {
        Connection conn = SQLConnect.connectDb();
        // SQL query to insert a new product into the 'product' table
        String sql = "INSERT INTO product (product_name, product_category, product_price) VALUES (?, ?, ?)";

        try {
            // Prepare the SQL query
            PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            // Set the parameters in the SQL query
            statement.setString(1, name);
            statement.setString(2, category);
            statement.setFloat(3, price);
            // Execute the SQL query
            statement.executeUpdate();

            // Retrieve the ID of the newly inserted product
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        } catch (SQLException e) {
            // Print the stack trace for any SQLException
            e.printStackTrace();
        }

        // Return -1 if the product couldn't be inserted for some reason
        return -1;
    }

    public void addProductButton() {
        // Get the product details from the user interface
        String productName = productNameField.getText();  // Get product name from the text field
        String productCategory = categoryBox.getValue().toString();  // Get product category from the combo box
        float productPrice = Float.parseFloat(priceTextField.getText());  // Get product price from the text field and convert it to float
        String productPriceText = priceTextField.getText(); // Get the product price from the text field as a string for easier validation

        // Validate that the product price is a valid float
        try {
            productPrice = Float.parseFloat(priceTextField.getText());
        } catch (NumberFormatException e) {
            showAlert("Error", "Product price must be a number.");
            return;
        }

        // Call the addProduct() method to insert a new product into the 'product' table and retrieve the ID of the newly inserted product
        int newProductId = addProduct(productName, productCategory, productPrice);

        // Validate that none of the fields are empty
        if (productName.isEmpty() || productCategory.isEmpty() || productPriceText.isEmpty()) {
            showAlert("Error", "All fields must be filled in.");
            return;
        }

        // Get the data from the TableView
        ObservableList<Pair<Inventory, Integer>> tableData = ingredientsTable.getItems();

        // Loop through each row in the TableView
        for (Pair<Inventory, Integer> row : tableData) {
            // Get the ingredient and quantity from the current row
            Inventory ingredient = row.getKey();
            int quantity = row.getValue();

            // Call the addProductIngredient() method to insert each ingredient into the 'product_inventory' table with the new product ID
            addProductIngredient(newProductId, ingredient.getInventoryId(), quantity);
        }

        // Clear the TableView and text fields after adding the product
        ingredientsTable.getItems().clear();
        productNameField.clear();
        categoryBox.setValue("");
        priceTextField.clear();

        showAlert("Success", "Product added successfully.");
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

    // METHOD TO ADD PRODUCT INGREDIENTS TO THE PRODUCT_INVENTORY JUNCTION TABLE
    private void addProductIngredient(int productId, int ingredientId, int quantity) {
        Connection conn = SQLConnect.connectDb();
        // SQL query to insert a new ingredient into the 'product_inventory' table for a specific product
        String sql = "INSERT INTO product_inventory (product_id, inventory_id, amount_used) VALUES (?, ?, ?)";

        try {
            // Prepare the SQL query
            PreparedStatement statement = conn.prepareStatement(sql);
            // Set the parameters in the SQL query
            statement.setInt(1, productId);
            statement.setInt(2, ingredientId);
            statement.setInt(3, quantity);
            // Execute the SQL query
            statement.executeUpdate();
        } catch (SQLException e) {
            // Print the stack trace for any SQLException
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
            if(RSSupId.next()){
                supplierName = RSSupId.getString("supplier_name");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return supplierName;
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

    //CHANGE LANGUAGE

    // get current stage
    private Stage getStage() {
        return (Stage) ingredientsBox.getScene().getWindow();
    }


    // change the language
    @FXML
    private void handleLanguageToggle() {
        try {
            ChangeLanguage.toggleLanguage(getStage(), "AddProductPage.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
