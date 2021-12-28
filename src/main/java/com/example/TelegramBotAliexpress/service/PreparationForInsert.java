package com.example.TelegramBotAliexpress.service;

import com.example.TelegramBotAliexpress.service.entity.Account;
import com.example.TelegramBotAliexpress.service.sql.Operation.InsertToSQL;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class PreparationForInsert {
    private static LocalDateTime dateTime = LocalDateTime.now();
    private long userId;
    private String message;
    private List<Account> accountList = new LinkedList<>();
    private int count;


    public PreparationForInsert(long userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public void addNewAcc() {
        preparation(false);
        InsertToSQL.addNewAccounts(accountList);
        accountList = new LinkedList<>();
    }

    public void addUseAccWithOrders(Boolean use) {
        preparation(use);
        accountList.forEach(InsertToSQL::addUseAccWithOrder);
        accountList = new LinkedList<>();
    }

    public void addUseAccWithoutOrders(Boolean use) {
        preparation(use);
        accountList.forEach(InsertToSQL::addUseAccWithoutOrder);
        accountList = new LinkedList<>();
    }

    private void preparation(Boolean use) {
        String[] accounts = message
                .replaceAll("\\n", " ")
                .replaceAll("\\\\", " ")
                .replaceAll("\\+", "")
                .split(" ");
        for (int i = 0; i < accounts.length; i++) {
            if (!accounts[i].contains("@"))
                continue;
            accountList.add(new Account(userId, accounts[i].trim(),
                    use ? dateTime : dateTime.minusWeeks(1)));
            count++;
        }
    }

    public int getCount() {
        return count;
    }
}
