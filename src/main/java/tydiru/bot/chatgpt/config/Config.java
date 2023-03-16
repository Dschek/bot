package tydiru.bot.chatgpt.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("application.yaml")
public class Config {
    @Value("${telegram.bot.name}")
    private String botUsername;
    @Value("${telegram.bot.token}")
    private String botToken;
}
