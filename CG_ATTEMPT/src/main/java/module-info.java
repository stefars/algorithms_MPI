module org.example.cg_attempt {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.annotation;
    requires org.locationtech.jts;


    opens org.example.cg_attempt to com.fasterxml.jackson.databind;
    exports org.example.cg_attempt;
}