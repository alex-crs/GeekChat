package server;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HistorySQLRequests {
    private static final Logger LOGGER = Logger.getLogger(HistorySQLRequests.class);

    public static String getHistory(String nickname) {
        Connection connection = AuthService.getConnection();
        StringBuilder stringOut = new StringBuilder();
        LOGGER.info(String.format("Клиент [%s] запрашивает историю чата", nickname));
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
                    for (int i = (str.length <= 100 ? 0 : str.length - 100); i < str.length; i++) {
                        stringOut.append(str[i] + "\n");
                    }
                    LOGGER.info(String.format("История передана клиенту [%s]", nickname));
                    return stringOut.toString();
                }
            }
            rs.close();
            query.close();
        } catch (SQLException e) {
            LOGGER.error("Произошла ошибка:", e);
        }
        LOGGER.info(String.format("У клиента [%s] отсутствует доступная история", nickname));
        return "";
    }

    public static void historySaveToSQL(String nickname, String chatHistory) {
        Connection connection = AuthService.getConnection();
        StringBuilder stringToSQL = new StringBuilder(); //составляем строку для загрузки в базу
        LOGGER.info(String.format("Сохранение истории клиента [%s]", nickname));
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
            LOGGER.info(String.format("История клиента [%s] успешно сохранена", nickname));
            query.close();
            rs.close();
        } catch (SQLException e) {
            LOGGER.error("Произошла ошибка:", e);
        }
    }
}
