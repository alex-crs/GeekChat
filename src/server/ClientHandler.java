package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //auth -/auth login pass

                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/auth ")) {
                            String[] tokens = str.split(" "); //делит и добавляет в массив по регулярке пробел
                            String nick = AuthService.getNickNameByLoginAndPassword(tokens[1], tokens[2]);
                            if (nick != null && !server.isAuthUser(nick)) {
                                sendMsg("/auth-ok");
                                setNickName(nick);
                                server.subscribe(ClientHandler.this);
                                break;
                            } else {
                                sendMsg("/Wrong login/password or User is already online");
                            }
                        }
                    }
                    while (true) {
                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            out.writeUTF("server closed");
                            System.out.printf("Client [$s] disconnected\n", socket.getInetAddress());
                            break;
                        } else if (str.startsWith("@")) {
                            String[] tokens = str.split(" ");
                            String nick = tokens[0].substring(1);
                            server.privateMessage(ClientHandler.this, nick, str.substring(nick.length()+1));

                        } else {
                            System.out.printf("Client [%s] - %s\n", socket.getInetAddress(), str);
                            server.broadCastMessage(nickname + ": " + str);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
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
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
