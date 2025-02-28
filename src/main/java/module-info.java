module kurata.assignment_1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens kurata.assignment_1 to javafx.fxml;
    exports kurata.assignment_1;
}