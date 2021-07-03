package org.sid.graphicControllers;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.sid.App;
import org.sid.backend.SQLConnector;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    private SQLConnector connector;

    @FXML
    public TextField usernameTextField;
    public PasswordField passwordField;
    public Label errorLbl;

    @FXML
    private void login() {
        try {
            connector = new SQLConnector(usernameTextField.getText(), passwordField.getText());
            connector.connectToSQL();
            changeScene();
        } catch (CommunicationsException e) {
            errorLbl.setText("Connection to SQL database failed");
            errorLbl.setVisible(true);
        } catch (ClassNotFoundException | SQLException | IllegalArgumentException e) {
            errorLbl.setText("Wrong Credentials");
            errorLbl.setVisible(true);
        } catch (IOException e) {
            errorLbl.setText("Configurations file not found");
            errorLbl.setVisible(true);
        }
    }

    private void changeScene() throws IOException {
        App.setRoot("secondary", connector);
    }
}
