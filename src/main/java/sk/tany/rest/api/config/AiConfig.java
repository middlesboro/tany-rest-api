package sk.tany.rest.api.config;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sk.tany.rest.api.service.chat.CrossSellAiAgent;
import sk.tany.rest.api.service.chat.CrossSellAssistant;
import sk.tany.rest.api.service.chat.CrossSellTools;
import sk.tany.rest.api.service.chat.OrderAssistant;
import sk.tany.rest.api.service.chat.OrderTools;

@Configuration
public class AiConfig {

    @Value("${tany.ai.provider:gemini}")
    private String aiProvider;

    @Value("${langchain4j.google-ai-gemini.api-key}")
    private String googleAiApiKey;

    @Value("${langchain4j.mistral.api-key}")
    private String mistralApiKey;

    @Bean
    public ChatModel chatModel() {
        if ("mistral".equalsIgnoreCase(aiProvider)) {
            return MistralAiChatModel.builder()
                    .apiKey(mistralApiKey)
                    .modelName("mistral-small-latest")
                    .build();
        }
        return GoogleAiGeminiChatModel.builder()
                .apiKey(googleAiApiKey)
                .modelName("gemini-2.5-flash-lite")
                .build();
    }

    @Bean
    public OrderAssistant orderAssistant(ChatModel chatModel, OrderTools orderTools) {
        return AiServices.builder(OrderAssistant.class)
                .chatModel(chatModel)
                // add chat memory to maintain context across interactions?
                .tools(orderTools)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "tany.ai.cross-sell.provider", havingValue = "ai")
    public CrossSellAssistant crossSellAssistant(ChatModel chatModel, CrossSellTools crossSellTools) {
        return AiServices.builder(CrossSellAiAgent.class)
                .chatModel(chatModel)
                .tools(crossSellTools)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }
}
