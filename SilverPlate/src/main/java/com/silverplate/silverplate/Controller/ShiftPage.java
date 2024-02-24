package com.silverplate.silverplate.Controller;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.silverplate.silverplate.ChangeLanguage;
import com.silverplate.silverplate.Main;
import com.silverplate.silverplate.Model.Shift;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import com.silverplate.silverplate.Model.Staff;
import com.silverplate.silverplate.SQLConnect;
import com.calendarfx.view.CalendarView;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class ShiftPage {
    @FXML
    private CalendarView shiftCalendar;
    @FXML
    private CalendarSource calendarSource = new CalendarSource("Source");
    @FXML
    private Calendar shifts = new Calendar("Shifts");

    // Define the selectedEntry instance variable
    private Entry<?> selectedEntry;

    public ShiftPage() {
        calendarSource.getCalendars().add(shifts);
    }

    ZoneId id = ZoneId.of("Australia/Brisbane");

    // Query the staff table to get the staff_id for the person's name
    public void getShiftData() {
        // Create a connection to your database

    }

    // Search for the staffId in the `staff` table based on the entry name
    // insert the staffId, start_time, end_time, and shift_date into the `staff_shift` SQL table
    public void saveEntry(Entry<?> entry) {
        Connection conn = SQLConnect.connectDb();
        String sql = "INSERT INTO staffshift (staff_id, start_time, end_time, shift_date) VALUES (?, ?, ?, ?)";

        try (
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Get the staff name from the entry's title
            String staffName = entry.getTitle();
            // Get the staff id corresponding to this name
            int staffId = getStaffIdByName(staffName);

            // If staffId is -1, the staff name was not found, so show an error dialog
            if (staffId == -1) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Invalid staff name: " + staffName);
                alert.showAndWait();
                return;  // stop execution of this method
            }

            // CalendarFX uses java.time classes, so we can directly get the start and end times
            LocalTime startTime = entry.getStartTime();
            LocalTime endTime = entry.getEndTime();

            // Getting the date of the Entry
            LocalDate date = entry.getStartDate();

            pstmt.setInt(1, staffId);
            pstmt.setTime(2, java.sql.Time.valueOf(startTime));
            pstmt.setTime(3, java.sql.Time.valueOf(endTime));
            pstmt.setDate(4, java.sql.Date.valueOf(date));

            pstmt.executeUpdate();

            // POPUP DIALOG SHOWING ADDED SUCCESS
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Added!");

            alert.showAndWait();

        } catch (SQLIntegrityConstraintViolationException e) {
            // handle duplicate entry attempt
            System.out.println("Attempted to insert duplicate entry: " + entry);
            // then continue to next entry
        } catch (SQLException e) {
            // Handle any SQL exceptions
            e.printStackTrace();
        }
    }

    // Method to saving all of the CalendarFX entries.
    public void saveAllEntries() {
        for (CalendarSource calendarSource : shiftCalendar.getCalendarSources()) {
            for (com.calendarfx.model.Calendar calendar : calendarSource.getCalendars()) {
                List<Entry<?>> entries = calendar.findEntries("");
                for (Entry<?> entry : entries) {
                    saveEntry(entry);
                }
            }
        }
    }

    // LOAD THE ENTRIES OF THE TABLE INTO THE CALENDAR VIEW
    public void loadEntries() {
        Connection conn = SQLConnect.connectDb();

        // query to get all of the staff_shifts onto the calendar view
        String sql = "SELECT * FROM staffshift";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery(sql);

            // ITERATE THROUGH ALL FIELDS
            while (rs.next()) {
                // GET THE NAME FROM THE GIVEN ID TO PUT INTO ENTRY TITLES
                String staffName = getStaffNameById(rs.getInt("staff_id"));
                LocalTime startTime = rs.getTime("start_time").toLocalTime();
                LocalTime endTime = rs.getTime("end_time").toLocalTime();
                LocalDate date = rs.getDate("shift_date").toLocalDate();

                // create CalendarFX entry
                Entry<String> entry = new Entry<>(staffName);
                entry.setInterval(date.atTime(startTime), date.atTime(endTime));

                // ADD THE ENTRY
                shifts.addEntry(entry);
            }
            // REFRESH THE VIEW
            shiftCalendar.getCalendarSources().clear();  // clear any previous sources
            shiftCalendar.getCalendarSources().add(calendarSource); // add the source containing our shifts
            shiftCalendar.setRequestedTime(LocalTime.now()); // update the calendar view
        } catch (SQLException e) {
            // Handle any SQL exceptions
            e.printStackTrace();
        }
    }

    // GET THE NAMES OF THE STAFF BASED ON THE ID
    public String getStaffNameById(int staffId) {
        Connection conn = SQLConnect.connectDb();

        String sql = "SELECT staff_name FROM staff WHERE staff_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // SET THE ? VARIABLE IN THE QUERY TO STAFF_ID
            pstmt.setInt(1, staffId);
            ResultSet rs = pstmt.executeQuery();

            // GET THE STAFF_NAME WHILE THE QUERY IS TRUE
            if (rs.next()) {
                return rs.getString("staff_name");
            }

        } catch (SQLException e) {
            // Handle any SQL exceptions
            e.printStackTrace();
        }

        return null;
    }

    // getting the id of the staff member to properly save it in the table.
    public int getStaffIdByName(String staffName) {
        Connection conn = SQLConnect.connectDb();

        // ensure that case sensitivity is not an issue
        String sql = "SELECT staff_id FROM staff WHERE LOWER(staff_name) = LOWER(?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the staff name to the prepared statement
            pstmt.setString(1, staffName);

            // Execute the SQL statement and get the result
            ResultSet rs = pstmt.executeQuery();

            // If a row has been found, return the staff ID
            if (rs.next()) {
                return rs.getInt("staff_id");
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Cannot find staff name: " + staffName);
                alert.showAndWait();
                // Throw an exception or return a special value if the staff was not found
                throw new IllegalArgumentException("Staff not found");
            }
        } catch (SQLException e) {
            // Print stack trace for any SQL exceptions
            e.printStackTrace();
            return -1; // return a special value to indicate that an error occurred
        }
    }
}
