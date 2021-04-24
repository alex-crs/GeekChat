package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HistorySQLRequests {

    public static String getHistory(String nickname) {
        Connection connection = AuthService.getConnection();
        StringBuilder stringOut = new StringBuilder();
        try {
            PreparedStatement query = connection.prepareStatement(
                    "SELECT chatHistory FROM messageHistory WHERE nickname LIKE ?"
            );
            query.setString(1, nickname);
            ResultSet rs = query.executeQuery();
            if (rs.next()) {
                String queryResult = rs.getString("chatHistory");
                if (!queryResult.isEmpty()) {
                    String[] str = queryResult.split("\n");
                    for (int i = (str.length <= 100 ? 0 : str.length - 100); i < str.length; i++) {  //не забыть поставить 100
                        stringOut.append(str[i] + "\n");
                    }
                    return stringOut.toString();
                }
            }
            rs.close();
            query.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void historySaveToSQL(String nickname, String chatHistory) {
        Connection connection = AuthService.getConnection();
        StringBuilder stringToSQL = new StringBuilder(); //составляем строку для загрузки в базу
        try {
            PreparedStatement query = connection.prepareStatement(
                    "SELECT chatHistory FROM messageHistory WHERE nickname LIKE ?"
            );
            query.setString(1, nickname);
            ResultSet rs = query.executeQuery();
            if (rs.next()) {  //если запрос положительный
                String queryResult = rs.getString("chatHistory");
                stringToSQL.append(queryResult.length() == 0 ? chatHistory : queryResult + "\n" + chatHistory); //получаем текущие данные из базы и добавляем к ним новые строчки
                query = connection.prepareStatement(
                        "UPDATE messageHistory SET chatHistory=? where nickname=?"
                );
                query.setString(1, stringToSQL.toString()); //обновляем данные в базе
                query.setString(2, nickname);
                query.executeUpdate();
            } else {
                query = connection.prepareStatement(
                        "INSERT INTO messageHistory (nickname, chatHistory) VALUES (?, ?);"
                );
                query.setString(1, nickname);  //если отсутствуют данные в базе, то создаем их
                query.setString(2, chatHistory);  //записываем данные
                query.executeUpdate();
            }
            query.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
