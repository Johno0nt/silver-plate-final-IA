package com.silverplate.silverplate;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WindowManager {
        private Stage stage;

        public WindowManager(Stage stage) {
            this.stage = stage;
        }

        public void switchTo(String fxmlFile) throws IOException {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }

    }
