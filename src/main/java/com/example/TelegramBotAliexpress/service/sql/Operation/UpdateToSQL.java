package com.example.TelegramBotAliexpress.service.sql.Operation;

import com.example.TelegramBotAliexpress.service.entity.Account;
import com.example.TelegramBotAliexpress.service.sql.Connecting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.logging.Logger;

public class UpdateToSQL {
    private static final String UPDATE_ACCOUNT_NEW = "UPDATE public.accounts_new\n" +
            "\tSET user_id=?, account_login=?\n" +
            "\tWHERE account_login=?";
    private static final String UPDATE_ACCOUNT_USE_WITH_ORDER = "UPDATE public.accounts_use_with_order\n" +
            "\tSET last_use=?\n" +
            "\tWHERE account_login=?";
    private static final String UPDATE_ACCOUNT_USE_WITH_ORDER_CENT_USE = "UPDATE public.accounts_use_with_order\n" +
            "\tSET last_use=?, cent_use = ? \n" +
            "\tWHERE account_login=?";//Аккаунты которые не помогут в сбиве за цент
    private static final String UPDATE_ACCOUNT_USE_WITHOUT_ORDER = "UPDATE public.accounts_use_without_order\n" +
            "\tSET last_use=?\n" +
            "\tWHERE account_login=?";
    private static final String UPDATE_ACCOUNT_USE_WITHOUT_ORDER_WITHOUT_CENT = "UPDATE public.accounts_use_without_order\n" +
            "\tSET last_use=?, cent_use = ?\n" +
            "\tWHERE account_login=?"; //Аккаунты которые не помогут в сбиве за цент
    private static Logger logger = Logger.getLogger("UpdateToSQL");

    public static void updateWithOrder(Account account, boolean cent) {
        updateAcc(account, cent ? UPDATE_ACCOUNT_USE_WITH_ORDER_CENT_USE : UPDATE_ACCOUNT_USE_WITH_ORDER);
        logger.info("Обновлён аккаунт с заказом");
    }

    public static void updateWithoutOrder(Account account, boolean cent) {
        updateAcc(account, cent ? UPDATE_ACCOUNT_USE_WITHOUT_ORDER_WITHOUT_CENT : UPDATE_ACCOUNT_USE_WITHOUT_ORDER);
        logger.info("Обновлён аккаунт без заказов");
    }

//    public static void updateWithoutOrderForCent(Account account) {
//        updateAcc(account, UPDATE_ACCOUNT_USE_WITHOUT_ORDER_WITHOUT_CENT);
//        logger.info("Обновлён аккаунт без заказов");
//    }

    private static void updateAcc(Account account, String updateAcc) {
        try (Connection con = Connecting.getConnection()) {
            con.setAutoCommit(false);
            PreparedStatement stmt = con.prepareStatement(updateAcc);
            try {
                if (updateAcc.equals(UPDATE_ACCOUNT_USE_WITHOUT_ORDER_WITHOUT_CENT)
                        || updateAcc.equals(UPDATE_ACCOUNT_USE_WITH_ORDER_CENT_USE)) {
                    stmt.setObject(1, LocalDateTime.now());
                    stmt.setBoolean(2, true);
                    stmt.setString(3, account.getLogin());
                    logger.info("Обновлён аккаунт который больше не собъёт за цент");
                } else {
                    stmt.setObject(1, account.getLastUse());
                    stmt.setString(2, account.getLogin());
                    logger.info("Обновлён аккаунт который теперь имеет заказы");
                }
                stmt.executeUpdate();
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
