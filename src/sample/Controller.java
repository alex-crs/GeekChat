package sample;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import server.ClientHandler;
import server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.util.*;

public class Controller {

    public static final String ADDRESS = "127.0.0.1";
    public static final int PORT = 6001;
    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    Insets mainSceneNormalInsets = new Insets(0, 0, 0, 0);
    Insets mainSceneAuthInsets = new Insets(25, 0, 0, 0);
    //ObservableList<String> users = FXCollections.observableArrayList();
    @FXML
    TableView<String> userList;
    @FXML
    TextArea dialog;
    @FXML
    TextField writerArea;
    @FXML
    ImageView userIcon;
    @FXML
    SplitPane mainScene;
    @FXML
    PasswordField passwordField;
    @FXML
    TextField loginField;
    @FXML
    HBox authLine;
    @FXML
    AnchorPane writePane;


    private boolean isAuthorized;

    public void setAuthorized(boolean authorized) {
        this.isAuthorized = authorized;

        if (!isAuthorized) {
            authLine.setVisible(true);
            writePane.setDisable(true);
            mainScene.setPadding(mainSceneAuthInsets);
        } else {
            authLine.setVisible(false);
            writePane.setDisable(false);
            mainScene.setPadding(mainSceneNormalInsets);
            userList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
    }


    @FXML
    public void sendMsg() {
        if (!writerArea.getText().isEmpty()) {
            try {
                out.writeUTF(writerArea.getText());
                writerArea.clear();
                writerArea.requestFocus();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void connect() {
        try {
            socket = new Socket(ADDRESS, PORT);

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        if ("/auth-ok".equals(str)) {
                            setAuthorized(true);
                            dialog.clear();
                            break;
                        } else {
                            dialog.appendText(str + "\n");
                        }
                    }
                    while (true) {
                        String str = in.readUTF();
                        if ("/end".equals(str)) {
                            break;
                        }
                        /*if (str.startsWith("/updateUL")) { //пока не работает
                            userListUpdate(str);
                        } else {*/
                            dialog.appendText(str + "\n");
                    }
                } catch (IOException e) {
                    System.out.println("Связь с сервером разорвана");
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setAuthorized(false);
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
            dialog.appendText("Connection refused\n");
        }
    }

    @FXML
    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            out.writeUTF("/end");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private void userListUpdate(String str) { //функционал пока не работает...
        String[] strArray = str.split(" ");
        //пытался добавить сюда и ListView и TableView и даже TreeView:( но со всем одна и та же проблема, список отображается, но удаляются пользователи некорректно...

    }


}
