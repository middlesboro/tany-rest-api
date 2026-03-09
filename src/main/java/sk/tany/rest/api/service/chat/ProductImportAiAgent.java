package sk.tany.rest.api.service.chat;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import sk.tany.rest.api.dto.admin.product.create.ProductImportUrlAiResponse;

public interface ProductImportAiAgent {
    @SystemMessage("You are an expert at extracting structured product data from raw HTML or text content. " +
            "Your task is to analyze the provided text content of a product page and extract the product details. " +
            "Return the data in the requested JSON format. " +
            "Try to find the title," +
            "price (price will be near quantities or add to cart button. usually with euro currency symbol with dot or comma. e.g. €10,50 or 9.5€. remove currency symbol.)," +
            "description (format description, add tag for bold if you think it's important), images, EAN." +
            "If a specific field is not found or cannot be determined, leave it empty or null.")
    @UserMessage("Extract product data from the following content:\n\n{{content}}")
    ProductImportUrlAiResponse extractProductData(@V("content") String content);
}
