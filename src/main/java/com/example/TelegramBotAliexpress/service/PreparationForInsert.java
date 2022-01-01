package com.example.TelegramBotAliexpress.service;

import com.example.TelegramBotAliexpress.enums.BotState;
import com.example.TelegramBotAliexpress.service.entity.Account;
import com.example.TelegramBotAliexpress.service.entity.TelegramUser;
import com.example.TelegramBotAliexpress.service.sql.Operation.InsertToSQL;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PreparationForInsert {
    private static LocalDateTime dateTime = LocalDateTime.now();
    private long userId;
    private String message;
    private int count;


    public PreparationForInsert(long userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public void addNewAcc() {
        this.count = (int) InsertToSQL.addNewAccounts(preparation(false));
    }

    public void addUseAccWithOrders(Boolean use) {
        this.count = (int) InsertToSQL.addUseAccWithOrder(preparation(use));
    }

    public void addUseAccWithoutOrders(Boolean use) {
        this.count = (int) InsertToSQL.addUseAccWithoutOrder(preparation(use));
    }

    private List<Account> preparation(Boolean use) {
        List<Account> accountList = new ArrayList<>();
        String[] accounts = message
                .replaceAll("\\n", " ")
                .replaceAll("\\\\", " ")
                .replaceAll("\\+", "")
                .split(" ");
        for (String account : accounts) {
            if (!account.contains("@"))
                continue;
            accountList.add(new Account(userId, account.trim(),
                    use ? dateTime : dateTime.minusWeeks(1)));
        }
        TelegramUser.setUserCurrentBotState(userId, BotState.WAIT_STATUS);
        return accountList;
    }

    public int getCount() {
        return count;
    }
}
