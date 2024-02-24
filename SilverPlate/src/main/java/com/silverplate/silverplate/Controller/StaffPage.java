package com.silverplate.silverplate.Controller;

import com.silverplate.silverplate.ChangeLanguage;
import com.silverplate.silverplate.Main;
import com.silverplate.silverplate.Model.Staff;
import com.silverplate.silverplate.SQLConnect;
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
import java.util.ResourceBundle;

public class StaffPage implements Initializable {
    // stage setting equivalence for tableView and Columns to populate with staff data.
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


    ResourceBundle englishBundle = ResourceBundle.getBundle("Text_en_AU");

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
    public void initialize(URL url, ResourceBundle rb) {
        loadTable();
    }

    // show staff table
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

    // go to addEditStaff page.

    // go to the add staff page.
    public void showAddEditStaffPage() throws IOException {
        Stage showAddEditStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("AddEditStaffPage.fxml"));
        fxmlLoader.setResources(englishBundle);
        Scene scene = new Scene(fxmlLoader.load());
        showAddEditStage.setTitle("Add or Edit Staff");
        showAddEditStage.setScene(scene);
        showAddEditStage.show();
    }


    // show shift pages
    public void showShifts() throws IOException {
        Stage shiftStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("StaffShiftsPage.fxml"));
        fxmlLoader.setResources(englishBundle);
        Scene scene = new Scene(fxmlLoader.load());
        shiftStage.setTitle("Shifts");
        shiftStage.setScene(scene);
        shiftStage.show();
    }

    //CHANGE LANGUAGE

    // get current stage
    private Stage getStage() {
        return (Stage) staffTable.getScene().getWindow();
    }

    // change the language
    @FXML
    private void handleLanguageToggle() {
        try {
            ChangeLanguage.toggleLanguage(getStage(), "StaffPage.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
