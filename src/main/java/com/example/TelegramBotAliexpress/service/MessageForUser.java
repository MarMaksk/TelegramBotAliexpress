package com.example.TelegramBotAliexpress.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.SendMessage;
import com.example.TelegramBotAliexpress.enums.BotState;
import com.example.TelegramBotAliexpress.service.entity.Account;
import com.example.TelegramBotAliexpress.service.entity.TelegramUser;
import com.example.TelegramBotAliexpress.service.sql.Operation.PriceOperation;

import java.util.List;

public class MessageForUser {
    private TelegramBot bot;


    public MessageForUser(TelegramBot bot) {
        this.bot = bot;
    }


    public void sendNewAcc(long userId, List<Account> account, boolean one) {
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
            bot.execute(new SendMessage(userId, account.get(0).getLogin()));
            bot.execute(new SendMessage(userId, account.get(1).getLogin()).replyMarkup(replyKeyboardMarkup));
        } else if (one) {
            bot.execute(new SendMessage(userId, account.get(0).getLogin()));
            simpleAnswer(userId, "0.01$ добавлено к общей сумме за день");
            TelegramUser.setMoneySpent(userId, 0.01);
            PriceOperation.priceInsert(userId);
        } else {
            simpleAnswer(userId, "К получению доступен лишь 1 аккаунт");
        }
    }

    public void sendAcc(long userId, List<Account> account) {
        if (account.isEmpty()) {
            bot.execute(new SendMessage(userId, "Доступных аккаунтов не найдено"));
            return;
        }
        bot.execute(new SendMessage(userId, "После использования аккаунты будут считаться использованными" +
                " и будут доступены снова через 24 часа"));
        account.forEach(acc -> bot.execute(new SendMessage(userId, acc.getLogin())));
    }

    public void simpleAnswer(long userId, String answer) {
        ReplyKeyboardRemove rkr = new ReplyKeyboardRemove();
        bot.execute(new SendMessage(userId, answer).replyMarkup(rkr));
    }

    public void answerAddition(long userId, PreparationForInsert addition) {
        simpleAnswer(userId, "Успешно добавлено: " + addition.getCount() + " аккаунта(ов)");
        TelegramUser.setUserCurrentBotState(userId, BotState.WAIT_STATUS);
    }

    public void greeting(long userId) {
        bot.execute(new SendMessage(userId, "Hi. " +
                "Бот самостоятельно следит за использованными аккаунтами и их не надо несколько раз добавлять. " +
                "Инструкция по командам:\n" +
                "Получить новый аккаунт - выдача аккаунта из базы новых. Автоматическое добавление в базу аккаунтов с заказами\n" +
                "Получить 5 аккаунтов для помощи - выдача аккаунтов в первую очередь из базы с заказами, потом без\n " +
                "Получить 2 аккаунтов для помощи - выдача аккаунтов в первую очередь из базы с заказами, потом без\n " +
                "Новые аккаунты здесь не выдаются. Аккаунты будут доступены снова через 24 часа\n" +
                "Получить аккаунт для сбива за цент - аккаунт из базы аккаунтов без заказов. " +
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
