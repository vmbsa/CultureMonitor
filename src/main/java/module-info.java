module org.sid {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.java;
    requires ini4j;
    requires mongo.java.driver;
    requires org.eclipse.paho.client.mqttv3;
    requires org.json;

    opens org.sid to javafx.fxml;
    exports org.sid;
    exports org.sid.graphicControllers;
    exports org.sid.backend;
    opens org.sid.graphicControllers to javafx.fxml;
}