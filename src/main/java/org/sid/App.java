package org.sid;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.sid.backend.SQLConnector;
import org.sid.graphicControllers.SecondaryController;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("login").load());
        stage.setScene(scene);
        stage.setTitle("Monitorização de Culturas");
        stage.show();
    }

    public static void setRoot(String fxml, SQLConnector connector) throws IOException {
        FXMLLoader loader = loadFXML(fxml);
        Parent root = loader.load();
        SecondaryController controller = loader.<SecondaryController>getController();
        controller.setSQLConnector(connector);
        scene.setRoot(root);
    }

    private static FXMLLoader loadFXML(String fxml) throws IOException {
        return new FXMLLoader(App.class.getResource(fxml + ".fxml"));
    }

    public static void main(String[] args) {
        launch();
    }

}