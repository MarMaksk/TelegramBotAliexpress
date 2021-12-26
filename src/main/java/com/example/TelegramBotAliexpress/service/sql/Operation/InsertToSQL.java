package com.example.TelegramBotAliexpress.service.sql.Operation;

import com.example.TelegramBotAliexpress.service.entity.Account;
import com.example.TelegramBotAliexpress.service.sql.Connecting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class InsertToSQL {
    private static final String INSERT_ACCOUNT_NEW = "INSERT INTO public.accounts_new(\n" +
            "\tuser_id, account_login)\n" +
            "\tVALUES (?, ?);";
    private static final String INSERT_ACCOUNT_USE_WITH_ORDER = "INSERT INTO public.accounts_use_with_order(\n" +
            "\tuser_id, account_login, last_use)\n" +
            "\tVALUES (?, ?, ?);";
    private static final String INSERT_ACCOUNT_USE_WITHOUT_ORDER = "INSERT INTO public.accounts_use_without_order(\n" +
            "\tuser_id, account_login, last_use, cent_use)\n" +
            "\tVALUES (?, ?, ?, ?);";
    private static Logger logger = Logger.getLogger("InsertToSQL");

    public static void addNewAccounts(Account account) {
        try (Connection con = Connecting.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(INSERT_ACCOUNT_NEW);
            con.setAutoCommit(false);
            try {
                stmt.setLong(1, account.getIdUser());
                stmt.setString(2, account.getLogin());
                stmt.executeUpdate();
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
            logger.info("Добавлен новый аккаунт");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void addUseAccWithOrder(Account account) {
        additionToTable(account, INSERT_ACCOUNT_USE_WITH_ORDER);
        logger.info("Добавлен аккаунт с заказом");
    }

    public static void addUseAccWithoutOrder(Account account) {
        additionToTable(account, INSERT_ACCOUNT_USE_WITHOUT_ORDER);
        logger.info("Добавлен аккаунт без заказа");
    }

    private static void additionToTable(Account account, String insertAccount) {
        try (Connection con = Connecting.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(insertAccount);
            con.setAutoCommit(false);
            try {
                stmt.setLong(1, account.getIdUser());
                stmt.setString(2, account.getLogin());
                stmt.setObject(3, account.getLastUse());
                if (insertAccount.equals(INSERT_ACCOUNT_USE_WITHOUT_ORDER))
                    stmt.setBoolean(4, false);
                stmt.executeUpdate();
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

}
