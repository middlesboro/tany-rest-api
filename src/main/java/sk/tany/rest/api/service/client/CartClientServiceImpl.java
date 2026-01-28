package sk.tany.rest.api.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.carrier.CarrierPriceRange;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.cart.CartRepository;
import sk.tany.rest.api.domain.cartdiscount.CartDiscount;
import sk.tany.rest.api.domain.cartdiscount.CartDiscountRepository;
import sk.tany.rest.api.domain.cartdiscount.DiscountType;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.CartItem;
import sk.tany.rest.api.dto.PriceBreakDown;
import sk.tany.rest.api.dto.PriceItem;
import sk.tany.rest.api.dto.PriceItemType;
import sk.tany.rest.api.dto.client.cartdiscount.CartDiscountClientDto;
import sk.tany.rest.api.dto.client.product.ProductClientDto;
import sk.tany.rest.api.exception.CartDiscountException;
import sk.tany.rest.api.exception.CartException;
import sk.tany.rest.api.exception.ProductException;
import sk.tany.rest.api.mapper.CartDiscountMapper;
import sk.tany.rest.api.mapper.CartMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartClientServiceImpl implements CartClientService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final ProductClientService productService;
    private final CartDiscountRepository cartDiscountRepository;
    private final CartDiscountMapper cartDiscountMapper;
    private final CarrierRepository carrierRepository;
    private final PaymentRepository paymentRepository;

    public CartDto getOrCreateCart(String cartId, String customerId) {
        CartDto cartDto = null;
        if (cartId != null) {
            cartDto = findById(cartId).orElse(null);
        }

        if (cartDto == null) {
            cartDto = new CartDto();
            cartDto.setItems(new ArrayList<>());
        }

        if (customerId != null) {
            cartDto.setCustomerId(customerId);
        }

        return save(cartDto);
    }

    public CartDto save(CartDto cartDto) {
        sk.tany.rest.api.domain.cart.Cart cart;
        if (cartDto.getCartId() != null) {
            cart = cartRepository.findById(cartDto.getCartId()).orElse(new sk.tany.rest.api.domain.cart.Cart());
        } else {
            cart = new sk.tany.rest.api.domain.cart.Cart();
        }

        // Ensure discount codes list is initialized
        if (cartDto.getAppliedDiscounts() != null) {
             List<String> codes = cartDto.getAppliedDiscounts().stream().map(CartDiscountClientDto::getCode).collect(Collectors.toList());
             cart.setDiscountCodes(codes);
        }

        // We need to map other fields too. cartMapper does it, but we modified Cart to have discountCodes,
        // and CartDto to have appliedDiscounts.
        // We need to make sure consistency is kept.
        // Actually cartMapper.updateEntityFromDto might not handle the List<CartDiscountClientDto> -> List<String> conversion
        // because names don't match (appliedDiscounts vs discountCodes).
        // So I will handle it manually here or update mapper. I'll handle manually for now.

        cartMapper.updateEntityFromDto(cartDto, cart);

        // If the mapper overwrote discountCodes with null (if it tried to map appliedDiscounts to it), we restore it.
        // But CartDto doesn't have discountCodes field, so Mapper probably ignores it.
        // However, if CartDto has appliedDiscounts, and we want to persist codes.
        // I will rely on logic:
        // 1. Calculate prices and applied discounts.
        // 2. Update CartDto with results.
        // 3. Update Cart entity with discount codes derived from CartDto or logic.

        // Actually, 'save' method here is used both for persistence AND for returning the response.
        // The pattern seems to be:
        // 1. Load Entity
        // 2. Map Dto to Entity
        // 3. Save Entity
        // 4. Map Entity to Dto

        // Since I added 'discountCodes' to Cart, I need to make sure they are preserved.
        // CartDto doesn't have 'discountCodes' field, it has 'appliedDiscounts' (List<CartDiscountClientDto>).
        // So Mapper won't touch 'discountCodes' in Entity.

        // So I must set discountCodes in Entity manually if I want to update them.

        // Let's recalculate everything before saving.
        calculateCartTotals(cartDto);

        // Update entity codes from the calculated applied discounts (which includes manual codes)
        // Wait, automatic discounts shouldn't be saved as codes if they are automatic.
        // But the prompt says: "if code is not set it's applied automatically".
        // Manual codes are those with 'code' field.

        List<String> manualCodes = new ArrayList<>();
        if (cartDto.getAppliedDiscounts() != null) {
            manualCodes = cartDto.getAppliedDiscounts().stream()
                .filter(d -> d.getCode() != null)
                .map(CartDiscountClientDto::getCode)
                .collect(Collectors.toList());
        }
        cart.setDiscountCodes(manualCodes);

        sk.tany.rest.api.domain.cart.Cart savedCart = cartRepository.save(cart);
        CartDto resultDto = cartMapper.toDto(savedCart);

        // Re-apply calculations to the result DTO because Mapper won't populate price fields as they are not in Entity
        // Or I should put price fields in Entity?
        // "implement logic to recalculate cart price".
        // If I don't store prices in DB, I have to calculate them every time I retrieve the cart.

        // I will copy the calculated values from cartDto to resultDto.
        resultDto.setTotalPrice(cartDto.getTotalPrice());
        resultDto.setTotalDiscount(cartDto.getTotalDiscount());
        resultDto.setFinalPrice(cartDto.getFinalPrice());
        resultDto.setAppliedDiscounts(cartDto.getAppliedDiscounts());
        resultDto.setPriceBreakDown(cartDto.getPriceBreakDown());

        return resultDto;
    }

    private Optional<CartDto> findById(String id) {
        return cartRepository.findById(id).map(cart -> {
            CartDto dto = cartMapper.toDto(cart);
            // We need to restore manually applied codes from entity to DTO to be used in calculation
            // But DTO uses List<CartDiscountClientDto>.
            // So we just load the codes, put them in a temporary place or just run calculation.

            // Actually, calculateCartTotals needs to know which manual codes are applied.
            // I should pass the manual codes from entity to the calculation method.

            // I'll create a transient field or just pass it.
            // But 'calculateCartTotals' takes 'cartDto'.
            // I'll populate 'appliedDiscounts' in DTO with dummy objects containing just codes,
            // and let the calculator validate and expand them.

            List<String> codes = cart.getDiscountCodes();
            if (codes != null) {
                List<CartDiscountClientDto> dtos = codes.stream().map(code -> {
                    CartDiscountClientDto d = new CartDiscountClientDto();
                    d.setCode(code);
                    return d;
                }).collect(Collectors.toList());
                dto.setAppliedDiscounts(dtos);
            }

            calculateCartTotals(dto);
            return dto;
        });
    }

    public String addProductToCart(String cartId, String productId, Integer quantity) {
        CartDto cartDto = null;
        if (cartId != null) {
            cartDto = findById(cartId).orElse(null);
        }

        if (cartDto == null) {
            cartDto = new CartDto();
            cartDto.setItems(new ArrayList<>());
        }

        if (cartDto.getItems() == null) {
            cartDto.setItems(new ArrayList<>());
        }

        ProductClientDto productDto = productService.findById(productId)
                .orElseThrow(() -> new ProductException.NotFound("Product not found"));

        int stock = productDto.getQuantity() != null ? productDto.getQuantity() : 0;
        if (quantity > stock) {
            throw new CartException.BadRequest("Not enough stock. Available: " + stock);
        }

        String image = (productDto.getImages() != null && !productDto.getImages().isEmpty())
                ? productDto.getImages().get(0)
                : null;

        Optional<CartItem> existingItem = cartDto.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(quantity);
            item.setTitle(productDto.getTitle());
            item.setPrice(productDto.getPrice());
            item.setImage(image);
        } else {
            CartItem newItem = new CartItem(productId, quantity);
            newItem.setTitle(productDto.getTitle());
            newItem.setPrice(productDto.getPrice());
            newItem.setImage(image);
            cartDto.getItems().add(newItem);
        }

        return save(cartDto).getCartId();
    }

    @Override
    public String removeProductFromCart(String cartId, String productId) {
        CartDto cartDto = findById(cartId)
                .orElseThrow(() -> new CartException.NotFound("Cart not found"));

        if (cartDto.getItems() != null) {
            cartDto.getItems().removeIf(item -> item.getProductId().equals(productId));
        }

        return save(cartDto).getCartId();
    }

    @Override
    public CartDto addCarrier(String cartId, String carrierId) {
        CartDto cartDto = findById(cartId)
                .orElseThrow(() -> new CartException.NotFound("Cart not found"));
        cartDto.setSelectedCarrierId(carrierId);
        return save(cartDto);
    }

    @Override
    public CartDto addPayment(String cartId, String paymentId) {
        CartDto cartDto = findById(cartId)
                .orElseThrow(() -> new CartException.NotFound("Cart not found"));
        cartDto.setSelectedPaymentId(paymentId);
        return save(cartDto);
    }

    @Override
    public CartDto addDiscount(String cartId, String code) {
        CartDto cartDto = findById(cartId)
                .orElseThrow(() -> new CartException.NotFound("Cart not found"));

        // Check if code exists
        cartDiscountRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new CartDiscountException.NotFound("Invalid or inactive discount code"));

        // Add to applied discounts if not already present
        if (cartDto.getAppliedDiscounts() == null) {
            cartDto.setAppliedDiscounts(new ArrayList<>());
        }

        boolean alreadyApplied = cartDto.getAppliedDiscounts().stream()
                .anyMatch(d -> code.equals(d.getCode()));

        if (!alreadyApplied) {
            CartDiscountClientDto d = new CartDiscountClientDto();
            d.setCode(code);
            cartDto.getAppliedDiscounts().add(d);
        }

        return save(cartDto);
    }

    @Override
    public CartDto removeDiscount(String cartId, String code) {
        CartDto cartDto = findById(cartId)
                .orElseThrow(() -> new CartException.NotFound("Cart not found"));

        if (cartDto.getAppliedDiscounts() != null) {
            cartDto.getAppliedDiscounts().removeIf(d -> code.equals(d.getCode()));
        }

        return save(cartDto);
    }

    private void calculateCartTotals(CartDto cartDto) {
        if (cartDto.getItems() == null) {
            cartDto.setItems(new ArrayList<>());
        }

        PriceBreakDown breakdown = new PriceBreakDown();
        cartDto.setPriceBreakDown(breakdown);

        List<String> productIds = cartDto.getItems().stream().map(CartItem::getProductId).collect(Collectors.toList());
        List<ProductClientDto> products = productService.findAllByIds(productIds);
        var productMap = products.stream().collect(Collectors.toMap(ProductClientDto::getId, p -> p));

        // 1. Calculate base total price of products
        BigDecimal productsTotal = BigDecimal.ZERO;
        BigDecimal productsTotalWithoutVat = BigDecimal.ZERO;
        BigDecimal productsTotalVat = BigDecimal.ZERO;

        if (cartDto.getItems() == null || cartDto.getItems().isEmpty()) {
            breakdown.setTotalPrice(productsTotal);
            breakdown.setTotalPriceWithoutVat(productsTotalWithoutVat);
            breakdown.setTotalPriceVatValue(productsTotalVat);
            return;
        }

        // Update items with fresh prices from product map
        for (CartItem item : cartDto.getItems()) {
            ProductClientDto product = productMap.get(item.getProductId());
            if (product != null) {
                BigDecimal effectivePrice = product.getPrice();
                BigDecimal effectivePriceWithoutVat = product.getPriceWithoutVat() != null ? product.getPriceWithoutVat() : product.getPrice();

                if (product.getDiscountPrice() != null && product.getDiscountPrice().compareTo(BigDecimal.ZERO) >= 0) {
                    BigDecimal originalPrice = product.getPrice();
                    BigDecimal originalPriceWithoutVat = effectivePriceWithoutVat;

                    effectivePrice = product.getDiscountPrice();

                    if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) != 0) {
                        BigDecimal ratio = originalPriceWithoutVat.divide(originalPrice, 4, RoundingMode.HALF_UP);
                        effectivePriceWithoutVat = effectivePrice.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
                    } else {
                        effectivePriceWithoutVat = effectivePrice;
                    }
                }

                item.setPrice(effectivePrice);
                item.setImage((product.getImages() != null && !product.getImages().isEmpty()) ? product.getImages().get(0) : null);
                item.setTitle(product.getTitle());

                BigDecimal priceWithVat = effectivePrice.multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP);
                BigDecimal priceWithoutVat = effectivePriceWithoutVat
                        .multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP);
                BigDecimal vatValue = priceWithVat.subtract(priceWithoutVat).setScale(2, RoundingMode.HALF_UP);

                productsTotal = productsTotal.add(priceWithVat);
                productsTotalWithoutVat = productsTotalWithoutVat.add(priceWithoutVat);
                productsTotalVat = productsTotalVat.add(vatValue);

                breakdown.getItems().add(new PriceItem(PriceItemType.PRODUCT, product.getId(), product.getTitle(), item.getImage(), item.getQuantity(), priceWithVat, priceWithoutVat, vatValue));
            }
        }
        cartDto.setTotalPrice(productsTotal);

        // 2. Identify all applicable discounts
        Set<String> cartCategoryIds = new HashSet<>();
        Set<String> cartBrandIds = new HashSet<>();
        products.forEach(p -> {
            if (p.getCategoryIds() != null) cartCategoryIds.addAll(p.getCategoryIds());
            if (p.getBrandId() != null) cartBrandIds.add(p.getBrandId());
        });

        List<CartDiscount> automaticDiscounts = cartDiscountRepository.findApplicableAutomaticDiscounts(
            new HashSet<>(productIds), cartCategoryIds, cartBrandIds
        );

        List<String> manualCodes = new ArrayList<>();
        if (cartDto.getAppliedDiscounts() != null) {
            manualCodes = cartDto.getAppliedDiscounts().stream()
                    .map(CartDiscountClientDto::getCode)
                    .filter(code -> code != null)
                    .collect(Collectors.toList());
        }

        List<CartDiscount> manualDiscounts = new ArrayList<>();
        for (String code : manualCodes) {
            cartDiscountRepository.findByCodeAndActiveTrue(code).ifPresent(manualDiscounts::add);
        }

        List<CartDiscount> allDiscounts = new ArrayList<>();
        allDiscounts.addAll(automaticDiscounts);
        allDiscounts.addAll(manualDiscounts);

        Instant now = Instant.now();
        allDiscounts = allDiscounts.stream()
                .filter(d -> (d.getDateFrom() == null || !now.isBefore(d.getDateFrom())) &&
                        (d.getDateTo() == null || !now.isAfter(d.getDateTo())))
                .distinct()
                .collect(Collectors.toList());

        BigDecimal totalDiscount = BigDecimal.ZERO;
        boolean freeShipping = false;
        Set<CartDiscount> actuallyAppliedDiscounts = new HashSet<>();

        for (CartDiscount discount : allDiscounts) {
            boolean isApplicable = false;
            BigDecimal discountAmount = BigDecimal.ZERO;

            boolean hasCategory = discount.getCategoryIds() != null && !discount.getCategoryIds().isEmpty();
            boolean hasProductRestriction = discount.getProductIds() != null && !discount.getProductIds().isEmpty();
            boolean hasBrandRestriction = discount.getBrandIds() != null && !discount.getBrandIds().isEmpty();
            boolean global = !hasCategory && !hasProductRestriction && !hasBrandRestriction;

            for (CartItem item : cartDto.getItems()) {
                ProductClientDto p = productMap.get(item.getProductId());
                if (p == null) continue;

                boolean itemApplicable = false;
                if (global) {
                    itemApplicable = true;
                } else {
                    if (hasProductRestriction && discount.getProductIds().contains(p.getId())) itemApplicable = true;
                    if (!itemApplicable && hasCategory && p.getCategoryIds() != null && p.getCategoryIds().stream().anyMatch(cid -> discount.getCategoryIds().contains(cid))) itemApplicable = true;
                    if (!itemApplicable && hasBrandRestriction && p.getBrandId() != null && discount.getBrandIds().contains(p.getBrandId())) itemApplicable = true;
                }

                if (itemApplicable) {
                    isApplicable = true;
                    if (discount.getDiscountType() == DiscountType.PERCENTAGE) {
                        BigDecimal itemPrice = item.getPrice();
                        BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                        BigDecimal amount = itemTotal.multiply(discount.getValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                        discountAmount = discountAmount.add(amount);
                    }
                }
            }

            if (isApplicable) {
                if (discount.getDiscountType() == DiscountType.FIXED_AMOUNT) {
                    BigDecimal limit = BigDecimal.ZERO;
                    for (CartItem item : cartDto.getItems()) {
                        ProductClientDto p = productMap.get(item.getProductId());
                        if (p == null) continue;
                        boolean itemApplicable = false;
                        if (global) itemApplicable = true;
                        else {
                            if (hasProductRestriction && discount.getProductIds().contains(p.getId())) itemApplicable = true;
                            if (!itemApplicable && hasCategory && p.getCategoryIds() != null && p.getCategoryIds().stream().anyMatch(cid -> discount.getCategoryIds().contains(cid))) itemApplicable = true;
                            if (!itemApplicable && hasBrandRestriction && p.getBrandId() != null && discount.getBrandIds().contains(p.getBrandId())) itemApplicable = true;
                        }
                        if (itemApplicable) {
                            limit = limit.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                        }
                    }
                    discountAmount = discount.getValue().min(limit);
                }

                if (discount.getDiscountType() == DiscountType.FREE_SHIPPING) {
                    freeShipping = true;
                    actuallyAppliedDiscounts.add(discount);
                }

                if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
                    totalDiscount = totalDiscount.add(discountAmount);
                    actuallyAppliedDiscounts.add(discount);

                    if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal discWithVat = discountAmount.negate().setScale(2, RoundingMode.HALF_UP);
                        // Approximate VAT split for discount. Assuming average VAT rate of the cart.
                        BigDecimal totalVatRatio = productsTotalWithoutVat.compareTo(BigDecimal.ZERO) > 0
                            ? productsTotal.divide(productsTotalWithoutVat, 4, RoundingMode.HALF_UP)
                            : BigDecimal.ONE;
                        BigDecimal discWithoutVat = discWithVat.divide(totalVatRatio, 2, RoundingMode.HALF_UP);
                        BigDecimal discVatValue = discWithVat.subtract(discWithoutVat);

                        breakdown.getItems().add(new PriceItem(PriceItemType.DISCOUNT, discount.getId(), discount.getTitle() != null ? discount.getTitle() : discount.getCode(), 1, discWithVat, discWithoutVat, discVatValue));
                    }
                }
            }
        }

        if (totalDiscount.compareTo(productsTotal) > 0) {
            totalDiscount = productsTotal;
        }
        cartDto.setTotalDiscount(totalDiscount);

        List<CartDiscountClientDto> appliedDtos = actuallyAppliedDiscounts.stream()
                .map(cartDiscountMapper::toClientDto)
                .collect(Collectors.toList());
        cartDto.setAppliedDiscounts(appliedDtos);

        BigDecimal finalPrice = productsTotal.subtract(totalDiscount);

        // Carrier
        if (cartDto.getSelectedCarrierId() != null) {
             Optional<sk.tany.rest.api.domain.carrier.Carrier> carrierOpt = carrierRepository.findById(cartDto.getSelectedCarrierId());
             if (carrierOpt.isPresent()) {
                 sk.tany.rest.api.domain.carrier.Carrier carrier = carrierOpt.get();
                 BigDecimal weight = BigDecimal.ZERO;
                 for (CartItem item : cartDto.getItems()) {
                     ProductClientDto p = productMap.get(item.getProductId());
                     if (p != null && p.getWeight() != null) {
                         weight = weight.add(p.getWeight().multiply(BigDecimal.valueOf(item.getQuantity())));
                     }
                 }

                 final BigDecimal finalWeight = weight;
                 CarrierPriceRange finalPriceRange = carrier.getRanges().stream()
                         .filter(range ->
                                 (range.getWeightFrom() == null || finalWeight.compareTo(range.getWeightFrom()) >= 0) &&
                                         (range.getWeightTo() == null || finalWeight.compareTo(range.getWeightTo()) <= 0)
                         )
                         .findFirst().orElse(null);

                 if (finalPriceRange == null) {
                     throw new CartException.BadRequest("Carrier not available for the given cart weight");
                 }

                 boolean thresholdMet = finalPriceRange.getFreeShippingThreshold() != null &&
                         productsTotal.subtract(totalDiscount).compareTo(finalPriceRange.getFreeShippingThreshold()) >= 0;
                 boolean isFreeShipping = freeShipping || thresholdMet;

                 finalPrice =  isFreeShipping ? finalPrice : finalPrice.add(finalPriceRange.getPrice());
                 breakdown.getItems().add(
                         new PriceItem(
                                 PriceItemType.CARRIER,
                                 carrier.getId(),
                                 carrier.getName(),
                         1,
                                 isFreeShipping ? BigDecimal.ZERO : finalPriceRange.getPrice(),
                                 isFreeShipping ? BigDecimal.ZERO : finalPriceRange.getPriceWithoutVat(),
                                 isFreeShipping ? BigDecimal.ZERO : finalPriceRange.getVatValue())
                 );
             }
        }

        // Payment
        if (cartDto.getSelectedPaymentId() != null) {
            paymentRepository.findById(cartDto.getSelectedPaymentId())
                .ifPresent(payment -> {
                    BigDecimal paymentPrice = payment.getPrice();
                    BigDecimal paymentWithoutVat = payment.getPriceWithoutVat();
                    BigDecimal paymentVatValue = payment.getVatValue();

                    breakdown.getItems().add(new PriceItem(PriceItemType.PAYMENT, payment.getId(), payment.getName(), 1, paymentPrice, paymentWithoutVat, paymentVatValue));
                });

             Optional<sk.tany.rest.api.domain.payment.Payment> pOpt = paymentRepository.findById(cartDto.getSelectedPaymentId());
             if (pOpt.isPresent()) {
                 finalPrice = finalPrice.add(pOpt.get().getPrice());
             }
        }

        cartDto.setFinalPrice(finalPrice.max(BigDecimal.ZERO));

        // Finalize breakdown totals
        BigDecimal totalWithVat = BigDecimal.ZERO;
        BigDecimal totalWithoutVat = BigDecimal.ZERO;
        BigDecimal totalVatValue = BigDecimal.ZERO;

        for (PriceItem item : breakdown.getItems()) {
            totalWithVat = totalWithVat.add(item.getPriceWithVat());
            totalWithoutVat = totalWithoutVat.add(item.getPriceWithoutVat());
            totalVatValue = totalVatValue.add(item.getVatValue());
        }

        breakdown.setTotalPrice(totalWithVat);
        breakdown.setTotalPriceWithoutVat(totalWithoutVat);
        breakdown.setTotalPriceVatValue(totalVatValue);
    }
}
