package com.example.TelegramBotAliexpress.service.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connecting {

        public static Connection getConnection(){
            Connection con = null;
            try {
                con = DriverManager.getConnection(
                        Config.getProperty(Config.DB_URL),
                        Config.getProperty(Config.DB_LOGIN),
                        Config.getProperty(Config.DB_PASSWORD)
                );
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return con;
        }
    }
