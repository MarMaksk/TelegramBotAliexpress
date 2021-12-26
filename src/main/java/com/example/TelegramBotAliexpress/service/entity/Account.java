package com.example.TelegramBotAliexpress.service.entity;

import java.time.LocalDateTime;

public class Account {
    private long idUser;
    private String login;
    private LocalDateTime lastUse = LocalDateTime.now();

    public Account() {
    }

    public Account(long idUser, String login, LocalDateTime lastUse) {
        this.idUser = idUser;
        this.login = login;
        this.lastUse = lastUse;
    }

    public Account(long idUser, String login) {
        this.idUser = idUser;
        this.login = login;
    }

    public LocalDateTime getLastUse() {
        return lastUse;
    }

    public long getIdUser() {
        return idUser;
    }

    public void setIdUser(long idUser) {
        this.idUser = idUser;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setLastUse(LocalDateTime lastUse) {
        this.lastUse = lastUse;
    }
}
