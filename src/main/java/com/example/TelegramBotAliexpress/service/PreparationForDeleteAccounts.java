package com.example.TelegramBotAliexpress.service;

import com.example.TelegramBotAliexpress.service.entity.Account;
import com.example.TelegramBotAliexpress.service.sql.Operation.DeleteFromSQL;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class PreparationForDeleteAccounts {
    private static Logger logger = Logger.getLogger("DeleteAccount");
    public static int deleteAcc(String login) {
        int count = 0;
        String[] accounts = login.replaceAll("\\n", " ").replaceAll("\\\\", " ").split(" ");
        List<String> deleteList = new ArrayList<>();
        for (String account : accounts) {
            if (!account.contains("@"))
                continue;
            deleteList.add(account);
            count++;
            logger.info(account + " отправлен на удаление");
        }
        DeleteFromSQL.removeNewAccount(deleteList);
        DeleteFromSQL.removeAccWithOrder(deleteList);
        DeleteFromSQL.removeAccWithoutOrder(deleteList);
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
