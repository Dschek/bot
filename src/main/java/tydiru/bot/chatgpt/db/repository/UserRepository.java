package tydiru.bot.chatgpt.db.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tydiru.bot.chatgpt.db.dto.Users;

@Repository
public interface UserRepository extends MongoRepository<Users, String> {
    Users findByTelegramChatId(String telegramChatId);
}