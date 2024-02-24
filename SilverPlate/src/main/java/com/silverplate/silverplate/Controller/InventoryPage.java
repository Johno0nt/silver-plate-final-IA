package com.silverplate.silverplate.Controller;

import com.silverplate.silverplate.ChangeLanguage;
import com.silverplate.silverplate.Main;
import com.silverplate.silverplate.Model.Inventory;
import com.silverplate.silverplate.Model.Staff;
import com.silverplate.silverplate.Model.Suppliers;
import com.silverplate.silverplate.SQLConnect;
import impl.com.calendarfx.view.NumericTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.controlsfx.control.SearchableComboBox;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class InventoryPage implements Initializable {
    // stage setting equivalence for tableView and Columns to populate with inventory data.
    @FXML
    private TableView<Inventory> inventoryTable;
    @FXML
    private TableColumn<Inventory, String> col_inventoryCategory;
    @FXML
    private TableColumn<Inventory, String> col_inventoryName;
    @FXML
    private TableColumn<Inventory, Integer> col_inventoryQt;
    @FXML
    private TableColumn<Inventory, String> col_supplier;
    @FXML
    private TableColumn<Inventory, LocalDate> col_dateReceived;
    @FXML
    private TableColumn<Inventory, Integer> col_invId;
    @FXML
    private TextField inventoryNameField;
    @FXML
    private TextField inventoryIdField;
    @FXML
    private NumericTextField inventoryQuantityField;
    @FXML
    private ComboBox inventoryCategoryBox;
    @FXML
    private SearchableComboBox supplierBox;
    @FXML
    private DatePicker dateReceivedPicker;
    @FXML
    private TextField searchField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        inventoryCategoryBox.setItems(FXCollections.observableArrayList("Dairy","Poultry","Meat","Dry Good","Vegetables", "Fruits", "Beverages"));
        setSupplierComboBox();
        supplierBox.setItems(supplierNames);

        loadTable();
        searchInventory();
    }

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

    // inventory list
    ObservableList<Inventory> inventoryList = FXCollections.observableArrayList();

    // combo box list of supplier names
    ObservableList<String> supplierNames = FXCollections.observableArrayList();

    // Supplier combobox setting.
    public void setSupplierComboBox() {
        Connection conn = SQLConnect.connectDb();
        // query to select the names from the table
        String nameQuery = "SELECT supplier_name from supplier";

        try {
            PreparedStatement ps = conn.prepareStatement(nameQuery);
            ResultSet rs = ps.executeQuery();
            // iterates until all names are in the list
            while (rs.next()) {
                String supplierName = rs.getString("supplier_name");
                supplierNames.add(supplierName);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // load the inventory data onto the table.
    private void loadTable() {
        Connection c = SQLConnect.connectDb();
        inventoryList = getDataInventory();

        col_invId.setCellValueFactory(new PropertyValueFactory<Inventory, Integer>("inventoryId"));
        col_inventoryName.setCellValueFactory(new PropertyValueFactory<Inventory, String>("inventoryName"));
        col_inventoryQt.setCellValueFactory(new PropertyValueFactory<Inventory, Integer>("inventoryQuantity"));
        col_dateReceived.setCellValueFactory(new PropertyValueFactory<Inventory, LocalDate>("dateReceived"));
        col_supplier.setCellValueFactory(new PropertyValueFactory<Inventory, String>("supplierName"));
        col_inventoryCategory.setCellValueFactory(new PropertyValueFactory<Inventory, String>("inventoryCategory"));
        inventoryTable.setItems(inventoryList);
    }

    public int findSupplierId() throws SQLException {
        // FIND THE ASSIGNED SUPPLIER ID FOR THE ACCOMPANYING SUPPLIER NAME
        Connection c = SQLConnect.connectDb();
        String supplierName = supplierBox.getValue().toString();
        String findSupplierId = "SELECT supplier_id FROM `supplier` WHERE supplier_name = ?"; // QUERY TO SEARCH FOR ID BASED ON NAME
        PreparedStatement findingId = c.prepareStatement(findSupplierId);
        int supplierId = -1;
        try {
            findingId.setString(1, supplierName); // SET ? IN QUERY TO NAME
            ResultSet RSSupId = findingId.executeQuery(); // EXECUTE THE QUERY ITSELF
            if(RSSupId.next()){
                supplierId = RSSupId.getInt("supplier_id");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return supplierId;
    }

    // add data from textFields into inventory table in SQL.
    public void addInventory() throws SQLException {
        // CONNECT TO DB
        PreparedStatement addSupplierPST = null;
        Connection c = SQLConnect.connectDb();
        // SQL INSERT QUERY
        String addSupplierQuery = "INSERT INTO `inventory` (ingredient_name, inventory_quantity, inventory_category, date_received, supplier_id) VALUES (?,?,?,?,?)";

        // FIND THE SUPPLIER ID
        int supplierId = findSupplierId();

        // RETRIEVE THE VALUES FROM THE TEXTFIELDS
        try {
            addSupplierPST = c.prepareStatement(addSupplierQuery);
            addSupplierPST.setString(1, inventoryNameField.getText());
            addSupplierPST.setString(2, inventoryQuantityField.getText());
            addSupplierPST.setString(3, inventoryCategoryBox.getValue().toString());
            addSupplierPST.setDate(4, java.sql.Date.valueOf(dateReceivedPicker.getValue()));
            addSupplierPST.setString(5, String.valueOf(supplierId));
            // EXECUTE QUERY WITH TEXTFIELD VALUES
            addSupplierPST.execute();

            // POPUP DIALOG BOX SHOWING SUCCESS
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Added!");

            alert.showAndWait();

            // UPDATE THE TABLE TO SHOW NEW VALUE
            loadTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    // edit the given data from the textFields
    public void inventoryEdit() {
        try {
            Connection c = SQLConnect.connectDb();
            // QUERY FOR UPDATING VALUES
            String updateSQL = "UPDATE `inventory` SET ingredient_name= ?,  inventory_quantity= ?, supplier_id= ?, inventory_category= ?, date_received = ? WHERE inventory_id = ?";
            PreparedStatement inventoryUpdate = c.prepareStatement(updateSQL);

            // find the supplierId
            int supplierId = findSupplierId();

            // ASSIGN THE ? VALUES TO THE TEXT-FIELD COUNTERPARTS
            inventoryUpdate.setString(1, inventoryNameField.getText());
            inventoryUpdate.setString(2, inventoryQuantityField.getText());
            inventoryUpdate.setInt(3, supplierId);
            inventoryUpdate.setString(4, inventoryCategoryBox.getValue().toString());
            inventoryUpdate.setDate(5, java.sql.Date.valueOf(dateReceivedPicker.getValue()));
            inventoryUpdate.setString(6, inventoryIdField.getText());


            // EXECUTE THE QUERY
            inventoryUpdate.execute();

            // SHOW THE DIALOG BOX
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Updated!");

            alert.showAndWait();

            loadTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // delete the data from the inventory table.
    public void inventoryDelete() {
        Connection c = SQLConnect.connectDb();
        // SQL QUERY TO DELETE ENTRY BASED ON NAME
        String inventoryDeleteQuery = "DELETE FROM `inventory` WHERE inventory_id = ?";
        try {
            PreparedStatement inventoryDelete = c.prepareStatement(inventoryDeleteQuery);
            // GET NAME VALUE TO SET WHERE CONDITION TRUE
            inventoryDelete.setString(1, inventoryIdField.getText());
            inventoryDelete.execute();

            // POPUP DIALOG SHOWING DELETE SUCCESS
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Deleted!");

            alert.showAndWait();

            // UPDATE THE TABLE
            loadTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // SEARCH FOR A INVENTORY ITEM WITH SEARCH BAR
    void searchInventory() {
        col_invId.setCellValueFactory(new PropertyValueFactory<Inventory, Integer>("inventoryId"));
        col_inventoryName.setCellValueFactory(new PropertyValueFactory<Inventory, String>("inventoryName"));
        col_inventoryQt.setCellValueFactory(new PropertyValueFactory<Inventory, Integer>("inventoryQuantity"));
        col_dateReceived.setCellValueFactory(new PropertyValueFactory<Inventory, LocalDate>("dateReceived"));
        col_supplier.setCellValueFactory(new PropertyValueFactory<Inventory, String>("supplierName"));
        col_inventoryCategory.setCellValueFactory(new PropertyValueFactory<Inventory, String>("inventoryCategory"));

        ObservableList<Inventory> inventorySearchList;
        inventorySearchList = getDataInventory();

        inventoryTable.setItems(inventorySearchList);

        // Wrap the observable list into a filtered list such that the table is displaying filtered data.
        FilteredList<Inventory> inventoryFilteredList = new FilteredList<>(inventorySearchList, b -> true);

        // set the filter to predicate the text based on user input into the search field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            inventoryFilteredList.setPredicate(Inventory -> {
                // IF filter text is empty, display all the suppliers
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Comparing the inventory names with the search field.
                String lowerCaseFilter = newValue.toLowerCase();

                // filter matches the name
                if (Inventory.getInventoryName().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true;
                } else if (Inventory.getInventoryCategory().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true; // filter matches category
                } else if (Inventory.getSupplierName().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true; // filter matches supplier
                } else {
                    return false;
                }
            });
        });
        SortedList<Inventory> sortedData = new SortedList<>(inventoryFilteredList);
        sortedData.comparatorProperty().bind(inventoryTable.comparatorProperty());
        inventoryTable.setItems(sortedData);
    }

    // update text-fields for each row clicked in the table.
    int index = -1;
    @FXML
    private void getSelected() {
        index = inventoryTable.getSelectionModel().getSelectedIndex();
        if (index <= -1) { // if index is -1, nothing has been selected.
            return;
        }

        // set text from table-view to text-fields
        inventoryIdField.setText(String.valueOf(col_invId.getCellData(index)));
        inventoryNameField.setText(col_inventoryName.getCellData(index));
        inventoryQuantityField.setText(String.valueOf(col_inventoryQt.getCellData(index)));
        inventoryCategoryBox.setValue(col_inventoryCategory.getCellData(index));
        supplierBox.setValue(col_supplier.getCellData(index));
        dateReceivedPicker.setValue(col_dateReceived.getCellData(index));
    }

    //CHANGE LANGUAGE

    // get current stage
    private Stage getStage() {
        return (Stage) supplierBox.getScene().getWindow();
    }

    // change the language
    @FXML
    private void handleLanguageToggle() {
        try {
            ChangeLanguage.toggleLanguage(getStage(), "InventoryPage.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
