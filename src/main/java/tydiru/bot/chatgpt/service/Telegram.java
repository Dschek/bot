package tydiru.bot.chatgpt.service;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tydiru.bot.chatgpt.config.Config;
import tydiru.bot.chatgpt.db.dto.Users;
import tydiru.bot.chatgpt.db.repository.UserRepository;

@Component
@Data
public class Telegram extends TelegramLongPollingBot {
    private final UserRepository userRepository;
    private Config config;
    private String gptToken;
    private Boolean isAuth = false;

    @Autowired
    public Telegram(Config config, UserRepository userRepository) {
        this.config = config;
        this.userRepository = userRepository;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            System.out.println();
            return;
        }
        String telegramChatId = String.valueOf(update.getMessage().getChatId());
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(telegramChatId));
        String messageText = update.getMessage().getText();
        switch (messageText) {
            case "/start":
                if(checkGPTToken(telegramChatId)){
                    message.setText("Я готов, поехали");
                    break;
                }
                message.setText("Пожалуста отправьте ваш chatGPT токен");
                break;
            case "/reset":
                message.setText(saveUser(telegramChatId, null)?"Токен успешно сброшен":"Извините, не удалось сбросить токен");
                break;
            default:
                if (!checkGPTToken(telegramChatId)) {
                    saveUser(telegramChatId, messageText);
                    message.setText(saveUser(telegramChatId, messageText)?"Токен успешно добавлен":"Извините, не удалось сохранить токен");
                    break;
                }else{
                    message.setText(gptToken);
                }

        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkGPTToken(String telegramChatId) {
        Users users = userRepository.findByTelegramChatId(telegramChatId);
        gptToken = users == null ? null : users.getGptTokenId();
        return gptToken != null;
    }

    private boolean saveUser(String telegramChatId, String gptTokenId){
        try {
            userRepository.save(new Users(telegramChatId, gptTokenId));
        }catch (Exception e){
            System.out.println(e);
            return false;
        }
        return true;
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
