package tydiru.bot.chatgpt.db.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
public class ChatGptResponse {
    String id;
    String object;
    String created;
    List<Choice> choises;
    Usage usage;
}
