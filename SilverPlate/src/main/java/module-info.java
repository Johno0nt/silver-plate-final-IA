module com.silverplate.silverplate {
    requires javafx.controls;
    requires javafx.base;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires com.calendarfx.view;
    requires org.apache.pdfbox;

    opens com.silverplate.silverplate to javafx.fxml;
    opens com.silverplate.silverplate.Model to javafx.base, javafx.fxml;
    opens com.silverplate.silverplate.Controller to javafx.base, javafx.fxml;


    exports com.silverplate.silverplate;
    exports com.silverplate.silverplate.Controller;
    exports com.silverplate.silverplate.Model;
}