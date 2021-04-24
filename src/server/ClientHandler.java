package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;
    private long timer;
    //черный список у пользователя
    private List<String> blacklist;
    private StringBuilder chatHistory;


    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            chatHistory = new StringBuilder();
            this.timer = System.currentTimeMillis();
            new Thread(() -> {
                while (true) {
                    long timerEnd = System.currentTimeMillis() - timer;
                    if (!this.socket.isClosed() && nickname == null && timerEnd > 50000) {
                        try {
                            this.socket.close();
                            break;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (nickname != null) {
                        break;
                    }
                }
            }).start();

            new Thread(() -> {
                try {
                    //auth -/auth login pass
                    boolean isExit = false;
                    while (true) {
                        String str = in.readUTF();
                        if (!str.isEmpty() && str.startsWith("/auth ")) {
                            String[] tokens = str.split(" "); //делит и добавляет в массив по регулярке пробел
                            String nick = AuthService.getNickNameByLoginAndPassword(tokens[1], tokens[2]);
                            if (nick != null) {
                                if (!server.isAuthUser(nick)) {
                                    setNickName(nick);
                                    sendMsg("/auth-ok " + nickname);
                                    server.subscribe(ClientHandler.this);
                                    blacklist = BlackListSQLRequests.getBlackList(nickname);
                                    sendMsg("/history " + HistorySQLRequests.getHistory(nickname));
                                    break;
                                } else {
                                    sendMsg("Учетная запись уже используется");
                                }
                            } else {
                                sendMsg("Неверный логин/пароль");
                            }
                        }
                        //Регистрация пользователя
                        if (!str.isEmpty() && str.startsWith("/signup ")) {
                            String[] tokens = str.split(" ");
                            int result = AuthService.addUser(tokens[1], tokens[2], tokens[3]);
                            if (result > 0) {
                                sendMsg("Успешная регистрация");
                            } else {
                                sendMsg("В регистрации отказано");
                            }
                        }
                        if (!str.isEmpty() && "/end".equals(str)) {
                            isExit = true;
                            break;
                        }
                    }
                    if (!isExit) {
                        while (true) {    //переписать на switch case и объединить в один блок
                            String str = in.readUTF();
                            if (!str.isEmpty() && str.equals("/end")) {
                                isExit = true;
                                HistorySQLRequests.historySaveToSQL(nickname, chatHistory.toString());
                                out.writeUTF("server closed");
                                System.out.printf("Client [$s] disconnected\n", socket.getInetAddress());
                                break;
                            } else if (str.startsWith("@")) {
                                String[] tokens = str.split(" ", 2); //можно поставить например 2 и тогда будет делиться на два элемента массива
                                String nick = tokens[0].substring(1);
                                server.privateMessage(this, tokens[0].substring(1), tokens[1]);

                            } else if (str.startsWith("/blacklist ")) {
                                String[] tokens = str.split(" ");
                                blacklist.add(tokens[1].toLowerCase());
                                sendMsg("You added " + tokens[1] + " to blacklist");
                                BlackListSQLRequests.blackListSQLSynchronization(nickname, blacklist);
                            } else {
                                server.broadCastMessage(this, nickname + ": " + str);
                            }
                        }
                    }
                } catch (SocketException e) {
                    System.out.println("Соединение сброшено");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    server.unSubscribe(this);
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setNickName(String nick) {
        this.nickname = nick;
    }

    public String getNickname() {
        return nickname;
    }

    public void sendMsg(String msg) {
        try {
            if (nickname != null && !msg.startsWith("/") && !msg.startsWith("@")) {
                chatHistory.append(msg + "\n");
            }
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkBlackList(String nickname) {
        return blacklist.contains(nickname);
    }
}
