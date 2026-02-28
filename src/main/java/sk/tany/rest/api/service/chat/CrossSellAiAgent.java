package sk.tany.rest.api.service.chat;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import sk.tany.rest.api.dto.CrossSellProductsResponse;

import java.util.List;

/**
 * Interface for the AI Cross-Sell Assistant.
 * Configured manually in {@link sk.tany.rest.api.config.AiConfig}.
 */
public interface CrossSellAiAgent extends CrossSellAssistant {

    @SystemMessage("You are a helpful assistant for finding cross-sell products based on a product title. " +
            "Given a product title, determine the best related products based on specific rules. " +
            "Use the CrossSellTools to find products. " +
            "Strictly follow these rules in order: " +
            "1. If product title contains 'henna' (hair color): search for 'prirodny sampon'. If < 3 results, add products from search 'sampon'. If still < 3, add products from search 'esencialny'. " +
            "2. If product title contains 'hair color' but NOT 'henna': search for 'biokap sampon'. If < 3 results, add products from search 'biokap kondicioner'. If still < 3, add products from search 'prirodny sampon'. " +
            "3. If product title contains 'vonne tycinky': search for 'stojan'. If < 3 results, add products from search 'vonne tycinky'. " +
            "4. If product title contains 'tuhy sampon': search for 'mydelnicka'. If < 3 results, add products from search 'mydlo'. " +
            "5. If none of the above match: search for 'sojova sviecka'. If < 3 results, add products from search 'kefka'. If still < 3, add products from search 'vrecusko'. " +
            "Return exactly 3 products if possible. Do not return the source product itself.")
    @UserMessage("Find cross-sell products for: {{productTitle}}. Pass excluded ids: {{excludeIds}} to tool to avoid duplicates. Provide the final list of 3 products in JSON format.")
    CrossSellProductsResponse findCrossSellProducts(@V("productTitle") String productTitle, @V("excludeIds") List<String> excludeIds);
}
