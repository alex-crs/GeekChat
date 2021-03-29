package sample;



import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    ObservableList<String> users = FXCollections.observableArrayList("Алексей", "Виталий", "Арина", "Doraemon");
    @FXML
    ListView<String> userList;
    @FXML
    TextArea dialog;
    @FXML
    TextField writerArea;
    @FXML
    Label userName;
    @FXML
    ImageView userIcon;
    String friendUser = "Doraemon";
    String chatUserName = "Я";

    @FXML
    public void sendMsg() {
        if (!writerArea.getText().isEmpty()) {
            dialog.appendText(chatUserName + ":" + "\n    " + writerArea.getText() + "\n ");
            writerArea.clear();
            writerArea.requestFocus();
        }
    }


    @Override
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        InputStream iconStream =
                getClass().getResourceAsStream("smile.png");
        Image image = new Image(iconStream);
        userIcon.setImage(image);
        userName.setText("Doraemon");
        userList.getItems().addAll(users);
        userList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        dialog.setEditable(false);
        dialog.appendText(friendUser + ":" + "\n    " + "Привет! Как дела?" + "\n ");
        writerArea.focusedProperty();
        writerArea.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode()== KeyCode.ENTER) {
                    sendMsg();
                    writerArea.focusedProperty();
                }
            }
        });


    }
}
