package com.example.TelegramBotAliexpress;

import com.example.TelegramBotAliexpress.service.entity.Account;
import com.example.TelegramBotAliexpress.service.entity.SpentAccAndMoney;
import com.example.TelegramBotAliexpress.service.sql.Operation.SelectAllAccsFromSQL;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.example.TelegramBotAliexpress.enums.BotState;
import com.example.TelegramBotAliexpress.enums.MessageType;
import com.example.TelegramBotAliexpress.service.PreparationForDeleteAccounts;
import com.example.TelegramBotAliexpress.service.MessageForUser;
import com.example.TelegramBotAliexpress.service.entity.TelegramUser;
import com.example.TelegramBotAliexpress.service.sql.Operation.PriceOperation;
import com.example.TelegramBotAliexpress.service.sql.Operation.SelectFromSQL;
import com.example.TelegramBotAliexpress.service.PreparationForInsert;

import java.util.Arrays;

public class Runner {
    private static TelegramBot bot;
    private static TelegramUser user = new TelegramUser();

    public Runner(TelegramBot bot) {
        this.bot = bot;
    }

    public void run() {
        bot.setUpdatesListener(lst -> {
            lst.forEach(update -> {
                        new Thread(() -> {
                            Long userId = update.message().from().id();
                            if (update.message().text() == null) {
                                if (update.message().caption().contains("https://")) {
                                    String[] split = update.message().caption().split("\n");
                                    String link = split[1];
                                    bot.execute(new DeleteMessage(userId, update.message().messageId()));
                                    bot.execute(new SendMessage(userId, link).disableWebPagePreview(true));
                                } else {
                                    bot.execute(new SendMessage(userId, "Бот читает только символы, буквы и цифры"));
                                }
                                return;
                            }
                            System.out.println(update.message().text());
                            MessageForUser message = new MessageForUser(bot, userId);
                            switch (messageType(update)) {
                                case START:
                                    message.greeting();
                                    break;
                                case addNewAcc:
                                    TelegramUser.setUserCurrentBotState(userId, BotState.ADD_NEW_USER);
                                    message.simpleAnswer("Введите аккаунты в формате:\n" +
                                            "Логин\n" +
                                            "Логин\n" +
                                            "Логин\n" +
                                            "и т.д.");
                                    break;
                                case addUseAccWithOrders:
                                    TelegramUser.setUserCurrentBotState(userId, BotState.ADD_USE_ACC_WITH_ORDERS);
                                    message.simpleAnswer("Введите аккаунты (с заказами) в формате:\n" +
                                            "Логин\n" +
                                            "Логин\n" +
                                            "Логин\n" +
                                            "и т.д.");
                                    break;
                                case addUseAccWithOrdersToday:
                                    TelegramUser.setUserCurrentBotState(userId, BotState.ADD_USE_ACC_WITH_ORDERS_TODAY);
                                    message.simpleAnswer("Введите аккаунты (с заказами и использованные в течении последних 24 часов) в формате:\n" +
                                            "Логин\n" +
                                            "Логин\n" +
                                            "Логин\n" +
                                            "и т.д.");
                                    break;
                                case addUseAccWithoutOrders:
                                    TelegramUser.setUserCurrentBotState(userId, BotState.ADD_USE_ACC_WITHOUT_ORDERS);
                                    message.simpleAnswer("Введите аккаунты (без заказов) в формате:\n" +
                                            "Логин\n" +
                                            "Логин\n" +
                                            "Логин\n" +
                                            "и т.д.");
                                    break;
                                case addUseAccWithoutOrdersToday:
                                    TelegramUser.setUserCurrentBotState(userId, BotState.ADD_USE_ACC_WITHOUT_ORDERS_TODAY);
                                    message.simpleAnswer("Введите аккаунты (без заказов и использованные в течении последних 24 часов) в формате:\n" +
                                            "Логин\n" +
                                            "Логин\n" +
                                            "Логин\n" +
                                            "и т.д.");
                                    break;
                                case getNewAcc:
                                    message.sendNewAcc(SelectFromSQL.selectNewAccounts(userId, true), true);
                                    break;
                                case getTwoNewAcc:
                                    message.sendNewAcc(SelectFromSQL.selectNewAccounts(userId, false), false);
                                    break;
                                case getAccForOrder:
                                    message.sendAcc(SelectFromSQL.selectAccountForOrder(userId, true, false));
                                    break;
                                case getAccForHelpFive:
                                    message.sendAcc(SelectFromSQL.selectAccWithOrder(userId, true));
                                    break;
                                case getAccForHelpTwo:
                                    message.sendAcc(SelectFromSQL.selectAccWithOrder(userId, false));
                                    break;
                                case getAccForCent:
                                    message.sendAcc(SelectFromSQL.selectAccountForOrder(userId, false, true));
                                    break;
                                case DELETE_ACC:
                                    message.simpleAnswer("Введите логин аккаунта который требуется удалить");
                                    TelegramUser.setUserCurrentBotState(userId, BotState.DELETE_ACCOUNT);
                                    break;
                                case DELETE_ALL_ACC:
                                    message.simpleAnswer("Для очистки базы новых аккаунтов введите 1\n" +
                                            "Для очистки базы аккаунтов с заказами введите 2\n" +
                                            "Для очистки базы аккаунтов без заказов введите 3\n" +
                                            "Для очистки всех баз введите 4\n" +
                                            "Для отмены любой другой символ");
                                    TelegramUser.setUserCurrentBotState(userId, BotState.DELETE_ALL_ACC);
                                    break;
                                case GET_SPENT_TODAY:
                                    getSpentToday(userId, message);
                                    break;
                                case GET_SPENT_ALL:
                                    message.simpleAnswer(PriceOperation.priceSelectAll(userId).equals("")
                                            ? "Трат не найдено" : PriceOperation.priceSelectAll(userId));
                                    break;
                                case DELETE_SPENT:
                                    PriceOperation.priceDelete(userId);
                                    break;
                                case SPLITE_ACCS:
                                    message.simpleAnswer("Готов к разделению");
                                    TelegramUser.setUserCurrentBotState(userId, BotState.SPLIT_ACCOUNTS);
                                    break;
                                default:
                                    System.out.println("Normal");
                            }
                        }).start();
                    }
            );
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void getSpentToday(Long userId, MessageForUser message) {
        SpentAccAndMoney spentAccAndMoney = PriceOperation.priceSelectToday(userId);
        int countAccsWithoutCent = 0;
        countAccsWithoutCent += SelectAllAccsFromSQL.selectAccWithOrder(userId).stream().filter(acc -> !acc.isCentUse()).count();
        countAccsWithoutCent += SelectAllAccsFromSQL.selectAccWithoutOrder(userId).stream().filter(acc -> !acc.isCentUse()).count();
        if (spentAccAndMoney != null) {
            message.simpleAnswer("Сегодня потрачено: " + spentAccAndMoney.getSpentMoney() + "$\n" +
                    "Аккаунтов за день: " + spentAccAndMoney.getSpentTotalAccs() + "\n" +
                    "Сбивов за цент: " + spentAccAndMoney.getSpentAccsForCent() + "\n" +
                    "Осталось для сбива за цент: " + countAccsWithoutCent);
        } else {
            message.simpleAnswer("Сегодня трат не было");
        }
    }

    private MessageType messageType(Update update) {
        PreparationForInsert addition = new PreparationForInsert(update.message().from().id(), update.message().text());
        String text = update.message().text();
        long userId = update.message().from().id();
        MessageForUser message = new MessageForUser(bot, userId);
        BotState userStatus = user.getUserCurrentBotState(userId);
        if (text.contains("https://")) {
            bot.execute(new DeleteMessage(userId, update.message().messageId()));
            bot.execute(new SendMessage(userId, text).disableWebPagePreview(true));
        }
        if (text.equals("/start")) return MessageType.START;
        if (text.equals("/adnc")) return MessageType.addNewAcc;
        if (text.equals("/aduawo")) return MessageType.addUseAccWithOrders;
        if (text.equals("/aduawot")) return MessageType.addUseAccWithOrdersToday;
        if (text.equals("/aduawoo")) return MessageType.addUseAccWithoutOrders;
        if (text.equals("/aduawoot")) return MessageType.addUseAccWithoutOrdersToday;
        if (text.equals("/getaccorder")) return MessageType.getAccForOrder;
        if (text.equals("/getaccforcent")) return MessageType.getAccForCent;
        if (text.equals("/getnewacc")) return MessageType.getNewAcc;
        if (text.equals("/gettwonewacc")) return MessageType.getTwoNewAcc;
        if (text.equals("/getuseaccfive")) return MessageType.getAccForHelpFive;
        if (text.equals("/getuseacctwo")) return MessageType.getAccForHelpTwo;
        if (text.equals("/deletespent")) return MessageType.DELETE_SPENT;
        if (text.equals("/getspenttoday")) return MessageType.GET_SPENT_TODAY;
        if (text.equals("/getspentall")) return MessageType.GET_SPENT_ALL;
        if (text.equals("/splitaccs")) return MessageType.SPLITE_ACCS;
        if (text.equals("/info")) return MessageType.START;
        if (text.equals("/deleteacc")) return MessageType.DELETE_ACC;
        if (text.equals("/deleteallacc")) return MessageType.DELETE_ALL_ACC;
        if (text.equals("/getallaccs")) {
            message.allAccountsToUser();
        }
        if (userStatus == BotState.DELETE_ALL_ACC) {
            if (text.matches("[1-4]")) {
                PreparationForDeleteAccounts.deleteAllAcc(userId, text);
                message.simpleAnswer("Аккаунты удалены успешно");
            } else
                message.simpleAnswer("Удаление отменено");
            TelegramUser.setUserCurrentBotState(userId, BotState.WAIT_STATUS);
        }
        if (userStatus == BotState.SELECT_PRICE) {
            bot.execute(new DeleteMessage(userId, update.message().messageId()));
            TelegramUser.setMoneySpent(userId, Double.valueOf(text.replace("$", "")));
            TelegramUser.setUserCurrentBotState(userId, BotState.WAIT_STATUS);
            PriceOperation.priceInsert(userId, false);
        }
        if (userStatus == BotState.DELETE_ACCOUNT) {
            int deleteAcc = PreparationForDeleteAccounts.deleteAcc(text);
            TelegramUser.setUserCurrentBotState(userId, BotState.WAIT_STATUS);
            message.simpleAnswer("Успешно удалено: " + deleteAcc + " аккаунта(ов)");
        }
        if (userStatus == BotState.ADD_NEW_USER) {
            addition.addNewAcc();
            message.answerAddition(addition);
        }
        if (userStatus == BotState.ADD_USE_ACC_WITH_ORDERS) {
            addition.addUseAccWithOrders(false);
            message.answerAddition(addition);
        }
        if (userStatus == BotState.ADD_USE_ACC_WITH_ORDERS_TODAY) {
            addition.addUseAccWithOrders(true);
            message.answerAddition(addition);
        }
        if (userStatus == BotState.ADD_USE_ACC_WITHOUT_ORDERS) {
            addition.addUseAccWithoutOrders(false);
            message.answerAddition(addition);
        }
        if (userStatus == BotState.ADD_USE_ACC_WITHOUT_ORDERS_TODAY) {
            addition.addUseAccWithoutOrders(true);
            message.answerAddition(addition);
        }
        if (userStatus == BotState.SPLIT_ACCOUNTS) {
            splitAccounts(text, userId, message);
        }
        if (update.myChatMember() != null) return MessageType.CHAT_MEMBER;
        return MessageType.UNSUPPORTED;
    }

    private void splitAccounts(String text, long userId, MessageForUser message) {
        String[] accounts = text
                .replaceAll("\\n", " ")
                .replaceAll("\\\\", " ")
                .replaceAll("\\+", "")
                .split(" ");
        Arrays.stream(accounts).forEach(acc -> {
            if (acc.contains("@"))
                message.simpleAnswer(acc.trim());
        });
        TelegramUser.setUserCurrentBotState(userId, BotState.WAIT_STATUS);
    }

}
