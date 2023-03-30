package tydiru.bot.chatgpt.db.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class MongoMessage {
    @Id
    String id;
    String telegramChatId;
    String role;
    String content;
}
