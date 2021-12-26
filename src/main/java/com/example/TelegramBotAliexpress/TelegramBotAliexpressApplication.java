package com.example.TelegramBotAliexpress;

import com.example.TelegramBotAliexpress.service.sql.Config;
import com.pengrad.telegrambot.TelegramBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TelegramBotAliexpressApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelegramBotAliexpressApplication.class, args);
		TelegramBot bot = new TelegramBot(Config.getProperty(Config.BOT_TOKEN));
		Runner run = new Runner(bot);
		run.run();
	}

}
