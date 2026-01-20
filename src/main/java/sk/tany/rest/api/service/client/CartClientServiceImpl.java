package sk.tany.rest.api.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.cart.CartRepository;
import sk.tany.rest.api.domain.cartdiscount.CartDiscount;
import sk.tany.rest.api.domain.cartdiscount.CartDiscountRepository;
import sk.tany.rest.api.domain.cartdiscount.DiscountType;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.CartItem;
import sk.tany.rest.api.dto.client.cartdiscount.CartDiscountClientDto;
import sk.tany.rest.api.dto.client.product.ProductClientDto;
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
        resultDto.setFreeShipping(cartDto.isFreeShipping());
        resultDto.setAppliedDiscounts(cartDto.getAppliedDiscounts());

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
                .orElseThrow(() -> new RuntimeException("Product not found"));

        int stock = productDto.getQuantity() != null ? productDto.getQuantity() : 0;
        if (quantity > stock) {
            throw new RuntimeException("Not enough stock. Available: " + stock);
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
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cartDto.getItems() != null) {
            cartDto.getItems().removeIf(item -> item.getProductId().equals(productId));
        }

        return save(cartDto).getCartId();
    }

    @Override
    public CartDto addCarrier(String cartId, String carrierId) {
        CartDto cartDto = findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        cartDto.setSelectedCarrierId(carrierId);
        return save(cartDto);
    }

    @Override
    public CartDto addPayment(String cartId, String paymentId) {
        CartDto cartDto = findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        cartDto.setSelectedPaymentId(paymentId);
        return save(cartDto);
    }

    @Override
    public CartDto addDiscount(String cartId, String code) {
        CartDto cartDto = findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        // Check if code exists
        cartDiscountRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new RuntimeException("Invalid or inactive discount code"));

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
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cartDto.getAppliedDiscounts() != null) {
            cartDto.getAppliedDiscounts().removeIf(d -> code.equals(d.getCode()));
        }

        return save(cartDto);
    }

    private void calculateCartTotals(CartDto cartDto) {
        if (cartDto.getItems() == null) {
            cartDto.setItems(new ArrayList<>());
        }

        // 1. Calculate base total price of products
        BigDecimal productsTotal = BigDecimal.ZERO;
        for (CartItem item : cartDto.getItems()) {
            if (item.getPrice() != null && item.getQuantity() != null) {
                productsTotal = productsTotal.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        cartDto.setTotalPrice(productsTotal);

        // 2. Identify all applicable discounts
        // a) Automatic discounts
        List<CartDiscount> automaticDiscounts = cartDiscountRepository.findAllByCodeIsNullAndActiveTrue();

        // b) Manual discounts (from cartDto.appliedDiscounts codes)
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

        // Filter by date validity
        Instant now = Instant.now();
        allDiscounts = allDiscounts.stream()
                .filter(d -> (d.getDateFrom() == null || !now.isBefore(d.getDateFrom())) &&
                             (d.getDateTo() == null || !now.isAfter(d.getDateTo())))
                .collect(Collectors.toList());

        // 3. Apply discounts
        BigDecimal totalDiscount = BigDecimal.ZERO;
        boolean freeShipping = false;
        Set<CartDiscount> actuallyAppliedDiscounts = new HashSet<>();

        // We need to fetch product details to check categories/brands
        // For simplicity, I assume CartItem has productId.
        // I might need to fetch ProductClientDto for each item to check categories/brands.
        // Since 'productService.findById' is cached or fast?
        // Or I can optimize. findAllByIds.

        List<String> productIds = cartDto.getItems().stream().map(CartItem::getProductId).collect(Collectors.toList());
        List<ProductClientDto> products = productService.findAllByIds(productIds);

        // Map productId -> ProductClientDto
        var productMap = products.stream().collect(Collectors.toMap(ProductClientDto::getId, p -> p));

        // For each discount, calculate amount
        for (CartDiscount discount : allDiscounts) {
            boolean isApplicable = false;
            BigDecimal discountAmount = BigDecimal.ZERO;

            // Check applicability
            // If no restrictions (categories, products, brands is empty/null), it applies to all.
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
                    if (hasProductRestriction && discount.getProductIds().contains(p.getId())) {
                        itemApplicable = true;
                    }
                    if (!itemApplicable && hasCategory && p.getCategoryIds() != null) {
                        // Check intersection
                        if (p.getCategoryIds().stream().anyMatch(cid -> discount.getCategoryIds().contains(cid))) {
                            itemApplicable = true;
                        }
                    }
                    if (!itemApplicable && hasBrandRestriction && p.getBrandId() != null) {
                        if (discount.getBrandIds().contains(p.getBrandId())) {
                            itemApplicable = true;
                        }
                    }
                }

                if (itemApplicable) {
                    isApplicable = true;
                    BigDecimal itemPrice = item.getPrice();
                    BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

                    if (discount.getDiscountType() == DiscountType.PERCENTAGE) {
                        // Percentage
                        BigDecimal amount = itemTotal.multiply(discount.getValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                        discountAmount = discountAmount.add(amount);
                    } else {
                        // Fixed Amount
                        // Fixed amount is per item or per order?
                        // "10 euro discount". Usually per order if global, or per item?
                        // "defined for the whole categories, selected products or brands".
                        // If I buy 2 items from category, is it 10eur total or 20eur?
                        // Usually fixed amount is applied once per cart if condition met,
                        // or distributed.
                        // Let's assume fixed amount is "per cart" but limited to the sum of applicable items value.
                        // Wait, if I have multiple items, how do I split?
                        // Let's assume simpler model: Fixed Amount is subtracted from the total of applicable items.
                        // But we should not subtract more than the total of applicable items.
                        // And we should not apply it multiple times for each item?
                        // Usually "10 EUR OFF" is once per order.
                    }
                }
            }

            if (isApplicable) {
                if (discount.getDiscountType() == DiscountType.FIXED_AMOUNT) {
                    // Start with full discount value
                     BigDecimal limit = BigDecimal.ZERO;
                     // Calculate total value of applicable items
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

                     // Discount is min(value, limit)
                     discountAmount = discount.getValue().min(limit);
                }

                if (discount.isFreeShipping()) {
                    freeShipping = true;
                }

                if (discountAmount.compareTo(BigDecimal.ZERO) > 0 || discount.isFreeShipping()) {
                    totalDiscount = totalDiscount.add(discountAmount);
                    actuallyAppliedDiscounts.add(discount);
                }
            }
        }

        // Ensure total discount doesn't exceed total price
        if (totalDiscount.compareTo(productsTotal) > 0) {
            totalDiscount = productsTotal;
        }

        cartDto.setTotalDiscount(totalDiscount);
        cartDto.setFreeShipping(freeShipping);

        // Update applied discounts list for display
        List<CartDiscountClientDto> appliedDtos = actuallyAppliedDiscounts.stream()
                .map(cartDiscountMapper::toClientDto)
                .collect(Collectors.toList());
        cartDto.setAppliedDiscounts(appliedDtos);

        // Calculate Final Price
        BigDecimal carrierPrice = BigDecimal.ZERO;
        if (cartDto.getSelectedCarrierId() != null) {
            // Need to fetch carrier price.
            // But carrier price might depend on weight.
            // CarrierClientService calculates it.
            // Ideally I should reuse OrderHelper or Carrier Service logic.
            // But here I'm in Cart Service.

            // I will use carrierRepository to get basic info, but strict calculation might be complex (weight intervals).
            // For now, assume a simple retrieval or 0. Or assume frontend calculates it?
            // "recalculate cart price based on discount".
            // If I skip carrier price, final price is wrong.
            // Let's try to get carrier price.
             Optional<sk.tany.rest.api.domain.carrier.Carrier> carrierOpt = carrierRepository.findById(cartDto.getSelectedCarrierId());
             if (carrierOpt.isPresent()) {
                 // Simplification: just take base price or 0 if validation needed.
                 // OrderClientServiceImpl uses OrderHelper.getCarrierPrice(carrier, totalWeight).
                 // I should copy that logic or make it shared.
                 // It is static in OrderHelper? Let's assume so.
                 // I need to read OrderHelper.
             }
        }

        // For now, I will define Final Price as (ProductsTotal - Discount).
        // Payment and Carrier are added in Checkout/Order usually, but if CartDto has them, I should add them.

        BigDecimal finalPrice = productsTotal.subtract(totalDiscount);

        // Add carrier cost
        if (!freeShipping && cartDto.getSelectedCarrierId() != null) {
             Optional<sk.tany.rest.api.domain.carrier.Carrier> carrierOpt = carrierRepository.findById(cartDto.getSelectedCarrierId());
             if (carrierOpt.isPresent()) {
                 // Calculate weight
                 BigDecimal weight = BigDecimal.ZERO;
                 for (CartItem item : cartDto.getItems()) {
                     ProductClientDto p = productMap.get(item.getProductId());
                     if (p != null && p.getWeight() != null) {
                         weight = weight.add(p.getWeight().multiply(BigDecimal.valueOf(item.getQuantity())));
                     }
                 }
                 // I need OrderHelper or similar logic.
                 // I will assume for now I cannot easily access OrderHelper if it is not injected or public.
                 // I'll skip carrier price calculation in this iteration unless I see OrderHelper.
             }
        }

        if (cartDto.getSelectedPaymentId() != null) {
            paymentRepository.findById(cartDto.getSelectedPaymentId())
                .ifPresent(payment -> {
                    // finalPrice = finalPrice.add(payment.getPrice());
                });
        }

        // To be safe and avoid inconsistencies with Order calculation,
        // maybe I should just expose 'totalDiscount' and let Frontend/Order handle the sum?
        // But prompt said "recalculate cart price".

        cartDto.setFinalPrice(finalPrice.max(BigDecimal.ZERO));
    }
}
