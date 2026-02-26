package sk.tany.rest.api.service.chat;

import dev.langchain4j.service.SystemMessage;

/**
 * Interface for the AI Order Assistant.
 * Configured manually in {@link sk.tany.rest.api.config.AiConfig}.
 */
public interface OrderAssistant {

    @SystemMessage("You are a helpful customer support assistant. You can check order statuses for users. " +
            "If the user provides an order ID and email or phone number, use the 'checkOrderStatus' tool to verify the status. " +
            "If the tool returns 'Order not found', inform the user. " +
            "If the tool returns a status, explain it to the user.")
    String chat(String userMessage);
}
