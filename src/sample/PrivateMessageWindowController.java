package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.DataOutputStream;
import java.io.IOException;

public class PrivateMessageWindowController {
    @FXML
    TextArea textArea;

    @FXML
    Button btn;

    public void btnClick() {
        if (!((PrivateMessageStage) btn.getScene().getWindow()).parentList.contains(textArea)) {
            ((PrivateMessageStage) btn.getScene().getWindow()).parentList.add(textArea);
        }
        DataOutputStream out = ((PrivateMessageStage) btn.getScene().getWindow()).out;
        String nickTo = ((PrivateMessageStage) btn.getScene().getWindow()).nickTo;
        try {
            out.writeUTF("@" + nickTo + " " + textArea.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }


        // get a handle to the stage
        Stage stage = (Stage) btn.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
}
