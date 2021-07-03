package org.sid.graphicControllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.sid.backend.IniReader;
import org.sid.backend.SQLConnector;

public class SecondaryController {

    private SQLConnector connector;
    private boolean isOriginalButton = true;

    @FXML
    public Button startMigrationBtn;
    public Label notificationLbl;
    public TextField periodicity;
    public Label errorLbl;

    @FXML
    private void startMigration() {
        if (isOriginalButton) {
            double p = 0.0;
            try {
                p = Double.parseDouble(periodicity.getText());
            } catch (NumberFormatException e) {
                errorLbl.setText("Periodicidade inválida");
            }
            if (p < 1.0 || p > 5.0) errorLbl.setText("Periodicidade inválida");
            else {
                isOriginalButton = false;
                errorLbl.setText("");
                startMigrationBtn.setText("Parar migraçao de dados");
                IniReader.connectToMongos(connector, (int) p);
            }
        } else {
            System.exit(0);
        }
    }

    public void setSQLConnector(SQLConnector connector) {
        this.connector = connector;
    }


}