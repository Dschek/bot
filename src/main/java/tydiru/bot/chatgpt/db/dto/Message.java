package tydiru.bot.chatgpt.db.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class Message {

    String role;
    String content;
}
