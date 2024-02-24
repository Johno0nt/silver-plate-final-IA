package com.silverplate.silverplate;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class ChangeLanguage {
    private static Locale currentLocale = Locale.ENGLISH; // default locale

    public static void toggleLanguage(Stage stage, String fxmlPath) throws IOException {
        // toggle between English and Vietnamese
        if (currentLocale == Locale.ENGLISH) {
            currentLocale = new Locale("vi", "VN");
        } else {
            currentLocale = Locale.ENGLISH;
        }

        ResourceBundle bundle = ResourceBundle.getBundle("Text", currentLocale);
        FXMLLoader fxmlLoader = new FXMLLoader(ChangeLanguage.class.getResource(fxmlPath), bundle);

        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);

        // This line will force the button to layout itself again, to workaround a known JavaFX bug.
        scene.getRoot().applyCss();

        stage.setScene(scene);
    }
}
