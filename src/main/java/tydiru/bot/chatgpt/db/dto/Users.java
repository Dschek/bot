package tydiru.bot.chatgpt.db.dto;

import lombok.Data;

@Data
public class Users {
    private Long telegramChatId;
    private String gptTokenId;
}
