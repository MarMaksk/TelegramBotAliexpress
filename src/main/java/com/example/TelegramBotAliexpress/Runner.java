package com.example.TelegramBotAliexpress;

import com.example.TelegramBotAliexpress.service.entity.SpentAccAndMoney;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.example.TelegramBotAliexpress.enums.BotState;
import com.example.TelegramBotAliexpress.enums.MessageType;
import com.example.TelegramBotAliexpress.service.DeleteAccount;
import com.example.TelegramBotAliexpress.service.MessageForUser;
import com.example.TelegramBotAliexpress.service.entity.TelegramUser;
import com.example.TelegramBotAliexpress.service.sql.Operation.PriceOperation;
import com.example.TelegramBotAliexpress.service.sql.Operation.SelectAllAccsFromSQL;
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
                            if (update.message().text() == null) {
                                bot.execute(new SendMessage(update.message().from().id(), "Бот читает только символы, буквы и цифры"));
                                return;
                            }
                            Long userId = update.message().from().id();
                            System.out.println(update.message().text());
                            MessageForUser message = new MessageForUser(bot);
                            switch (messageType(update)) {
                                case START:
                                    message.greeting(userId);
                                    break;
                                case addNewAcc:
                                    TelegramUser.setUserCurrentBotState(userId, BotState.ADD_NEW_USER);
                                    message.simpleAnswer(userId, "Введите аккаунты в формате:\n" +
                                            "Логин\n" +
                                            "Логин\n" +
                                            "Логин\n" +
                                            "и т.д.");
                                    break;
                                case addUseAccWithOrders:
                                    TelegramUser.setUserCurrentBotState(userId, BotState.ADD_USE_ACC_WITH_ORDERS);
                                    message.simpleAnswer(userId, "Введите аккаунты (с заказами) в формате:\n" +
                                            "Логин\n" +
                                            "Логин\n" +
                                            "Логин\n" +
                                            "и т.д.");
                                    break;
                                case addUseAccWithOrdersToday:
                                    TelegramUser.setUserCurrentBotState(userId, BotState.ADD_USE_ACC_WITH_ORDERS_TODAY);
                                    message.simpleAnswer(userId, "Введите аккаунты (с заказами и использованные в течении последних 24 часов) в формате:\n" +
                                            "Логин\n" +
                                            "Логин\n" +
                                            "Логин\n" +
                                            "и т.д.");
                                    break;
                                case addUseAccWithoutOrders:
                                    TelegramUser.setUserCurrentBotState(userId, BotState.ADD_USE_ACC_WITHOUT_ORDERS);
                                    message.simpleAnswer(userId, "Введите аккаунты (без заказов) в формате:\n" +
                                            "Логин\n" +
                                            "Логин\n" +
                                            "Логин\n" +
                                            "и т.д.");
                                    break;
                                case addUseAccWithoutOrdersToday:
                                    TelegramUser.setUserCurrentBotState(userId, BotState.ADD_USE_ACC_WITHOUT_ORDERS_TODAY);
                                    message.simpleAnswer(userId, "Введите аккаунты (без заказов и использованные в течении последних 24 часов) в формате:\n" +
                                            "Логин\n" +
                                            "Логин\n" +
                                            "Логин\n" +
                                            "и т.д.");
                                    break;
                                case getNewAcc: ;
                                    message.sendNewAcc(userId, SelectFromSQL.selectNewAccounts(userId, true), true);
                                    break;
                                case getTwoNewAcc:
                                    message.sendNewAcc(userId, SelectFromSQL.selectNewAccounts(userId, false), false);
                                    break;
                                case getAccForOrder:
                                    message.sendAcc(userId, SelectFromSQL.selectAccountForOrder(userId, true, false));
                                    break;
                                case getAccForHelpFive:
                                    message.sendAcc(userId, SelectFromSQL.selectAccWithOrder(userId, true));
                                    break;
                                case getAccForHelpTwo:
                                    message.sendAcc(userId, SelectFromSQL.selectAccWithOrder(userId, false));
                                    break;
                                case getAccForCent:
                                    message.sendAcc(userId, SelectFromSQL.selectAccountForOrder(userId, false, true));
                                    break;
                                case DELETE_ACC:
                                    message.simpleAnswer(userId, "Введите логин аккаунта который требуется удалить");
                                    TelegramUser.setUserCurrentBotState(userId, BotState.DELETE_ACCOUNT);
                                    break;
                                case DELETE_ALL_ACC:
                                    message.simpleAnswer(userId, "Для очистки базы новых аккаунтов введите 1\n" +
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
                                    message.simpleAnswer(userId, PriceOperation.priceSelectAll(userId).equals("")
                                            ? "Трат не найдено" : PriceOperation.priceSelectAll(userId));
                                    break;
                                case DELETE_SPENT:
                                    PriceOperation.priceDelete(userId);
                                    break;
                                case SPLITE_ACCS:
                                    message.simpleAnswer(userId, "Готов к разделению");
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
        if (spentAccAndMoney != null) {
            message.simpleAnswer(userId, "Сегодня потрачено: " + spentAccAndMoney.getSpentMoney() + "$\n" +
                    "Аккаунтов за день: " + spentAccAndMoney.getSpentTotalAccs() + "\n" +
                    "Сбивов за цент: " + spentAccAndMoney.getSpentAccsForCent());
        } else {
            message.simpleAnswer(userId, "Сегодня трат не было");
        }
    }

    private MessageType messageType(Update update) {
        PreparationForInsert addition = new PreparationForInsert(update.message().from().id(), update.message().text());
        String text = update.message().text();
        long userId = update.message().from().id();
        MessageForUser message = new MessageForUser(bot);
        BotState userStatus = user.getUserCurrentBotState(userId);
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
            message.simpleAnswer(userId, "Все аккаунты новичка:");
            message.simpleAnswer(userId, SelectAllAccsFromSQL.selectNewAccounts(userId));
            message.simpleAnswer(userId, "Все аккаунты без заказов:");
            message.simpleAnswer(userId, SelectAllAccsFromSQL.selectAccWithoutOrder(userId));
            message.simpleAnswer(userId, "Все аккаунты с заказами:");
            message.simpleAnswer(userId, SelectAllAccsFromSQL.selectAccWithOrder(userId));
        }
        if (userStatus == BotState.DELETE_ALL_ACC) {
            if (text.matches("[1-4]")) {
                DeleteAccount.deleteAllAcc(userId, text);
                message.simpleAnswer(userId, "Аккаунты удалены успешно");
            } else
                message.simpleAnswer(userId, "Удаление отменено");
            TelegramUser.setUserCurrentBotState(userId, BotState.WAIT_STATUS);
        }
        if (userStatus == BotState.SELECT_PRICE) {
            message.simpleAnswer(userId, text + " добавлено к общей сумме за день");
            TelegramUser.setMoneySpent(userId, Double.valueOf(text.replace("$", "")));
            TelegramUser.setUserCurrentBotState(userId, BotState.WAIT_STATUS);
            PriceOperation.priceInsert(userId, false);
        }
        if (userStatus == BotState.DELETE_ACCOUNT) {
            int deleteAcc = DeleteAccount.deleteAcc(text);
            TelegramUser.setUserCurrentBotState(userId, BotState.WAIT_STATUS);
            message.simpleAnswer(userId, "Успешно удалено: " + deleteAcc + " аккаунта(ов)");
        }
        if (userStatus == BotState.ADD_NEW_USER) {
            addition.addNewAcc();
            message.answerAddition(userId, addition);
        }
        if (userStatus == BotState.ADD_USE_ACC_WITH_ORDERS) {
            addition.addUseAccWithOrders(false);
            message.answerAddition(userId, addition);
        }
        if (userStatus == BotState.ADD_USE_ACC_WITH_ORDERS_TODAY) {
            addition.addUseAccWithOrders(true);
            message.answerAddition(userId, addition);
        }
        if (userStatus == BotState.ADD_USE_ACC_WITHOUT_ORDERS) {
            addition.addUseAccWithoutOrders(false);
            message.answerAddition(userId, addition);
        }
        if (userStatus == BotState.ADD_USE_ACC_WITHOUT_ORDERS_TODAY) {
            addition.addUseAccWithoutOrders(true);
            message.answerAddition(userId, addition);
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
                message.simpleAnswer(userId, acc.trim());
        });
        TelegramUser.setUserCurrentBotState(userId, BotState.WAIT_STATUS);
    }

}
