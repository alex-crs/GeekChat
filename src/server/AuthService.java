package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AuthService {
    private static Connection connection;
    private static Statement statement;

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");  //регистрация драйвера-менеджера
            connection = DriverManager.getConnection("jdbc:sqlite:main.db"); //подключаем базу данных
            statement = connection.createStatement(); //создаем состояние
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static int addUser(String login, String pass, String nickname) { //добавление пользователя в базу
        try {
            String query = "INSERT INTO users (login, password, nickname) VALUES (?, ?, ?);";
            PreparedStatement ps = connection.prepareStatement(query); //запрос на изменение данных в SQL
            ps.setString(1, login);
            ps.setInt(2, pass.hashCode());
            ps.setString(3, nickname);
            return ps.executeUpdate(); //проверяет сколько строк было изменено в базе данных, если ни одной то возвращает соответственно 0
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static ArrayList<String> getBlackList(String nickname) {
        String query = String.format("select blacklist from users where nickname='%s'", nickname); //запрос содержимого черного списка

        try {
            ResultSet rs = statement.executeQuery(query);
            if (rs.next()) {
                if (rs.getString(1) != null) {
                    String blacklist = rs.getString(1);
                    return new ArrayList<>(Arrays.asList(blacklist.split(" ")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static String getHistory(String nickname) {
        String query = String.format("select chatHistory from messageHistory where nickname='%s'", nickname); //запрос истории

        try {
            ResultSet rs = statement.executeQuery(query);
            if (rs.next()) {
                if (rs.getString(1) != null) {

                    return rs.getString(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void historySaveToSQL(String nickname, String chatHistory) {
        String queryUpdate = "UPDATE messageHistory SET chatHistory=? where nickname=?";
        String queryAdd = "INSERT INTO messageHistory (nickname, chatHistory) VALUES (?, ?);";
        String querySelect = String.format("select chatHistory from messageHistory where nickname='%s'", nickname);

        try {
            ResultSet rsSelect = statement.executeQuery(querySelect);
            if (rsSelect.next()) {
                PreparedStatement rsUpdate = connection.prepareStatement(queryUpdate);
                rsUpdate.setString(1, chatHistory);
                rsUpdate.setString(2, nickname);
                rsUpdate.executeUpdate();
            } else {
                PreparedStatement rsAdd = connection.prepareStatement(queryAdd);
                rsAdd.setString(1,nickname);
                rsAdd.setString(2,chatHistory);
                rsAdd.executeUpdate();
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void blackListSQLSynchronization(String nick, List<String> blacklistArray) {
        String query = "UPDATE users SET blacklist=? where nickname=?"; //UPDATE users SET blacklist='nick1' where nickname='nick2'
        StringBuilder blacklist = new StringBuilder();
        for (String l : blacklistArray) {
            blacklist.append(l + " ");
        }
        try {
            PreparedStatement rs = connection.prepareStatement(query);
            rs.setString(1, blacklist.toString());
            rs.setString(2, nick);
            rs.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getNickNameByLoginAndPassword(String login, String password) {
        String query = String.format("select nickname, password from users where login='%s'", login);

        try {
            ResultSet rs = statement.executeQuery(query);
            int myHash = password.hashCode();

            if (rs.next()) {
                String nick = rs.getString(1);
                //изменяем тип поля PASSWORD на INTEGER в бд
                int dbHash = rs.getInt(2);
                if (myHash == dbHash) {
                    return nick;
                }
                //return rs.getString("nickname"); //нумерация начинается с 1 а не с 0 как обычно
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
