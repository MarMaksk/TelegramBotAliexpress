package com.example.TelegramBotAliexpress.service.sql.Operation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import static com.example.TelegramBotAliexpress.service.sql.Connecting.*;

public class DeleteFromSQL {
    private static final String DELETE_ACC_NEW = "DELETE FROM public.accounts_new\n" +
            "    WHERE account_login = ?";
    private static final String DELETE_ACC_WITH_ORDER = "DELETE FROM public.accounts_use_with_order\n" +
            "    WHERE account_login = ?";
    private static final String DELETE_ACC_WITHOUT_ORDER = "DELETE FROM public.accounts_use_without_order\n" +
            "    WHERE account_login = ?";
    private static final String DELETE_ACC_NEW_USER = "DELETE FROM public.accounts_new\n" +
            "    WHERE user_id = ?";
    private static final String DELETE_ACC_WITH_ORDER_USER = "DELETE FROM public.accounts_use_with_order\n" +
            "    WHERE user_id = ?";
    private static final String DELETE_ACC_WITHOUT_ORDER_USER = "DELETE FROM public.accounts_use_without_order\n" +
            "    WHERE user_id = ?";
    private static Logger logger = Logger.getLogger("DeleteFromSQL");

    public static void removeNewAccount(String login) {
        remove(login, getConnection(), DELETE_ACC_NEW);
        logger.info("Удалён новый аккаунт");
    }

    public static void removeAccWithOrder(String login) {
        remove(login, getConnection(), DELETE_ACC_WITH_ORDER);
        logger.info("Удалён аккаунт с заказом");
    }

    public static void removeAccWithoutOrder(String login) {
        remove(login, getConnection(), DELETE_ACC_WITHOUT_ORDER);
        logger.info("Удалён аккаунт без заказа");
    }

    public static void removeNewAccountUser(long userId) {
        removeAll(userId, getConnection(), DELETE_ACC_NEW_USER);
        logger.info("Удалёны все новый аккаунты");
    }

    public static void removeAccWithOrderUser(long userId) {
        removeAll(userId, getConnection(), DELETE_ACC_WITH_ORDER_USER);
        logger.info("Удалёны все аккаунты с заказом");
    }

    public static void removeAccWithoutOrderUser(long userId) {
        removeAll(userId, getConnection(), DELETE_ACC_WITHOUT_ORDER_USER);
        logger.info("Удалёны все аккаунты без заказа");
    }

    private static void removeAll(long userId, Connection connection, String deleteAcc) {
        try (Connection con = connection) {
            PreparedStatement stmt = con.prepareStatement(deleteAcc);
            stmt.setLong(1, userId);
            stmt.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private static void remove(String login, Connection connection, String deleteAcc) {
        try (Connection con = connection) {
            PreparedStatement stmt = con.prepareStatement(deleteAcc);
            stmt.setString(1, login);
            stmt.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}

