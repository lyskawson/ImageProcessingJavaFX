module org.example.imagesprocessingjavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;


    opens org.example.imagesprocessingjavafx to javafx.fxml;
    exports org.example.imagesprocessingjavafx;
}