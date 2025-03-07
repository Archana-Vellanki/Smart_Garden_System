module com.example.ooad_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires java.desktop;


    opens com.example.ooad_project to javafx.fxml;
    exports com.example.ooad_project;
    exports com.example.ooad_project.Plant;
    opens com.example.ooad_project.Plant to javafx.fxml;
    exports com.example.ooad_project.Parasite;
    opens com.example.ooad_project.Parasite to javafx.fxml;
    exports com.example.ooad_project.Plant.Children;
    opens com.example.ooad_project.Plant.Children to javafx.fxml;
    exports com.example.ooad_project.API;
    opens com.example.ooad_project.API to javafx.fxml;
    exports com.example.ooad_project.Events;
    opens com.example.ooad_project.Events to javafx.fxml;
}