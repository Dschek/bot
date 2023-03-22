package tydiru.bot.chatgpt.db.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tydiru.bot.chatgpt.db.dto.MongoMessage;

import java.util.List;

@Repository
public interface MongoMessageRepository extends MongoRepository<MongoMessage, String> {
    List<MongoMessage> findByTelegramChatId(String telegramChatId);
}
