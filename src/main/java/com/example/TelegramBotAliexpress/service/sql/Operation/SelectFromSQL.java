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
    private static final String SELECT_ACCOUNT_USE_WITH_ORDER_TWO = "SELECT account_login, cent_use\n" +
            "\tFROM public.accounts_use_with_order WHERE user_id = ? AND last_use < now() - '1 days' :: interval LIMIT 2";
    private static final String SELECT_ACCOUNT_USE_WITH_ORDER_FIVE = "SELECT account_login, cent_use\n" +
            "\tFROM public.accounts_use_with_order WHERE user_id = ? AND last_use < now() - '1 days' :: interval LIMIT 5";
    private static final String SELECT_ACCOUNT_USE_WITHOUT_ORDER_TWO = "SELECT account_login, cent_use\n" +
            "\tFROM public.accounts_use_without_order WHERE user_id = ?" +
            "AND last_use < now() - '1 days' :: interval LIMIT 2";
    private static final String SELECT_ACCOUNT_USE_WITHOUT_ORDER_FIVE = "SELECT account_login, cent_use\n" +
            "\tFROM public.accounts_use_without_order WHERE user_id = ?" +
            "AND last_use < now() - '1 days' :: interval LIMIT 5";
    private static final String SELECT_ACCOUNT_USE_WITHOUT_ORDER_FOR_CENT = "SELECT account_login, last_use\n" +
            "\tFROM public.accounts_use_without_order WHERE user_id = ? AND cent_use = false AND last_use < now() - '1 days' :: interval LIMIT 1";
    private static final String SELECT_ACCOUNT_USE_WITH_ORDER_FOR_CENT = "SELECT account_login, last_use\n" +
            "\tFROM public.accounts_use_with_order WHERE user_id = ? AND cent_use = false AND last_use < now() - '1 days' :: interval LIMIT 1";
    private static final String SELECT_ACCOUNT_USE_FOR_ORDER = "SELECT account_login, last_use, cent_use\n" +
            "\tFROM public.accounts_use_without_order WHERE user_id = ? LIMIT 1";
    private static Logger logger = Logger.getLogger("SelectFromSQL");

    public static List<Account> selectNewAccounts(Long userId, boolean centUse) {
        // ЕСЛИ centUse = TRUE ТО АККАУНТ ВЫДАЁТСЯ 1
        // А ТАКЖЕ ОН СЧИТАЕТСЯ АККАУНТОМ КОТОРЫЙ СБИВАЛ ЗА ЦЕНТ
        try (Connection con = Connecting.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(centUse ? SELECT_ACCOUNT_NEW_ONE : SELECT_ACCOUNT_NEW_TWO);
            stmt.setLong(1, userId);
            ResultSet resultSet = stmt.executeQuery();
            List<Account> account = new LinkedList<>();
            while (resultSet.next()) {
                account.add(new Account(userId, resultSet.getString("account_login"),
                        LocalDateTime.now(), centUse));
            }
            if (!account.isEmpty()) {
                InsertToSQL.addUseAccWithoutOrder(account);
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
            List<Account> accountList = new LinkedList<>();
            while (resultSet.next()) {
                accountList.add(new Account(userId, resultSet.getString("account_login"),
                        LocalDateTime.now(),
                        resultSet.getBoolean("cent_use")));
            }
            UpdateToSQL.updateWithOrder(accountList);
            if (accountList.size() < (five ? 5 : 2)) {
                accountList = selectAccountWithoutOrderFiveOrTwo(userId, five);
            }
            logger.info("Выдан аккаунт с заказом");
            return accountList;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static List<Account> selectAccountForOrder(Long userId, Boolean delete, Boolean cent) {
        return selectAccWithoutOrder(userId, SELECT_ACCOUNT_USE_FOR_ORDER, delete, cent);
    }

    public static List<Account> selectAccountWithoutOrderFiveOrTwo(Long userId, boolean five) {
        List<Account> accountList = new LinkedList<>();
        try (Connection con = Connecting.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(five ?
                    SELECT_ACCOUNT_USE_WITHOUT_ORDER_FIVE : SELECT_ACCOUNT_USE_WITHOUT_ORDER_TWO);
            stmt.setLong(1, userId);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                accountList.add(new Account(userId, resultSet.getString("account_login"),
                        LocalDateTime.now(),
                        resultSet.getBoolean("cent_use")));
            }
            UpdateToSQL.updateWithoutOrder(accountList);
            logger.info("Выдано " + (five ? 5 : 2) + " аккаунтов для сбива");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return accountList;
    }

    private static List<Account> selectAccWithoutOrder(Long userId, String sql, Boolean delete, Boolean cent) {
        List<Account> accountList = new LinkedList<>();
        try (Connection con = Connecting.getConnection()) {
            sql = cent ? SELECT_ACCOUNT_USE_WITHOUT_ORDER_FOR_CENT : sql;
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setLong(1, userId);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                accountList.add(new Account(userId, resultSet.getString("account_login"),
                        resultSet.getTimestamp("last_use").toLocalDateTime(),
                        sql.equals(SELECT_ACCOUNT_USE_FOR_ORDER) ?
                                resultSet.getBoolean("cent_use") : cent));
            }
            if (accountList.isEmpty() && cent) {
                accountList = selectAccWithoutOrder(userId, SELECT_ACCOUNT_USE_WITH_ORDER_FOR_CENT, false, false);
                if (!accountList.isEmpty()) {
                    accountList.get(0).setCentUse(true);
                    UpdateToSQL.updateWithOrder(accountList);
                }
            } else
                for (Account acc : accountList) {
                    if (delete) {
                        DeleteFromSQL.removeAccWithoutOrder(acc.getLogin());
                        InsertToSQL.addUseAccWithOrder(List.of(acc));
                    } else {
//                    if (sql.equals(SELECT_ACCOUNT_USE_WITH_ORDER_FOR_CENT)) {
//                      //  acc.setCentUse(cent);
//                        UpdateToSQL.updateWithOrder(List.of(acc));
//                    }
//                    else if (sql.equals(SELECT_ACCOUNT_USE_WITHOUT_ORDER_FOR_CENT))
//                        UpdateToSQL.updateWithoutOrderForCent(acc);
//                    else {
                        UpdateToSQL.updateWithoutOrder(List.of(acc)); //Сюда попадаем лишь когда запрашиваем аккаунт без заказов для сбива за цент
//                    }
                    }
                }
            logger.info("Выдан аккаунт без заказов");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return accountList;
    }

}
