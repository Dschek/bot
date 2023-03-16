package tydiru.bot.chatgpt.service;

import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tydiru.bot.chatgpt.config.Config;
import tydiru.bot.chatgpt.db.repository.UserRepository;

@Component
@Data
public class Telegram extends TelegramLongPollingBot {
    private Config config;
    private String gptToken;

    public Telegram(Config config) {
        this.config = config;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            System.out.println();
            return;
        }
        long telegramChatId = update.getMessage().getChatId();
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(telegramChatId));
        switch (update.getMessage().getText()) {
            case "/start":
                message.setText(checkGPTToken(telegramChatId) ? "Я готов, поехали" : "Пожалуста отправьте ваш chatGPT токен");
                break;
            default:
                message.setText("fuck you <|>");
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkGPTToken(long telegramChatId) {
        gptToken = UserRepository.getToken(telegramChatId);
        return gptToken == null;
    }

    @Override
    public String getBotUsername() {
        return config.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }
}
