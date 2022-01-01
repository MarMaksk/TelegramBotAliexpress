package com.example.TelegramBotAliexpress.service.sql.Operation;

import com.example.TelegramBotAliexpress.enums.BotState;
import com.example.TelegramBotAliexpress.service.entity.Account;
import com.example.TelegramBotAliexpress.service.entity.TelegramUser;
import com.example.TelegramBotAliexpress.service.sql.Connecting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class InsertToSQL {
    private static final String INSERT_ACCOUNT_NEW = "INSERT INTO public.accounts_new(\n" +
            "\tuser_id, account_login)\n" +
            "\tVALUES (?, ?);";
    private static final String INSERT_ACCOUNT_USE_WITH_ORDER = "INSERT INTO public.accounts_use_with_order(\n" +
            "\tuser_id, account_login, last_use, cent_use)\n" +
            "\tVALUES (?, ?, ?, ?);";
    private static final String INSERT_ACCOUNT_USE_WITHOUT_ORDER = "INSERT INTO public.accounts_use_without_order(\n" +
            "\tuser_id, account_login, last_use, cent_use)\n" +
            "\tVALUES (?, ?, ?, ?);";
    private static Logger logger = Logger.getLogger("InsertToSQL");

    public static long addNewAccounts(List<Account> account) {
        long count = 0;
        try (Connection con = Connecting.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(INSERT_ACCOUNT_NEW);
            con.setAutoCommit(false);
            try {
                for (Account acc : account) {
                    stmt.setLong(1, acc.getIdUser());
                    stmt.setString(2, acc.getLogin());
                    stmt.addBatch();
                }
                int[] ints = stmt.executeBatch();
                count = Arrays.stream(ints).filter(res -> res == 1).count();
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
            logger.info("Добавлен новый аккаунт");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return count;
    }

    public static long addUseAccWithOrder(List<Account> accountList) {
        logger.info("Добавлен аккаунт с заказом");
        return additionToTable(accountList, INSERT_ACCOUNT_USE_WITH_ORDER);
    }

    public static long addUseAccWithoutOrder(List<Account> accountList) {
        logger.info("Добавлен аккаунт без заказа");
        return additionToTable(accountList, INSERT_ACCOUNT_USE_WITHOUT_ORDER);
    }

    private static long additionToTable(List<Account> accountList, String insertAccount) {
        long count = 0;
        try (Connection con = Connecting.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(insertAccount);
            con.setAutoCommit(false);
            try {
                for (Account account : accountList) {
                    stmt.setLong(1, account.getIdUser());
                    stmt.setString(2, account.getLogin());
                    stmt.setObject(3, account.getLastUse());
                    stmt.setBoolean(4, account.isCentUse());
                    stmt.addBatch();
                }
                int[] ints = stmt.executeBatch();
                count = Arrays.stream(ints).filter(res -> res == 1).count();
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return count;
    }

}
