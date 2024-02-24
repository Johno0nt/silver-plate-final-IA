package com.silverplate.silverplate.Controller;

import com.silverplate.silverplate.ChangeLanguage;
import com.silverplate.silverplate.Main;
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

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.function.Supplier;

public class SuppliersPage implements Initializable {
    @FXML
    private ComboBox<String> categoryBox;

    @FXML
    private TableView<Suppliers> supplierTable;

    @FXML
    private TableColumn<Suppliers, String> col_supplierName;

    @FXML
    private TableColumn<Suppliers, String> col_supplierPhone;

    @FXML
    private TableColumn<Suppliers, String> col_supplierEmail;

    @FXML
    private TableColumn<Suppliers, String> col_inventoryCategory;

    @FXML
    private TableColumn<Suppliers, Integer> col_supplierId;

    @FXML
    private TextField nameField;

    @FXML
    private NumericTextField phoneField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField searchField;

    @FXML
    private TextField idField;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        categoryBox.setItems(FXCollections.observableArrayList("Dairy","Poultry","Meat","Dry Good","Vegetables", "Fruits", "Beverages"));
        loadTable();
        searchSupplier();

        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 10) {
                phoneField.setText(oldValue); // Discard the new value
            }
        });
    }

    // Retrieve the SQL data from the Supplier Table:
    public static ObservableList<Suppliers> getSupplierData() {
        // connect to the db.
        Connection conn = SQLConnect.connectDb();
        // initialize an arraylist to store the staffList.
        ObservableList<Suppliers> supplierList = FXCollections.observableArrayList();
        try {
            // query the db.
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM `supplier`");
            ResultSet rs = ps.executeQuery();

            // fill out the staff arrayList while query is active.
            while (rs.next()) {
                supplierList.add(new Suppliers(rs.getString("supplier_name"),
                        rs.getString("supplier_phone"),
                        rs.getString("supplier_email"),
                        rs.getString("supplier_category"),
                        Integer.parseInt(rs.getString("supplier_id"))));
            }
        } catch (Exception e) {

        }
        return supplierList;
    }

    // initialize the arraylist for the suppliers
    ObservableList<Suppliers> supplierList = FXCollections.observableArrayList();

    // load data from SQL query.
    private void loadTable() {
        Connection c = SQLConnect.connectDb();
        supplierList = getSupplierData();

        col_supplierId.setCellValueFactory(new PropertyValueFactory<Suppliers, Integer>("supplierId"));
        col_supplierName.setCellValueFactory(new PropertyValueFactory<Suppliers, String>("supplierName"));
        col_supplierPhone.setCellValueFactory(new PropertyValueFactory<Suppliers, String>("supplierPhone"));
        col_supplierEmail.setCellValueFactory(new PropertyValueFactory<Suppliers, String>("supplierEmail"));
        col_inventoryCategory.setCellValueFactory(new PropertyValueFactory<Suppliers, String>("inventoryCategory"));
        supplierTable.setItems(supplierList);
    }

    // add data from textFields into Supplier table in SQL.
    public void addSupplier() {
        // CONNECT TO DB
        PreparedStatement addSupplierPST = null;
        Connection c = SQLConnect.connectDb();
        // SQL INSERT QUERY
        String addSupplierQuery = "INSERT INTO `supplier` (supplier_name, supplier_email, supplier_phone, supplier_category) VALUES (?,?,?,?)";
        // RETRIEVE THE VALUES FROM THE TEXTFIELDS
        try {
            addSupplierPST = c.prepareStatement(addSupplierQuery);
            addSupplierPST.setString(1, nameField.getText());
            addSupplierPST.setString(2, emailField.getText());
            addSupplierPST.setString(3, phoneField.getText().toString());
            addSupplierPST.setString(4, categoryBox.getValue().toString());
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
    public void supplierEdit() {
        try {
            Connection c = SQLConnect.connectDb();
            // QUERY FOR UPDATING VALUES
            String updateSQL = "UPDATE `supplier` SET supplier_name= ?, supplier_email = ?, supplier_phone = ?, supplier_category = ? WHERE supplier_id = ?";
            PreparedStatement supplierUp = c.prepareStatement(updateSQL);

            // ASSIGN THE ? VALUES TO THE TEXT-FIELD COUNTERPARTS
            supplierUp.setString(1, nameField.getText());
            supplierUp.setString(2, emailField.getText());
            supplierUp.setString(3, phoneField.getText().toString());
            supplierUp.setString(4, categoryBox.getValue().toString());
            supplierUp.setString(5, idField.getText().toString());

            // EXECUTE THE QUERY
            supplierUp.execute();

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

    // delete the data from the Supplier table.
    public void supplierDelete() {
        Connection c = SQLConnect.connectDb();
        // SQL QUERY TO DELETE ENTRY BASED ON NAME
        String supplierDeleteQuery = "DELETE FROM `supplier` WHERE supplier_id = ?";
        try {
            PreparedStatement supplierDelete = c.prepareStatement(supplierDeleteQuery);
            // GET NAME VALUE TO SET WHERE CONDITION TRUE
            supplierDelete.setString(1, idField.getText());
            supplierDelete.execute();

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

    // SEARCH FOR A SUPPLIER WITH SEARCH BAR
    void searchSupplier() {
        col_supplierId.setCellValueFactory(new PropertyValueFactory<Suppliers, Integer>("supplierId"));
        col_supplierName.setCellValueFactory(new PropertyValueFactory<Suppliers, String>("supplierName"));
        col_supplierPhone.setCellValueFactory(new PropertyValueFactory<Suppliers, String>("supplierPhone"));
        col_supplierEmail.setCellValueFactory(new PropertyValueFactory<Suppliers, String>("supplierEmail"));
        col_inventoryCategory.setCellValueFactory(new PropertyValueFactory<Suppliers, String>("inventoryCategory"));

        ObservableList<Suppliers> supplierSearchList;
        supplierSearchList = getSupplierData();

        supplierTable.setItems(supplierSearchList);

        // Wrap the observable list into a filtered list such that the table is displaying filtered data.
        FilteredList<Suppliers> suppliersFilteredList = new FilteredList<>(supplierSearchList, b -> true);

        // set the filter to predicate the text based on user input into the search field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            suppliersFilteredList.setPredicate(suppliers -> {
                // IF filter text is empty, display all the suppliers
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Comparing the supplier names with the search field.
                String lowerCaseFilter = newValue.toLowerCase();

                // filter matches the name
                if (suppliers.getSupplierName().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true;
                } else if (suppliers.getSupplierPhone().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true; // filter matches phone
                } else if (suppliers.getSupplierEmail().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true; // filter matches email
                } else if (suppliers.getInventoryCategory().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true; // filter matches inventory category
                } else {
                    return false;
                }
            });
        });
        SortedList<Suppliers> sortedData = new SortedList<>(suppliersFilteredList);
        sortedData.comparatorProperty().bind(supplierTable.comparatorProperty());
        supplierTable.setItems(sortedData);
    }

    int index = -1;

    // update text-fields for each row clicked in the table.
    @FXML
    private void getSelected() {
        index = supplierTable.getSelectionModel().getSelectedIndex();
        if (index <= -1) { // if index is -1, nothing has been selected.
            return;
        }

        // set text from table-view to text-fields
        idField.setText(String.valueOf(col_supplierId.getCellData(index)));
        nameField.setText(col_supplierName.getCellData(index));
        phoneField.setText(col_supplierPhone.getCellData(index));
        emailField.setText(col_supplierEmail.getCellData(index));
        categoryBox.setValue(col_inventoryCategory.getCellData(index));
    }

    //CHANGE LANGUAGE
    // get current stage
    private Stage getStage() {
        return (Stage) supplierTable.getScene().getWindow();
    }

    // change the language
    @FXML
    private void handleLanguageToggle() {
        try {
            ChangeLanguage.toggleLanguage(getStage(), "SuppliersPage.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
