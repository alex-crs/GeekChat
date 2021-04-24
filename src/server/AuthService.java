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

    public static Connection getConnection() {
        return connection;
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
