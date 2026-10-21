module com.dio {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.dio to javafx.fxml;
    exports com.dio;
}
