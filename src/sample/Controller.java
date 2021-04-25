package sample;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    public static final String ADDRESS = "127.0.0.1";
    public static final int PORT = 6001;
    private static final int HISTORY_SIZE = 100; //размер загружаемой истории
    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    Insets mainSceneNormalInsets = new Insets(0, 0, 0, 0);
    Insets mainSceneAuthInsets = new Insets(25, 0, 0, 0);
    File file;
    BufferedWriter bw;
    private List<TextArea> textAreas;
    @FXML
    Label name;  //никнэйм
    @FXML
    TextArea generalDialog;
    @FXML
    ListView<String> userList;
    @FXML
    TextField writerArea;
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
            userList.setVisible(false);
            name.setVisible(false);
        } else {
            authLine.setVisible(false);
            writePane.setDisable(false);
            mainScene.setPadding(mainSceneNormalInsets);
            userList.setVisible(true);
            name.setVisible(true);
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
            setAuthorized(false);

            new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        if (!str.isEmpty() && "/end".equals(str)) {
                            break;
                        }
                        if (!str.isEmpty() && str.startsWith("/auth-ok")) {
                            String[] tokens = str.split(" ");
                            setAuthorized(true);
                            generalDialog.clear();
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    name.setText(tokens[1]);
                                }
                            });
                            localHistoryLoad(tokens[1]);  //<- добавил метод загрузки истории из файла
                            break;
                        } else {
                            for (TextArea ta : textAreas) {
                                generalDialog.appendText(str + "\n");
                            }
                        }
                    }
                    bw = new BufferedWriter(new FileWriter(file, true));
                    while (true) {
                        String str = in.readUTF();
                        if (!str.isEmpty() && "/end".equals(str)) {
                            break;
                        } else if (str.startsWith("/history ")) {
                            generalDialog.clear();
                            generalDialog.appendText(str.substring(9, str.length()));
                        } else if (str.startsWith("/updateUL")) {
                            String[] tokens = str.split(" ");
                            Arrays.sort(tokens);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    userList.getItems().clear();
                                    for (int i = 1; i < tokens.length; i++) {
                                        userList.getItems().add(tokens[i]);
                                    }
                                }
                            });
                        } else {
                            generalDialog.appendText(str + "\n");
                            bw.write(str + "\n"); //<- сохраняем историю в файл
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Связь с сервером разорвана");
                } finally {
                    try {
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setAuthorized(false);
                }
            }).start();
        } catch (IOException e) {
            generalDialog.appendText("Нет соединения с сервером\n");
        }
    }

    @FXML
    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            if (socket != null && !loginField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
                out.writeUTF("/auth " + loginField.getText().toLowerCase() + " " + passwordField.getText());
                loginField.clear();
                passwordField.clear();
            }
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Нет соединения с сервером");
        }
    }

    public void disconnect() {
        if (socket != null) {
            if (!socket.isClosed()) {
                try {
                    out.writeUTF("/end");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void selectClient(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            MiniStage ms = new MiniStage(userList.getSelectionModel().getSelectedItem(), out, textAreas);
            ms.setMinWidth(400);
            ms.setMinHeight(100);
            ms.setResizable(false);
            ms.show();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connect();
        setAuthorized(false);
        textAreas = new ArrayList<>();
        textAreas.add(generalDialog);
    }

    public void logUp(ActionEvent actionEvent) {
        RegistrationStage rs = new RegistrationStage(out);
        rs.setMinWidth(400);
        rs.setMinHeight(150);
        rs.setResizable(false);
        rs.show();
    }

    @FXML
    public void offline() {
        try {
            out.writeUTF("/end");
            writerArea.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void localHistoryLoad(String name) {
        file = new File("src/sample/history/" + name + ".txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            ArrayList<String> lines = new ArrayList<>();
            while (true) {
                String line = br.readLine();
                if (line != null) {
                    lines.add(line);
                } else {
                    for (int i = (lines.size() <= HISTORY_SIZE ? 0 : lines.size() - HISTORY_SIZE); i < lines.size(); i++) {
                        generalDialog.appendText(lines.get(i) + "\n");
                    }
                    break;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
