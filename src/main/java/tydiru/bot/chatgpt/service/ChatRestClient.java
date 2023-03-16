package tydiru.bot.chatgpt.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import tydiru.bot.chatgpt.db.dto.ChatGPTRequest;
import tydiru.bot.chatgpt.db.dto.ChatGptResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatRestClient {

    @Autowired
    private final ObjectMapper objectMapper;



    public ChatGptResponse post(ChatGPTRequest request, String token) {
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + token);

        try {
            HttpEntity<ChatGPTRequest> requestEntity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response
                    = restTemplate.exchange(fooResourceUrl, HttpMethod.POST, requestEntity, String.class);
            JavaType javaType = objectMapper.getTypeFactory().constructType(ChatGptResponse.class);

            return objectMapper.readValue(response.getBody(), javaType);
        } catch (HttpServerErrorException e) {
            log.error("Вызов завершился ошибкой с кодом {}: {}", e.getStatusCode(), e.getMessage());
            return null;
        } catch (JsonProcessingException e) {
            log.error("Маппинг ответа завершился ошибкой : {}", e.getMessage());
            return null;
        }
    }
}
