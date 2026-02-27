package sk.tany.rest.api.service.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.dto.CrossSellProductDto;
import sk.tany.rest.api.dto.CrossSellProductsResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "tany.ai.cross-sell.provider", havingValue = "java", matchIfMissing = true)
public class CrossSellAssistantJavaImpl implements CrossSellAssistant {

    private final CrossSellTools crossSellTools;

    @Override
    public CrossSellProductsResponse findCrossSellProducts(String productTitle, List<String> excludeIds) {
        if (productTitle == null) {
            CrossSellProductsResponse response = new CrossSellProductsResponse();
            response.setProducts(new ArrayList<>());
            return response;
        }

        List<CrossSellProductDto> resultProducts = new ArrayList<>();
        Set<String> excludedProductIds = new HashSet<>(excludeIds != null ? excludeIds : List.of());
        String lowerCaseTitle = productTitle.toLowerCase();

        if (lowerCaseTitle.contains("henna")) {
            addProducts("prirodny sampon", excludedProductIds, resultProducts);
            if (resultProducts.size() < 3) addProducts("sampon", excludedProductIds, resultProducts);
            if (resultProducts.size() < 3) addProducts("esencialny", excludedProductIds, resultProducts);
        } else if (lowerCaseTitle.contains("hair color")) {
            addProducts("biokap sampon", excludedProductIds, resultProducts);
            if (resultProducts.size() < 3) addProducts("biokap kondicioner", excludedProductIds, resultProducts);
            if (resultProducts.size() < 3) addProducts("prirodny sampon", excludedProductIds, resultProducts);
        } else if (lowerCaseTitle.contains("vonne tycinky")) {
            addProducts("stojan", excludedProductIds, resultProducts);
            if (resultProducts.size() < 3) addProducts("vonne tycinky", excludedProductIds, resultProducts);
        } else if (lowerCaseTitle.contains("tuhy sampon")) {
            addProducts("mydelnicka", excludedProductIds, resultProducts);
            if (resultProducts.size() < 3) addProducts("mydlo", excludedProductIds, resultProducts);
        } else {
            addProducts("sojova sviecka", excludedProductIds, resultProducts);
            if (resultProducts.size() < 3) addProducts("kefka", excludedProductIds, resultProducts);
            if (resultProducts.size() < 3) addProducts("vrecusko", excludedProductIds, resultProducts);
        }

        CrossSellProductsResponse response = new CrossSellProductsResponse();
        response.setProducts(resultProducts);
        return response;
    }

    private void addProducts(String query, Set<String> excludedProductIds, List<CrossSellProductDto> resultProducts) {
        if (resultProducts.size() >= 3) {
            return;
        }

        List<CrossSellProductDto> foundProducts = crossSellTools.searchProducts(query, new ArrayList<>(excludedProductIds));

        for (CrossSellProductDto product : foundProducts) {
            if (resultProducts.size() >= 3) {
                break;
            }
            if (!excludedProductIds.contains(product.getId())) {
                resultProducts.add(product);
                excludedProductIds.add(product.getId());
            }
        }
    }
}
