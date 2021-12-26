package com.example.TelegramBotAliexpress.service.sql.Operation;

import com.example.TelegramBotAliexpress.service.sql.Connecting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class SelectAllAccsFromSQL {
    private static final String SELECT_ACCOUNT_NEW = "SELECT account_login\n" +
            "\tFROM public.accounts_new WHERE user_id = ?";
    private static final String SELECT_ACCOUNT_USE_WITH_ORDER = "SELECT account_login\n" +
            "\tFROM public.accounts_use_with_order WHERE user_id = ?";
    private static final String SELECT_ACCOUNT_USE_WITHOUT_ORDER = "SELECT account_login\n" +
            "\tFROM public.accounts_use_without_order WHERE user_id = ?";
    private static Logger logger = Logger.getLogger("SelectAllAccsFromSQL");
    public static String selectNewAccounts(Long userId) {
        return getAccounts(userId, SELECT_ACCOUNT_NEW);
    }

    public static String selectAccWithOrder(Long userId) {
        return getAccounts(userId, SELECT_ACCOUNT_USE_WITH_ORDER);
    }

    public static String selectAccWithoutOrder(Long userId) {
        return getAccounts(userId, SELECT_ACCOUNT_USE_WITHOUT_ORDER);
    }

    private static String getAccounts(Long userId, String selectAccountNew) {
        try (Connection con = Connecting.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(selectAccountNew);
            stmt.setLong(1, userId);
            ResultSet resultSet = stmt.executeQuery();
            String res = "";
            while (resultSet.next()) {
                res += resultSet.getString("account_login");
                res += "\n";
            }
            logger.info("Выданы все аккаунты");
            return res;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
}
