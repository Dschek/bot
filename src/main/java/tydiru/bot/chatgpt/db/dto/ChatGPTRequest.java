package tydiru.bot.chatgpt.db.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
public class ChatGPTRequest {
    String model;
    List<Message> messages;
}
