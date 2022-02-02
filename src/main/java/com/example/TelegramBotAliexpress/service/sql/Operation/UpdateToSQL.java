package com.example.TelegramBotAliexpress.service.sql.Operation;

import com.example.TelegramBotAliexpress.service.entity.Account;
import com.example.TelegramBotAliexpress.service.sql.Connecting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

public class UpdateToSQL {
    private static final String UPDATE_ACCOUNT_USE_WITH_ORDER_CENT_USE = "UPDATE public.accounts_use_with_order\n" +
            "\tSET last_use=?, cent_use = ? \n" +
            "\tWHERE account_login=?";//Аккаунты которые не помогут в сбиве за цент
    private static final String UPDATE_ACCOUNT_USE_WITHOUT_ORDER_WITHOUT_CENT = "UPDATE public.accounts_use_without_order\n" +
            "\tSET last_use=?, cent_use = ?\n" +
            "\tWHERE account_login=?"; //Аккаунты которые не помогут в сбиве за цент
    private static Logger logger = Logger.getLogger("UpdateToSQL");

    public static void updateWithOrder(List<Account> account) {
        updateAcc(account, UPDATE_ACCOUNT_USE_WITH_ORDER_CENT_USE);
        logger.info("Обновлён аккаунт с заказом");
    }

    public static void updateWithoutOrder(List<Account> account) {
        updateAcc(account, UPDATE_ACCOUNT_USE_WITHOUT_ORDER_WITHOUT_CENT);
        logger.info("Обновлён аккаунт без заказов");
    }

    private static void updateAcc(List<Account> account, String updateAcc) {
        try (Connection con = Connecting.getConnection()) {
            con.setAutoCommit(false);
            PreparedStatement stmt = con.prepareStatement(updateAcc);
            try {
                for (Account acc : account) {
                    stmt.setObject(1, LocalDateTime.now());
                    stmt.setBoolean(2, acc.isCentUse());
                    stmt.setString(3, acc.getLogin());
                    stmt.addBatch();
                }
                logger.info("Обновлён аккаунт который больше не собъёт за цент");
                stmt.executeBatch();
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                ex.printStackTrace();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
