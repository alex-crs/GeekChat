package server;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> users; //синхронизированный лист
    private static final Logger LOGGER = Logger.getLogger(Server.class);
    private int PORT = 6001;

    public Server() {
        users = new Vector<>();
        ServerSocket server = null; //мы отправляем
        Socket socket = null; //мы получаем

        try {
            LOGGER.info("Попытка запуска сервера");
            AuthService.connect();
            server = new ServerSocket(PORT);
            LOGGER.info("Сервер запущен, слушает порт: " + PORT);
            while (true) {
                socket = server.accept(); //сервер принимает данные если связь установлена
                //System.out.printf("Клиент пытается подключиться [%s]\n", socket.getInetAddress());
                LOGGER.info(String.format("Клиент [%s] осуществляет подключение к серверу", socket.getInetAddress()));
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.printf("Client %s disconnected", socket.getInetAddress());
                socket.close();
            } catch (IOException e) {
                LOGGER.error("Произошла ошибка:", e);
            }
            try {
                server.close();
            } catch (IOException e) {
                LOGGER.error("Произошла ошибка:", e);
            }
            AuthService.disconnect();
        }

    }

    public void subscribe(ClientHandler client) {
        broadCastMessage(client, "В чате появился " + client.getNickname());
        users.add(client);
        sendUserList();
    }

    public void unSubscribe(ClientHandler client) {
        users.remove(client);
        if (client.getNickname() != null && !client.getNickname().isEmpty()) {
            broadCastMessage(client, "Из чата вышел " + client.getNickname());
            sendUserList();
        }
    }

    public void broadCastMessage(ClientHandler from, String str) {
        for (ClientHandler c : users) {
            if (!c.checkBlackList(from.getNickname().toLowerCase())) {
                c.sendMsg(str);
            }
        }
    }

    private void sendUserList() {
        StringBuilder userListString = new StringBuilder();
        userListString.append("/updateUL");
        for (ClientHandler l : users) {
            userListString.append(" " + l.getNickname());
        }
        String out = userListString.toString();
        for (ClientHandler c : users) {
            c.sendMsg(out);
        }
    }

    public void privateMessage(ClientHandler nickFrom, String nickTo, String message) {
        for (ClientHandler nickBase : users) {
            if (nickTo.equals(nickBase.getNickname()) && !nickBase.checkBlackList(nickFrom.getNickname().toLowerCase())) {
                if (!nickFrom.getNickname().equals(nickTo)) { //нельзя отправлять самому себе (хотя я бы отправлял:) может у меня и друзей то нет
                    nickBase.sendMsg("->[Пришло приватно от " + nickFrom.getNickname() + "]" + message);
                    nickFrom.sendMsg("->"+nickFrom.getNickname() + ": [Отправлено приватно для " + nickTo + "]" + message);
                }
            }
        }

    }

    public boolean isAuthUser(String nick) {
        for (ClientHandler l : users) {
            if (nick.equals(l.getNickname())) {
                return true;
            }
        }
        return false;
    }

}
