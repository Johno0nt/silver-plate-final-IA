package com.silverplate.silverplate.Controller;

import com.silverplate.silverplate.ChangeLanguage;
import com.silverplate.silverplate.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ResourceBundle;

public class LoginPage {
    @FXML
    private Label loginWarn;

    @FXML
    private Button loginbutton;

    @FXML
    private PasswordField pinField;
    private final String correctPin = "407364";

    ResourceBundle englishBundle = ResourceBundle.getBundle("Text_en_AU");

    @FXML
    private void handleLogin() throws IOException {
        // set the dashboard window.
        Stage dashStage = new Stage();

        // get the pin from the text field.
        String inputPin = pinField.getText();

        // validate the gathered pin.
        if (inputPin.equals(correctPin)) {
            // close the login page if the correct pin is input
            Stage currentStage = (Stage) loginbutton.getScene().getWindow();
            currentStage.close();
            // show the dashboard if the input PIN matches the correct PIN
            goDashboard(dashStage);
        } else {
            // display an error message if the input PIN is incorrect
            loginWarn.setText("Login Failed");
            pinField.setText("");
        }
    }

    // transition to dashboard.
    public void goDashboard(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("DashboardPage.fxml"));
        fxmlLoader.setResources(englishBundle);
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Dashboard");
        stage.setScene(scene);
        stage.show();
    }

}