package tydiru.bot.chatgpt.db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class Usage {

    @JsonProperty("prompt_tokens")
    String promptTokens;

    @JsonProperty("completion_tokens")
    String completionTokens;

    @JsonProperty("total_tokens")
    String totalTokens;
}
