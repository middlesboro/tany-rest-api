package sk.tany.rest.api.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sk.tany.rest.api.service.chat.OrderAssistant;
import sk.tany.rest.api.service.chat.OrderTools;

@Configuration
public class AiConfig {

    @Value("${langchain4j.google-ai-gemini.api-key:demo}")
    private String googleAiApiKey;

    @Bean
    public ChatModel chatLanguageModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(googleAiApiKey)
                .modelName("gemini-1.5-flash")
                .build();
    }

    @Bean
    public OrderAssistant orderAssistant(ChatModel chatLanguageModel, OrderTools orderTools) {
        return AiServices.builder(OrderAssistant.class)
                .chatModel(chatLanguageModel)
                .tools(orderTools)
                .build();
    }
}
