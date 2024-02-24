package com.silverplate.silverplate.Controller;

import com.silverplate.silverplate.ChangeLanguage;
import com.silverplate.silverplate.Main;
import com.silverplate.silverplate.Model.Inventory;
import com.silverplate.silverplate.Model.Product;
import com.silverplate.silverplate.Model.Staff;
import com.silverplate.silverplate.Model.Suppliers;
import com.silverplate.silverplate.SQLConnect;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ProductsPage implements Initializable {
    // stage setting equivalence for tableView and Columns to populate with staff data.
    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, Integer> col_productId;
    @FXML
    private TableColumn<Product, String> col_productCategory;
    @FXML
    private TableColumn<Product, String> col_productName;
    @FXML
    private TableColumn<Product, Float> col_productPrice;
    @FXML
    private TableColumn<Product, String> col_productIngredients;

    ResourceBundle englishBundle = ResourceBundle.getBundle("Text_en_AU");

    // go to the add products page.
    public void showAddProductsPage() throws IOException {
        Stage addProductStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("AddProductPage.fxml"));
        fxmlLoader.setResources(englishBundle);
        Scene scene = new Scene(fxmlLoader.load());
        addProductStage.setTitle("Add Products");
        addProductStage.setScene(scene);
        addProductStage.show();
    }

    // go to the add products page.
    public void showEditProductsPage() throws IOException {
        Stage addProductStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("EditProductPage.fxml"));
        fxmlLoader.setResources(englishBundle);
        Scene scene = new Scene(fxmlLoader.load());
        addProductStage.setTitle("Edit Products");
        addProductStage.setScene(scene);
        addProductStage.show();
    }

    // FIND THE NAME OF THE SUPPLIER BASED ON THE ID FOR THE FETCHING OF THE DATA
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

    // GETTING THE PRODUCTS DATA ALONGSIDE THE JUNCTION TABLE PRODUCTS_INVENTORY
    public ObservableList<Product> fetchProducts() {
        Connection conn = SQLConnect.connectDb();
        ObservableList<Product> products = FXCollections.observableArrayList();
        String sql = "SELECT *  FROM product p\n" +
                "JOIN product_inventory pi ON p.product_id = pi.product_id\n" +
                "JOIN inventory i ON pi.inventory_id = i.inventory_id\n" +
                "ORDER BY p.product_id";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery(sql)) {

            int currentProductId = -1;
            Product currentProduct = null;
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                if (productId != currentProductId) {
                    // we've encountered a new product, so add the old one to the list (if it exists)
                    if (currentProduct != null) {
                        products.add(currentProduct);
                    }
                    // and start a new one
                    String productName = rs.getString("product_name");
                    String productCategory = rs.getString("product_category");
                    float productPrice = rs.getFloat("product_price");
                    currentProduct = new Product(productId, productCategory, productName, productPrice, FXCollections.observableArrayList(), FXCollections.observableArrayList());
                    currentProductId = productId;
                }
                // add the inventory item to the current product
                int inventoryId = rs.getInt("inventory_id");
                String inventoryName = rs.getString("ingredient_name");
                int supplierId = rs.getInt("supplier_id");
                String supplierName = findSupplierName(supplierId);
                int inventoryQuantity = rs.getInt("amount_used");
                LocalDate dateReceived = rs.getDate("date_received").toLocalDate();
                String inventoryCategory = rs.getString("inventory_category");
                // add to products
                currentProduct.getIngredients().add(new Inventory(inventoryId, inventoryName, supplierId, supplierName, inventoryQuantity, dateReceived, inventoryCategory));
                currentProduct.getIngredientQuantity().add(inventoryQuantity);
            }
            // add the last product to the list (if it exists)
            if (currentProduct != null) {
                products.add(currentProduct);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return products;
    }

    // LOAD THE PRODUCTS FROM THE TABLE INTO THE TABLEVIEW
    public void loadTable() {
        col_productId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        col_productCategory.setCellValueFactory(new PropertyValueFactory<>("productCategory"));
        col_productName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        col_productPrice.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        col_productIngredients.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            List<String> ingredientDisplay = new ArrayList<>();
            for (int i = 0; i < product.getIngredients().size(); i++) {
                String display = product.getIngredientQuantity().get(i) + "x " + product.getIngredients().get(i).getInventoryName();
                ingredientDisplay.add(display);
            }
            return new SimpleStringProperty(String.join(", ", ingredientDisplay));
        });

        // Fetch data and populate table
        List<Product> productList = fetchProducts();
        ObservableList<Product> products = FXCollections.observableArrayList(productList);
        productTable.setItems(products);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadTable();
    }

    //CHANGE LANGUAGE

    // get current stage
    private Stage getStage() {
        return (Stage) productTable.getScene().getWindow();
    }

    // get ResourceBundle
    public ResourceBundle getResourceBundle() {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("ProductsPage.fxml");
        return resourceBundle;
    }

    // change the language
    @FXML
    private void handleLanguageToggle() {
        try {
            ChangeLanguage.toggleLanguage(getStage(), "ProductsPage.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
