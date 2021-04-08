package server;

import java.sql.*;

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

    public static String getNickNameByLoginAndPassword(String login, String password) {
        String query = String.format("select nickname from users where login='%s' and password='%s'", login, password);
        try {
            ResultSet rs = statement.executeQuery(query);
            if (rs.next()){
                return rs.getString("nickname"); //нумерация начинается с 1 а не с 0 как обычно
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
