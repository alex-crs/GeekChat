package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> users; //синхронизированный лист

    public Server() {
        users = new Vector<>();
        ServerSocket server = null; //мы отправляем
        Socket socket = null; //мы получаем

        try {
            AuthService.connect();
            server = new ServerSocket(6001);
            System.out.println("Server start");

            //System.out.println("Result from DB: " + AuthService.getNickNameByLoginAndPassword("login1","pass1"));


            while (true) {
                socket = server.accept(); //сервер принимает данные
                System.out.printf("Client [%s] connected\n", socket.getInetAddress());
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }

    }

    public void subscribe(ClientHandler client) {
        broadCastMessage("В чате появился " + client.getNickname());
        users.add(client);
        //broadCastMessage(sendUserList());
    }

    public void unSubscribe(ClientHandler client) {
        users.remove(client);
        broadCastMessage("Из чата вышел " + client.getNickname());
        //broadCastMessage(sendUserList());

    }

    public void broadCastMessage(String str) {
        for (ClientHandler c : users) {
            c.sendMsg(str);
        }
    }

    private String sendUserList() { //пока работает некорректно( посмотрю на уроке как вы сделали
        String usersString = "/updateUL";
        for (ClientHandler l : users) {
            usersString = usersString + " " + l.getNickname();
        }
        return usersString;
    }

    public void privateMessage(ClientHandler client, String nick, String text) {
        for (ClientHandler c : users) {
            if (nick.equals(c.getNickname())) {
                c.sendMsg(client.getNickname() + " [Отправлено для " + nick + "]" + text);
            }
        }
        client.sendMsg(client.getNickname() + " [Отправлено для " + nick + "]" + text);
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
