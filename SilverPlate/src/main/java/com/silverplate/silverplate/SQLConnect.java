package com.silverplate.silverplate;

import com.silverplate.silverplate.Model.Staff;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.swing.JOptionPane;

import java.sql.*;

public class SQLConnect {
    Connection conn;

    public static Connection connectDb() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/silverplateapp", "root", "");
            return conn;
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
