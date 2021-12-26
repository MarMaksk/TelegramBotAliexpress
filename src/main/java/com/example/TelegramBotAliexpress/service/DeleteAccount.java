package com.example.TelegramBotAliexpress.service;

import com.example.TelegramBotAliexpress.service.sql.Operation.DeleteFromSQL;

import java.util.logging.Logger;

public class DeleteAccount {
    private static Logger logger = Logger.getLogger("DeleteAccount");
    public static int deleteAcc(String login) {
        int count = 0;
        String[] accounts = login.replaceAll("\\n", " ").replaceAll("\\\\", " ").split(" ");
        for (String account : accounts) {
            if (!account.contains("@"))
                continue;
            DeleteFromSQL.removeNewAccount(account);
            DeleteFromSQL.removeAccWithOrder(account);
            DeleteFromSQL.removeAccWithoutOrder(account);
            count++;
            logger.info(account + " удалён из баз");
        }
        return count;
    }

    public static void deleteAllAcc(long userId, String type) {
        if (type.equals("1"))
            DeleteFromSQL.removeNewAccountUser(userId);
        if (type.equals("2"))
            DeleteFromSQL.removeAccWithOrderUser(userId);
        if (type.equals("3"))
            DeleteFromSQL.removeAccWithoutOrderUser(userId);
        if (type.equals("4")) {
            DeleteFromSQL.removeNewAccountUser(userId);
            DeleteFromSQL.removeAccWithOrderUser(userId);
            DeleteFromSQL.removeAccWithoutOrderUser(userId);
        }
    }


}
