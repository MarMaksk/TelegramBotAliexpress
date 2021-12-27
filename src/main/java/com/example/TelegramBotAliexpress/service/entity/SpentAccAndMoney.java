package com.example.TelegramBotAliexpress.service.entity;

public class SpentAccAndMoney {
    private double spentMoney;
    private int spentTotalAccs;
    private int spentAccsForCent;

    public SpentAccAndMoney(double spentMoney, int spentTotalAccs, int spentAccsForCent) {
        this.spentMoney = spentMoney;
        this.spentTotalAccs = spentTotalAccs;
        this.spentAccsForCent = spentAccsForCent;
    }

    public double getSpentMoney() {
        return spentMoney;
    }

    public void setSpentMoney(double spentMoney) {
        this.spentMoney = spentMoney;
    }

    public int getSpentTotalAccs() {
        return spentTotalAccs;
    }

    public void setSpentTotalAccs(int spentTotalAccs) {
        this.spentTotalAccs = spentTotalAccs;
    }

    public int getSpentAccsForCent() {
        return spentAccsForCent;
    }

    public void setSpentAccsForCent(int spentAccsForCent) {
        this.spentAccsForCent = spentAccsForCent;
    }
}
