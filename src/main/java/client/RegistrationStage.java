package client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.DataOutputStream;
import java.io.IOException;

public class RegistrationStage extends Stage {
    DataOutputStream out;


    public RegistrationStage(DataOutputStream out) {
        this.out = out;
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/fxml/signup.fxml"));
            setTitle("Sign Up");
            Scene scene = new Scene(root, 400, 150);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
