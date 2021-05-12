package server;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlackListSQLRequests {
    private static final Logger LOGGER = Logger.getLogger(BlackListSQLRequests.class);

    public static ArrayList<String> getBlackList(String nickname) {
        Connection connection = AuthService.getConnection();
        LOGGER.info(String.format("Клиент [%s] запрашивает содержимое blacklist", nickname));
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT blockedUsers FROM blacklist WHERE nickname LIKE ?"
            );
            ps.setString(1,nickname);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String blacklist = rs.getString("blockedUsers");
                if (blacklist != null) {
                    LOGGER.info(String.format("Содержимое blacklist передано клиенту [%s]", nickname));
                    return new ArrayList<>(Arrays.asList(blacklist.split(" ")));
                }
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            LOGGER.error("Произошла ошибка:", e);
        }
        LOGGER.info(String.format("У клиента [%s] пустой blacklist", nickname));
        return new ArrayList<>();
    }

    public static void blackListSQLSynchronization(String nickname, List<String> blacklistArray) {  //попробовать написать одним запросом
        Connection connection = AuthService.getConnection();
        StringBuilder blockedUsers = new StringBuilder();
        LOGGER.info(String.format("Сохранение содержимого blacklist клиента [%s]", nickname));
        for (String l : blacklistArray) {
            blockedUsers.append(l + " ");
        }
        try {
            PreparedStatement query = connection.prepareStatement(
                    "SELECT blockedUsers FROM blacklist WHERE nickname LIKE ?"
            );
            query.setString(1,nickname);
            ResultSet rs = query.executeQuery();
            if (rs.next()) {
                query = connection.prepareStatement(
                        "UPDATE blacklist SET blockedUsers=? where nickname=?"
                );
                query.setString(1, blockedUsers.toString());
                query.setString(2, nickname);
                query.executeUpdate();
            } else {
                query = connection.prepareStatement(
                        "INSERT INTO blacklist (nickname, blockedUsers) VALUES (?, ?);"
                );
                query.setString(1, nickname);
                query.setString(2, blockedUsers.toString());
                query.executeUpdate();
            }
            LOGGER.info(String.format("Содержимое blacklist клиента [%s] успешно сохранено", nickname));
            query.close();
            rs.close();
        } catch (SQLException e) {
            LOGGER.error("Произошла ошибка:", e);
        }
    }

}
