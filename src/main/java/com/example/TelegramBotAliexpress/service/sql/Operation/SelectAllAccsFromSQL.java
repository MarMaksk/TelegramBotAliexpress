package com.example.TelegramBotAliexpress.service.sql.Operation;

import com.example.TelegramBotAliexpress.service.entity.Account;
import com.example.TelegramBotAliexpress.service.sql.Connecting;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class SelectAllAccsFromSQL {
    private static final String SELECT_ACCOUNT_NEW = "SELECT account_login\n" +
            "\tFROM public.accounts_new WHERE user_id = ?";
    private static final String SELECT_ACCOUNT_USE_WITH_ORDER = "SELECT account_login, last_use, cent_use\n" +
            "\tFROM public.accounts_use_with_order WHERE user_id = ?";
    private static final String SELECT_ACCOUNT_USE_WITHOUT_ORDER = "SELECT account_login, last_use, cent_use\n" +
            "\tFROM public.accounts_use_without_order WHERE user_id = ?";
    private static final Logger logger = Logger.getLogger("SelectAllAccsFromSQL");

    public static List<Account> selectNewAccounts(Long userId) {
        return getAccounts(userId, SELECT_ACCOUNT_NEW);
    }

    public static List<Account> selectAccWithOrder(Long userId) {
        return getAccounts(userId, SELECT_ACCOUNT_USE_WITH_ORDER);
    }

    public static List<Account> selectAccWithoutOrder(Long userId) {
        return getAccounts(userId, SELECT_ACCOUNT_USE_WITHOUT_ORDER);
    }

    private static List<Account> getAccounts(Long userId, String sql) {
        List<Account> accountList = new LinkedList<>();
        try (Connection con = Connecting.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setLong(1, userId);
            ResultSet resultSet = stmt.executeQuery();
            StringBuilder res = new StringBuilder();
            while (resultSet.next()) {
                accountList.add(new Account(userId,
                        resultSet.getString("account_login"),
                        sql.equals(SELECT_ACCOUNT_NEW) ?
                                LocalDateTime.now().minusWeeks(1)
                                :
                                resultSet.getTimestamp("last_use").toLocalDateTime(),
                        !sql.equals(SELECT_ACCOUNT_NEW) && resultSet.getBoolean("cent_use")));
            }
            logger.info("Выданы все аккаунты");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return accountList;
    }
}
