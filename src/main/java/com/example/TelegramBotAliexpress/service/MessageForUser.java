package com.example.TelegramBotAliexpress.service;

import com.example.TelegramBotAliexpress.service.sql.Operation.SelectAllAccsFromSQL;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.SendMessage;
import com.example.TelegramBotAliexpress.enums.BotState;
import com.example.TelegramBotAliexpress.service.entity.Account;
import com.example.TelegramBotAliexpress.service.entity.TelegramUser;
import com.example.TelegramBotAliexpress.service.sql.Operation.PriceOperation;

import java.time.LocalDateTime;
import java.util.List;

public class MessageForUser {
    private static final Integer MESSAGE_SIZE = 150;
    private TelegramBot bot;
    private long userId;

    public MessageForUser(TelegramBot bot, long userId) {
        this.bot = bot;
        this.userId = userId;
    }


    public void sendNewAcc(List<Account> account, boolean one) {
        if (account.isEmpty()) {
            bot.execute(new SendMessage(userId, "Доступных аккаунтов не найдено"));
            return;
        }
        if (account.size() == 2) {
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup("")
                    .addRow("0.3$")
                    .addRow("0.5$")
                    .addRow("1$")
                    .addRow("2$")
                    .resizeKeyboard(false).selective(true).oneTimeKeyboard(true);
            TelegramUser.setUserCurrentBotState(userId, BotState.SELECT_PRICE);
            bot.execute(new SendMessage(userId, account.get(0).getLogin()
                    .replace(":", " ").replace(";", " ")));
            bot.execute(new SendMessage(userId, account.get(1).getLogin()
                    .replace(":", " ").replace(";", " ")).replyMarkup(replyKeyboardMarkup));
        } else if (one) {
            bot.execute(new SendMessage(userId, account.get(0).getLogin()
                    .replace(":", " ").replace(";", " ")));
            TelegramUser.setMoneySpent(userId, 0.01);
            PriceOperation.priceInsert(userId, true);
        } else {
            simpleAnswer("К получению доступен лишь 1 аккаунт");
        }
    }

    public void sendAcc(List<Account> account) {
        if (account.isEmpty()) {
            bot.execute(new SendMessage(userId, "Доступных аккаунтов не найдено"));
            return;
        }
        account.forEach(acc -> bot.execute(new SendMessage(userId, acc.getLogin())));
    }

    public void simpleAnswer(String answer) {
        ReplyKeyboardRemove rkr = new ReplyKeyboardRemove();
        bot.execute(new SendMessage(userId, answer).replyMarkup(rkr));
    }

    public void answerAddition(PreparationForInsert addition) {
        simpleAnswer("Успешно добавлено: " + addition.getCount() + " аккаунта(ов)");
        TelegramUser.setUserCurrentBotState(userId, BotState.WAIT_STATUS);
    }

    public void allAccountsToUser() {
        StringBuilder sb = new StringBuilder();
        simpleAnswer("Все аккаунты новичка:");
        List<Account> accountListNew = SelectAllAccsFromSQL.selectNewAccounts(userId);
        accountListNew.forEach(acc -> sb.append(acc.getLogin()).append("\n"));
        simpleAnswer(sb.toString());
        sb.delete(0, sb.length());
        simpleAnswer("Аккаунты без заказов использованные в течении последних 24 часов " + accountListNew.size());
        List<Account> accountsWithoutOrder = SelectAllAccsFromSQL.selectAccWithoutOrder(userId);
        preparationAccsAfter(sb, accountsWithoutOrder);
        simpleAnswer("Аккаунтов без заказа: " + accountsWithoutOrder.size() +
                ".Не использованные в течении последних 24 часов: ");
        preparationAccsBefore(sb, accountsWithoutOrder);
        List<Account> accountsWithOrder = SelectAllAccsFromSQL.selectAccWithOrder(userId);
        simpleAnswer("Аккаунтов с заказами: " + accountsWithOrder.size() +
                ".Не использованные в течении последних 24 часов: ");
        preparationAccsAfter(sb, accountsWithOrder);
        simpleAnswer("Аккаунты с заказом не использованные в течении последних 24 часов");
        preparationAccsBefore(sb, accountsWithOrder);
    }

    private void preparationAccsAfter(StringBuilder sb, List<Account> accountsWithoutOrder) {
        for (Account account : accountsWithoutOrder)
            if (account.getLastUse().isAfter(LocalDateTime.now().minusDays(1)))
                sb.append(account.getLogin()).append(" ").append(account.isCentUse() ? "Да" : "Нет").append("\n");
        simpleAnswer(sb.toString());
        sb.delete(0, sb.length());
    }

    private void preparationAccsBefore(StringBuilder sb, List<Account> accountList) {
        if (accountList.size() > MESSAGE_SIZE) {
            int size = accountList.size() / 4;
            for (int i = 0; i <= size; i++) {
                Account account = accountList.get(i);
                if (account.getLastUse().isBefore(LocalDateTime.now().minusDays(1)))
                    sb.append(account.getLogin()).append(" ").append(account.isCentUse() ? "Да" : "Нет").append("\n");
            }
            simpleAnswer(sb.toString());
            sb.delete(0, sb.length());
            for (int i = size + 1; i <= size * 2; i++) {
                Account account = accountList.get(i);
                if (account.getLastUse().isBefore(LocalDateTime.now().minusDays(1)))
                    sb.append(account.getLogin()).append(" ").append(account.isCentUse() ? "Да" : "Нет").append("\n");
            }
            simpleAnswer(sb.toString());
            sb.delete(0, sb.length());
            for (int i = size * 2 + 1; i <= size * 3; i++) {
                Account account = accountList.get(i);
                if (account.getLastUse().isBefore(LocalDateTime.now().minusDays(1)))
                    sb.append(account.getLogin()).append(" ").append(account.isCentUse() ? "Да" : "Нет").append("\n");
            }
            for (int i = size * 3 + 1; i <= size * 4; i++) {
                Account account = accountList.get(i);
                if (account.getLastUse().isBefore(LocalDateTime.now().minusDays(1)))
                    sb.append(account.getLogin()).append(" ").append(account.isCentUse() ? "Да" : "Нет").append("\n");
            }
            sb.append(accountList.size());
            simpleAnswer(sb.toString());
            sb.delete(0, sb.length());
        } else {
            for (Account account : accountList)
                if (account.getLastUse().isBefore(LocalDateTime.now().minusDays(1)))
                    sb.append(account.getLogin()).append(" ").append(account.isCentUse() ? "Да" : "Нет").append("\n");
            simpleAnswer(sb.toString());
            sb.delete(0, sb.length());
        }
    }

    public void greeting() {
        bot.execute(new SendMessage(userId, "Hi. " +
                "Бот самостоятельно следит за использованными аккаунтами и их не надо несколько раз добавлять. " +
                "Инструкция по командам:\n" +
                "Получить новый аккаунт - выдача аккаунта из базы новых. Автоматическое добавление в базу аккаунтов с заказами\n" +
                "Получить 5 аккаунтов для помощи - выдача аккаунтов в первую очередь из базы с заказами, потом без\n " +
                "Получить 2 аккаунтов для помощи - выдача аккаунтов в первую очередь из базы с заказами, потом без\n " +
                "Новые аккаунты здесь не выдаются. Аккаунты будут доступены снова через 24 часа\n" +
                "Получить аккаунт для сбива за цент - аккаунт из базы аккаунтов без заказов или с заказами. " +
                "Аккаунт будет доступен снова через 24 часа\n" +
                "Добавить новый аккаунт - добавление аккаунта в базу новых аккаунтов. " +
                "Из этой базы выдаются аккаунты для финальных сбивов\n" +
                "Добавить аккаунт с заказами - аккаунт уже помогавший в финальном сбиве. И ИМЕЮЩИЙ ЗАКАЗ\n" +
                "Добавить аккаунт с заказами исп сегодня - аккаунт уже помогавший, ИМЕЮЩИЙ ЗАКАЗ и использовавшийся сегодня. " +
                "Будет доступен через 24 часа\n" +
                "Добавить акк без заказов - аккаунт уже помогавший в сбиве но не имеющий заказов\n" +
                "Добавить акк без заказов исп сегодня - аккаунт уже помогавший в сбиве, но не имеющий заказов, " +
                "использованный сегодня. Будет доступен через 24 часа\n" +
                "Сколько потрачено денег сегодня - сумма денег потраченных за день\n" +
                "Сколько потрачено денег за всё время - расходы в формате \"Дата Сумма\"\n" +
                "Получить все аккаунты из базы - возврат всех добавленых аккаунтов. " +
                "Аккаунты разбиты на категории и разделены пробелом\n" +
                "Удалить аккаунт из баз - удаление аккаунта из всех баз\n" +
                "Очистить базу контактов - очистка определённой или всех баз\n" +
                "Информация о командах - повторный вывод этой информации\n" +
                "При добавлении из Excel телеграмм предложит отправить картинкой. Нажимаем ОТМЕНА и отправляем"));
    }
}
