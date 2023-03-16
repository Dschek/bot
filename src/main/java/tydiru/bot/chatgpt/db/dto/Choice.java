package tydiru.bot.chatgpt.db.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class Choice {
    int index;
    Message message;
    String finishReason;
}
