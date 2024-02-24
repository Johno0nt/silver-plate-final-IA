package com.silverplate.silverplate.Controller;

import com.silverplate.silverplate.ChangeLanguage;
import com.silverplate.silverplate.Model.Staff;
import com.silverplate.silverplate.SQLConnect;
import impl.com.calendarfx.view.NumericTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AddEditStaffPage implements Initializable {
    @FXML
    private TableView<Staff> staffTable;
    @FXML
    private TableColumn<Staff, String> col_staffName;
    @FXML
    private TableColumn<Staff, String> col_staffAddress;
    @FXML
    private TableColumn<Staff, String> col_staffEmail;
    @FXML
    private TableColumn<Staff, String> col_staffPhone;
    @FXML
    private TableColumn<Staff, Integer> col_staffId;

    @FXML
    private TextField staffNameField;

    @FXML
    private TextField staffAddressField;

    @FXML
    private TextField staffEmailField;

    @FXML
    private NumericTextField staffPhoneField;

    @FXML
    private TextField staffIdField;

    // Get the data from the SQL table of Staff.
    public static ObservableList<Staff> getDataStaff() {
        // connect to the db.
        Connection conn = SQLConnect.connectDb();
        // initialize an arraylist to store the staffList.
        ObservableList<Staff> staffList = FXCollections.observableArrayList();
        try {
            // query the db.
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM `staff`");
            ResultSet rs = ps.executeQuery();

            // fill out the staff arrayList while query is active.
            while (rs.next()) {
                staffList.add(new Staff(rs.getString("staff_name"),
                        rs.getString("staff_address"),
                        rs.getString("staff_email"),
                        rs.getString("staff_phone"),
                        Integer.parseInt(rs.getString("staff_id"))));
            }
        } catch (Exception e) {

        }
        return staffList;
    }
    ObservableList<Staff> listStaff = FXCollections.observableArrayList();

    int index = -1;

    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement pst = null;
    Staff staff = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadTable();

        staffPhoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 10) {
                staffPhoneField.setText(oldValue); // Discard the new value
            }
        });
    }

    private void loadTable() {
        Connection c = SQLConnect.connectDb();
        listStaff = getDataStaff();

        col_staffId.setCellValueFactory(new PropertyValueFactory<Staff, Integer>("staffId"));
        col_staffName.setCellValueFactory(new PropertyValueFactory<Staff, String>("staffName"));
        col_staffEmail.setCellValueFactory(new PropertyValueFactory<Staff, String>("staffEmail"));
        col_staffPhone.setCellValueFactory(new PropertyValueFactory<Staff, String>("staffPhone"));
        col_staffAddress.setCellValueFactory(new PropertyValueFactory<Staff, String>("staffAddress"));
        staffTable.setItems(listStaff);
    }

    // add data from textFields into Staff table in SQL.
    public void addStaff() {
        // CONNECT TO DB
        PreparedStatement addStaffPST = null;
        Connection c = SQLConnect.connectDb();
        // SQL INSERT QUERY
        String addSupplierQuery = "INSERT INTO `staff` (staff_name, staff_email, staff_phone, staff_address) VALUES (?,?,?,?)";
        // RETRIEVE THE VALUES FROM THE TEXTFIELDS
        try {
            addStaffPST = c.prepareStatement(addSupplierQuery);
            addStaffPST.setString(1, staffNameField.getText());
            addStaffPST.setString(2, staffEmailField.getText());
            addStaffPST.setString(3, staffPhoneField.getText().toString());
            addStaffPST.setString(4, staffAddressField.getText());
            // EXECUTE QUERY WITH TEXTFIELD VALUES
            addStaffPST.execute();

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
    public void staffEdit() {
        try {
            Connection c = SQLConnect.connectDb();
            // QUERY FOR UPDATING VALUES
            String updateSQL = "UPDATE `staff` SET staff_name= ?, staff_email = ?, staff_phone = ?, staff_address = ? WHERE staff_id = ?";
            PreparedStatement supplierUp = c.prepareStatement(updateSQL);

            // ASSIGN THE ? VALUES TO THE TEXT-FIELD COUNTERPARTS
            supplierUp.setString(1, staffNameField.getText());
            supplierUp.setString(2, staffEmailField.getText());
            supplierUp.setString(3, staffPhoneField.getText().toString());
            supplierUp.setString(4, staffAddressField.getText());
            supplierUp.setString(5, staffIdField.getText().toString());

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

    // update text-fields for each row clicked in the table.
    @FXML
    private void getStaffSelected() {
        index = staffTable.getSelectionModel().getSelectedIndex();
        if (index <= -1) { // if index is -1, nothing has been selected.
            return;
        }

        // set text from table-view to text-fields
        staffIdField.setText(String.valueOf(col_staffId.getCellData(index)));
        staffNameField.setText(col_staffName.getCellData(index));
        staffPhoneField.setText(col_staffPhone.getCellData(index));
        staffEmailField.setText(col_staffEmail.getCellData(index));
        staffAddressField.setText(col_staffAddress.getCellData(index));
    }

    // delete the data from the Supplier table.
    public void staffDelete() {
        Connection c = SQLConnect.connectDb();
        // SQL QUERY TO DELETE ENTRY BASED ON NAME
        String supplierDeleteQuery = "DELETE FROM `staff` WHERE staff_id = ?";
        try {
            PreparedStatement supplierDelete = c.prepareStatement(supplierDeleteQuery);
            // GET NAME VALUE TO SET WHERE CONDITION TRUE
            supplierDelete.setString(1, staffIdField.getText());
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

    //CHANGE LANGUAGE

    // get current stage
    private Stage getStage() {
        return (Stage) staffNameField.getScene().getWindow();
    }

    // change the language
    @FXML
    private void handleLanguageToggle() {
        try {
            ChangeLanguage.toggleLanguage(getStage(), "AddEditStaffPage.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
