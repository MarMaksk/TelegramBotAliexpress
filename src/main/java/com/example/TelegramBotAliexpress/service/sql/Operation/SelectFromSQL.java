package com.example.TelegramBotAliexpress.service.sql.Operation;

import com.example.TelegramBotAliexpress.service.entity.Account;
import com.example.TelegramBotAliexpress.service.sql.Connecting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class SelectFromSQL {
    private static final String SELECT_ACCOUNT_NEW_ONE = "SELECT account_login\n" +
            "\tFROM public.accounts_new WHERE user_id = ? LIMIT 1";
    private static final String SELECT_ACCOUNT_NEW_TWO = "SELECT account_login\n" +
            "\tFROM public.accounts_new WHERE user_id = ? LIMIT 2";
    private static final String SELECT_ACCOUNT_USE_WITH_ORDER_TWO = "SELECT account_login\n" +
            "\tFROM public.accounts_use_with_order WHERE user_id = ? AND last_use < now() - '1 days' :: interval LIMIT 2";
    private static final String SELECT_ACCOUNT_USE_WITH_ORDER_FIVE = "SELECT account_login\n" +
            "\tFROM public.accounts_use_with_order WHERE user_id = ? AND last_use < now() - '1 days' :: interval LIMIT 5";
    private static final String SELECT_ACCOUNT_USE_WITHOUT_ORDER_TWO = "SELECT account_login\n" +
            "\tFROM public.accounts_use_without_order WHERE user_id = ?" +
            "AND last_use < now() - '1 days' :: interval LIMIT 2";
    private static final String SELECT_ACCOUNT_USE_WITHOUT_ORDER_FIVE = "SELECT account_login\n" +
            "\tFROM public.accounts_use_without_order WHERE user_id = ?" +
            "AND last_use < now() - '1 days' :: interval LIMIT 5";
    private static final String SELECT_ACCOUNT_USE_WITHOUT_ORDER_FOR_CENT = "SELECT account_login, last_use\n" +
            "\tFROM public.accounts_use_without_order WHERE user_id = ? AND cent_use = false AND last_use < now() - '1 days' :: interval LIMIT 1";
    private static final String SELECT_ACCOUNT_USE_WITH_ORDER_FOR_CENT = "SELECT account_login, last_use\n" +
            "\tFROM public.accounts_use_with_order WHERE user_id = ? AND cent_use = false AND last_use < now() - '1 days' :: interval LIMIT 1";
    private static final String SELECT_ACCOUNT_USE_FOR_ORDER = "SELECT account_login, last_use\n" +
            "\tFROM public.accounts_use_without_order WHERE user_id = ? LIMIT 1";
    private static Logger logger = Logger.getLogger("SelectFromSQL");

    public static List<Account> selectNewAccounts(Long userId, boolean one) {
        try (Connection con = Connecting.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(one ? SELECT_ACCOUNT_NEW_ONE : SELECT_ACCOUNT_NEW_TWO);
            stmt.setLong(1, userId);
            ResultSet resultSet = stmt.executeQuery();
            List<Account> account = new LinkedList<>();
            while (resultSet.next()) {
                account.add(new Account(userId, resultSet.getString("account_login"),
                        LocalDateTime.now()));
            }
            if (!account.isEmpty()) {
                account.forEach(InsertToSQL::addUseAccWithoutOrder);
                account.forEach(acc -> DeleteFromSQL.removeNewAccount(acc.getLogin()));
                logger.info("Выдан новый аккаунт");
            }
            return account;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static List<Account> selectAccWithOrder(Long userId, boolean five) {
        try (Connection con = Connecting.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(five ?
                    SELECT_ACCOUNT_USE_WITH_ORDER_FIVE : SELECT_ACCOUNT_USE_WITH_ORDER_TWO);
            stmt.setLong(1, userId);
            ResultSet resultSet = stmt.executeQuery();
            List<Account> account = new LinkedList<>();
            while (resultSet.next()) {
                account.add(new Account(userId, resultSet.getString("account_login")));
                account.forEach(acc -> {
                    UpdateToSQL.updateWithOrder(acc, false);
                });
            }
            if (account.isEmpty()) {
                account = selectAccWithoutOrder(userId, five ?
                                SELECT_ACCOUNT_USE_WITHOUT_ORDER_FIVE : SELECT_ACCOUNT_USE_WITHOUT_ORDER_TWO
                        , false, false);
            }
            logger.info("Выдан аккаунт с заказом");
            return account;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static List<Account> selectAccountForOrder(Long userId, Boolean delete, Boolean cent) {
        return selectAccWithoutOrder(userId, SELECT_ACCOUNT_USE_FOR_ORDER, delete, cent);
    }

    private static List<Account> selectAccWithoutOrder(Long userId, String sql, Boolean delete, Boolean cent) {
        List<Account> accountList = new LinkedList<>();
        try (Connection con = Connecting.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(cent ? SELECT_ACCOUNT_USE_WITHOUT_ORDER_FOR_CENT : sql);
            stmt.setLong(1, userId);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                accountList.add(new Account(userId, resultSet.getString("account_login"),
                        resultSet.getTimestamp("last_use").toLocalDateTime()));
            }
            if (accountList.isEmpty() && cent) {
                accountList = selectAccWithoutOrder(userId, SELECT_ACCOUNT_USE_WITH_ORDER_FOR_CENT, false, false);
            }
            for (Account acc : accountList) {
                if (delete) {
                    DeleteFromSQL.removeAccWithoutOrder(acc.getLogin());
                    InsertToSQL.addUseAccWithOrder(acc);
                } else {
                    if (sql.equals(SELECT_ACCOUNT_USE_WITH_ORDER_FOR_CENT))
                        UpdateToSQL.updateWithOrder(acc, true);
//                    else if (sql.equals(SELECT_ACCOUNT_USE_WITHOUT_ORDER_FOR_CENT))
//                        UpdateToSQL.updateWithoutOrderForCent(acc);
                    else
                        UpdateToSQL.updateWithoutOrder(acc, cent);
                }
            }
            logger.info("Выдан аккаунт без заказов");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return accountList;
    }

}
