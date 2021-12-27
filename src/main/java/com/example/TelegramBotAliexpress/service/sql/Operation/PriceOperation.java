package com.example.TelegramBotAliexpress.service.sql.Operation;

import com.example.TelegramBotAliexpress.service.entity.SpentAccAndMoney;
import com.example.TelegramBotAliexpress.service.entity.TelegramUser;
import com.example.TelegramBotAliexpress.service.sql.Connecting;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.logging.Logger;

public class PriceOperation {

    private static final String INSERT_MONEY = "INSERT INTO public.users_price(\n" +
            "\tuser_id, calendar_date, spent_money, account_total, accounts_cent)\n" +
            "\tVALUES (?, ?, ?, ?, ?);";
    private static final String UPDATE_MONEY = "UPDATE public.users_price\n" +
            "\tSET spent_money=?, account_total=?, accounts_cent=?\n" +
            "\tWHERE user_id=? AND calendar_date=?";
    private static final String SELECT_MONEY_DATE = "SELECT spent_money, account_total, accounts_cent\n" +
            "\tFROM public.users_price WHERE user_id = ? AND calendar_date = ?";
    private static final String SELECT_MONEY_ALL = "SELECT calendar_date, spent_money, account_total, accounts_cent\n" +
            "\tFROM public.users_price WHERE user_id = ?";
    private static final String DELETE_MONEY = "DELETE FROM public.users_price\n" +
            "\tWHERE user_id = ? AND calendar_date = ?";
    private static Logger logger = Logger.getLogger("PriceOperation");

    public static void priceInsert(long userId, boolean oneAccount) {
        try (Connection con = Connecting.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(INSERT_MONEY);
            con.setAutoCommit(false);
            double money = TelegramUser.getMoneySpent(userId);
            try {
                stmt.setLong(1, userId);
                stmt.setString(2, getDateToday());
                stmt.setDouble(3, money);
                if (oneAccount) {
                    stmt.setLong(4, 1);
                    stmt.setLong(5, 1);
                } else {
                    stmt.setLong(4, 2);
                    stmt.setLong(5, 0);
                }
                stmt.executeUpdate();
                con.commit();
                logger.info("Добавлены новые данные о сумме");
            } catch (SQLException ex) {
                con.rollback();
                priceUpdate(userId, money, oneAccount);
                throw ex;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void priceUpdate(long userId, Double money, boolean oneAccount) {
        try (Connection con = Connecting.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(UPDATE_MONEY);
            con.setAutoCommit(false);
            try {
                SpentAccAndMoney spentAccAndMoney = priceSelectToday(userId);
                int totalAccs = spentAccAndMoney.getSpentTotalAccs();
                int accsForCent = spentAccAndMoney.getSpentAccsForCent();
                stmt.setDouble(1, spentAccAndMoney.getSpentMoney() + money);
                stmt.setLong(2, oneAccount ? totalAccs + 1 : totalAccs + 2);
                stmt.setLong(3, oneAccount ? accsForCent + 1 : accsForCent);
                stmt.setLong(4, userId);
                stmt.setString(5, getDateToday());
                stmt.executeUpdate();
                con.commit();
                logger.info("Получены данные о сумме");
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static SpentAccAndMoney priceSelectToday(long userId) {
        SpentAccAndMoney spent = null;
        try (Connection con = Connecting.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(SELECT_MONEY_DATE);
            stmt.setLong(1, userId);
            stmt.setString(2, getDateToday());
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                spent = new SpentAccAndMoney(resultSet.getDouble("spent_money"),
                        resultSet.getInt("account_total"),
                        resultSet.getInt("accounts_cent"));
            }
            logger.info("Получены данные о сумме");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return spent;
    }

    public static String priceSelectAll(long userId) {
        StringBuilder res = new StringBuilder();
        try (Connection con = Connecting.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(SELECT_MONEY_ALL);
            try {
                LocalDateTime ldt = LocalDateTime.now();
                stmt.setLong(1, userId);
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    res.append(resultSet.getString("calendar_date")).append(": ");
                    res.append(resultSet.getDouble("spent_money")).append("$\n");
                    res.append("Аккаунтов за день: ").append(resultSet.getInt("account_total")).append("\n");
                    res.append("Сбивов за цент: ").append(resultSet.getInt("accounts_cent")).append("\n");
                }
                System.out.println("Получены данные о сумме");
            } catch (SQLException ex) {
                throw ex;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return res.toString();
    }

    public static void priceDelete(long userId) {
        try (Connection con = Connecting.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(DELETE_MONEY);
            stmt.setLong(1, userId);
            stmt.setString(2, getDateToday());
            stmt.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private static String getDateToday() {
        LocalDateTime ldt = LocalDateTime.now();
        return ldt.getDayOfMonth() + " " + ldt.getMonth().getDisplayName(TextStyle.FULL, new Locale("ru"))
                + " " + ldt.getYear();
    }
}
