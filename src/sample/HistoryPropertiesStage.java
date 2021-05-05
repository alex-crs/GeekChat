package sample;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.DataOutputStream;
import java.io.IOException;

public class HistoryPropertiesStage extends Stage {
    DataOutputStream out;

    public HistoryPropertiesStage(DataOutputStream out) {
        Parent root = null;
        this.out = out;
        try {
            root = FXMLLoader.load(getClass().getResource("historyPropertiesWindow.fxml"));
            setTitle("Параметры истории");
            Scene scene = new Scene(root, 400, 150);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
