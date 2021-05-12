package server;

import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private ExecutorService threadManager;
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class);


    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            chatHistory = new StringBuilder();
            this.timer = System.currentTimeMillis();
            this.threadManager = Executors.newFixedThreadPool(2);
            Thread timeOutThread = new Thread(() -> {
                while (true) {
                    long timerEnd = System.currentTimeMillis() - timer;
                    if (!this.socket.isClosed() && nickname == null && timerEnd > 120000) {
                        try {
                            LOGGER.info(String.format("Клиент [%s] отключен по таймауту", socket.getInetAddress()));
                            this.socket.close();
                            break;
                        } catch (IOException e) {
                            LOGGER.error("Произошла ошибка:", e);
                        }
                    } else if (nickname != null) {
                        break;
                    }
                }
            });

            Thread mainClientHandlerThread = new Thread(() -> {
                try {
                    //auth -/auth login pass
                    boolean isExit = false;
                    while (true) {
                        String str = in.readUTF();
                        if (!str.isEmpty() && str.startsWith("/auth ")) {
                            LOGGER.info(socket.getInetAddress()+" пытается авторизоваться на сервере");
                            String[] tokens = str.split(" "); //делит и добавляет в массив по регулярке пробел
                            String nick = AuthService.getNickNameByLoginAndPassword(tokens[1], tokens[2]);
                            if (nick != null) {
                                if (!server.isAuthUser(nick)) {
                                    setNickName(nick);
                                    sendMsg("/auth-ok " + nickname);
                                    server.subscribe(ClientHandler.this);
                                    LOGGER.info(String.format("%s авторизован на сервере", nickname));
                                    blacklist = BlackListSQLRequests.getBlackList(nickname);
                                    break;
                                } else {
                                    LOGGER.info(String.format("%s уже в системе, в доступе отказано", tokens[1]));
                                    sendMsg("Учетная запись уже используется");
                                }
                            } else {
                                LOGGER.info(String.format("%s ввел неверные учетные данные", socket.getInetAddress()));
                                sendMsg("Неверный логин/пароль");
                            }
                        }
                        //Регистрация пользователя
                        if (!str.isEmpty() && str.startsWith("/signup ")) {
                            String[] tokens = str.split(" ");
                            int result = AuthService.addUser(tokens[1], tokens[2], tokens[3]);
                            if (result > 0) {
                                LOGGER.info(String.format("%s успешно зарегистрировался в системе", tokens[1]));
                                sendMsg("Успешная регистрация");
                            } else {
                                LOGGER.info(String.format("%s в регистрации отказано", tokens[1]));
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
                                LOGGER.debug(String.format("Синхронизация истории пользователя %s с базой данных", nickname));
                                HistorySQLRequests.historySaveToSQL(nickname, chatHistory.toString());
                                LOGGER.debug(String.format("Синхронизация истории пользователя %s с базой данных успешно завершена", nickname));
                                out.writeUTF("Отключен от сервера");
                                LOGGER.info(String.format("%s отключился от сервера", nickname));
                                break;
                            } else if (str.startsWith("@")) {
                                String[] tokens = str.split(" ", 2);
                                String nick = tokens[0].substring(1);
                                server.privateMessage(this, nick, tokens[1]);
                                LOGGER.info(String.format("%s отправил приватное сообщение %s", nickname, nick));
                            } else if (str.startsWith("/blacklist ")) {
                                String[] tokens = str.split(" ");
                                blacklist.add(tokens[1].toLowerCase());
                                sendMsg("You added " + tokens[1] + " to blacklist");
                                BlackListSQLRequests.blackListSQLSynchronization(nickname, blacklist);
                                LOGGER.info(String.format("%s добавил в черный список %s", nickname, tokens[1]));
                            } else if ("/getHistory".equals(str)) {
                                sendMsg("/history " + HistorySQLRequests.getHistory(nickname));
                                LOGGER.info(String.format("%s загрузил историю из базы данных", nickname));
                            } else {
                                server.broadCastMessage(this, nickname + ": " + str);
                                LOGGER.info(String.format("%s отправил broadcast сообщение", nickname));
                            }
                        }
                    }
                } catch (SocketException e) {
                    LOGGER.debug(String.format("Соединение с %s сброшено", socket.getInetAddress()));
                } catch (IOException e) {
                    LOGGER.error("Произошла ошибка:", e);
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        LOGGER.debug("Произошла ошибка:", e);
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        LOGGER.debug("Произошла ошибка:", e);
                    }
                    try {
                        LOGGER.info(String.format("%s удален из базы ClientHandler", socket.getInetAddress()));
                        socket.close();
                    } catch (IOException e) {
                        LOGGER.debug("Произошла ошибка:", e);
                    }
                    server.unSubscribe(this);
                }
            });
            threadManager.execute(timeOutThread);
            threadManager.execute(mainClientHandlerThread);
        } catch (IOException e) {
            LOGGER.error("Произошла ошибка:", e);
        }
        threadManager.shutdown();
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
            LOGGER.error("Произошла ошибка:", e);
        }
    }

    public boolean checkBlackList(String nickname) {
        return blacklist.contains(nickname);
    }
}
