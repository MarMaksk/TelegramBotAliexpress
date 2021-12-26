package com.example.TelegramBotAliexpress.service.entity;

import com.example.TelegramBotAliexpress.enums.BotState;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class TelegramUser {
    private static Map<Long, BotState> usersBotStates = new HashMap<>();
    private static Map<Long, String> lastOrderAcc = new HashMap<>();
    private static Map<Long, Double> moneySpent = new HashMap<>();
    private static Logger logger = Logger.getLogger("TelegramUser");

    public static void setMoneySpent(Long userId, Double money) {
        moneySpent.put(userId, money);
    }

    public static Double getMoneySpent(Long userId) {
        return moneySpent.get(userId) == null ? 0 : moneySpent.get(userId);
    }

    public static void setUserLastUseAcc(Long userId, String login) {
        lastOrderAcc.put(userId, login);
    }

    public static String getUserLastUseAcc(Long userId) {
        return lastOrderAcc.get(userId) == null ? "Аккаунт не найден" : lastOrderAcc.get(userId);
    }


    public static void setUserCurrentBotState(Long userId, BotState botState) {
        logger.info("Статус пользователя " + userId + " изменён на " + botState);
        usersBotStates.put(userId, botState);
    }

    public static BotState getUserCurrentBotState(Long userId) {
        return usersBotStates.get(userId) == null ? BotState.WAIT_STATUS : usersBotStates.get(userId);
    }
}
