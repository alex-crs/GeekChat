package client;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class HistoryPropertiesController implements Initializable {
    @FXML
    TextField historySize;

    @FXML
    Button btn;

    @FXML
    Label loadHistoryStatus;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        historySize.setText(Controller.getHistorySize() + "");
        if (!Controller.isLoadHistoryFromServer()) {
            loadHistoryStatus.setText("История загружается из локального файла.");
        } else {
            loadHistoryStatus.setText("История загружается с сервера");
        }
    }

    public void loadHistoryFromServer() {
        if (!Controller.isLoadHistoryFromServer()) {
            Controller.setLoadHistoryFromServer(true);
            loadHistoryStatus.setText("История загружается с сервера");
        } else {
            Controller.setLoadHistoryFromServer(false);
            loadHistoryStatus.setText("История загружается из локального файла.");
        }
    }

    public void setNewHistorySizeLoad() {
        try {
            Controller.setHistorySize(Integer.parseInt(historySize.getText()));
        } catch (NumberFormatException e) {
            e.printStackTrace(); //обработать исключение в label (указать тип ошибки)
        }
    }
}
