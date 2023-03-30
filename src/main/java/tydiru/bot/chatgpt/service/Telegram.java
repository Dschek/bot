package tydiru.bot.chatgpt.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tydiru.bot.chatgpt.config.Config;
import tydiru.bot.chatgpt.db.dto.ChatGPTRequest;
import tydiru.bot.chatgpt.db.dto.Message;
import tydiru.bot.chatgpt.db.dto.MongoMessage;
import tydiru.bot.chatgpt.db.dto.Users;
import tydiru.bot.chatgpt.db.repository.MongoMessageRepository;
import tydiru.bot.chatgpt.db.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Data
@RequiredArgsConstructor
@Slf4j
public class Telegram extends TelegramLongPollingBot {
    private final UserRepository userRepository;
    private final ChatRestClient chatRestClient;
    private final Config config;

    private final MongoMessageRepository mongoMessageRepository;

    private String gptToken;
    private Boolean isAuth = false;

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
            case "/clear":
                mongoMessageRepository.deleteById(telegramChatId);
                break;
            case "/check":
                List<MongoMessage> messageList = mongoMessageRepository.findByTelegramChatId(telegramChatId);
                message.setText("Список вопросов: " + messageList.stream().map(MongoMessage::getContent).toList());
                break;
            case "/checkIndex":
                message.setText("Index: " + getUsersIndex(telegramChatId));
                break;
            default:
                if (!checkGPTToken(telegramChatId)) {
                    saveUser(telegramChatId, messageText);
                    message.setText(saveUser(telegramChatId, messageText)?"Токен успешно добавлен":"Извините, не удалось сохранить токен");
                    break;
                } else {
                    ChatGPTRequest chatGPTRequest = new ChatGPTRequest();
                    chatGPTRequest.setModel("gpt-3.5-turbo");
                    Integer index = getUsersIndex(telegramChatId);
                    if (index == 0) {
                        message.setText(firstQuestion(chatGPTRequest, messageText, telegramChatId, index));
                    } else if (index < 20) {
                        message.setText(otherMessages(chatGPTRequest, messageText, telegramChatId, index));
                    } else {
                        message.setText("Извините, лимит чата исччерпан, необходимо отчистить чат командой /clean");
                    }
                }
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    @Transactional
    String firstQuestion(ChatGPTRequest chatGPTRequest, String messageText, String telegramChatId, int index ) {
        chatGPTRequest.setMessages(List.of(new Message("user", messageText)));
        String response = chatRestClient.post(chatGPTRequest, gptToken).getBody().getChoices().get(0).getMessage().getContent();
        mongoMessageRepository.saveAll(
                List.of(new MongoMessage(telegramChatId, "user", messageText),
                        new MongoMessage(telegramChatId, "assistant", response)));

        updateUser(telegramChatId,index+1);
        return response;
    }
    @Transactional
    String otherMessages(ChatGPTRequest chatGPTRequest, String messageText, String telegramChatId, int index) {
        List<MongoMessage> mongoMessages = mongoMessageRepository.findByTelegramChatId(telegramChatId);
        List<Message> messages = new ArrayList<>();
        for (MongoMessage mongoMessage : mongoMessages) {
            Message chatMessage = new Message();
            chatMessage.setContent(mongoMessage.getContent());
            chatMessage.setRole(mongoMessage.getRole());
            messages.add(chatMessage);
        }
        messages.add(new Message("user", messageText));
        chatGPTRequest.setMessages(messages);
        log.info("Отправляю запрос {}", chatGPTRequest);
        String response = chatRestClient.post(chatGPTRequest, gptToken).getBody().getChoices().get(0).getMessage().getContent();
        mongoMessageRepository.saveAll(
                List.of(new MongoMessage(telegramChatId, "user", messageText),
                        new MongoMessage(telegramChatId, "assistant", response)));
        updateUser(telegramChatId,index+1);
        return response;
    }

    private boolean checkGPTToken(String telegramChatId) {
        Users users = userRepository.findByTelegramChatId(telegramChatId);
        gptToken = users == null ? null : users.getGptTokenId();
        return gptToken != null;
    }

    private boolean saveUser(String telegramChatId, String gptTokenId){
        try {
            userRepository.save(new Users(telegramChatId, gptTokenId, 0));
        }catch (Exception e){
            System.out.println(e);
            return false;
        }
        return true;
    }

    private Integer getUsersIndex(String telegramChatId) {
        Users users = userRepository.findByTelegramChatId(telegramChatId);
        return users != null ? users.getCurrentMessageIndex() : -1;
    }

    private void updateUser(String telegramChatId, Integer currentMessageIndex) {
        Users users = userRepository.findByTelegramChatId(telegramChatId);
        users.setCurrentMessageIndex(currentMessageIndex);
        userRepository.save(users);
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
