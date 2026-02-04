package sk.tany.rest.api.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import sk.tany.rest.api.domain.auth.AuthorizationCode;
import sk.tany.rest.api.domain.auth.MagicLinkToken;
import sk.tany.rest.api.domain.auth.MagicLinkTokenState;
import sk.tany.rest.api.domain.blog.Blog;
import sk.tany.rest.api.domain.brand.Brand;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierPrice;
import sk.tany.rest.api.domain.carrier.CarrierPriceRange;
import sk.tany.rest.api.domain.carrier.CarrierType;
import sk.tany.rest.api.domain.cart.Cart;
import sk.tany.rest.api.domain.cart.CartItem;
import sk.tany.rest.api.domain.cartdiscount.CartDiscount;
import sk.tany.rest.api.domain.cartdiscount.DiscountType;
import sk.tany.rest.api.domain.category.Category;
import sk.tany.rest.api.domain.common.Sequence;
import sk.tany.rest.api.domain.customer.Address;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.Role;
import sk.tany.rest.api.domain.emailnotification.EmailNotification;
import sk.tany.rest.api.domain.filter.FilterParameter;
import sk.tany.rest.api.domain.filter.FilterParameterType;
import sk.tany.rest.api.domain.filter.FilterParameterValue;
import sk.tany.rest.api.domain.homepage.HomepageGrid;
import sk.tany.rest.api.domain.homepage.SortField;
import sk.tany.rest.api.domain.homepage.SortOrder;
import sk.tany.rest.api.domain.jwk.JwkKey;
import sk.tany.rest.api.domain.onedrive.OneDriveToken;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderItem;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.order.OrderStatusHistory;
import sk.tany.rest.api.domain.pagecontent.PageContent;
import sk.tany.rest.api.domain.payment.BesteronPayment;
import sk.tany.rest.api.domain.payment.GlobalPaymentsPayment;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.payment.PaymentType;
import sk.tany.rest.api.domain.payment.enums.PaymentStatus;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductFilterParameter;
import sk.tany.rest.api.domain.product.ProductStatus;
import sk.tany.rest.api.domain.productlabel.ProductLabel;
import sk.tany.rest.api.domain.productlabel.ProductLabelPosition;
import sk.tany.rest.api.domain.productsales.ProductSales;
import sk.tany.rest.api.domain.review.Review;
import sk.tany.rest.api.domain.shopsettings.ShopSettings;
import sk.tany.rest.api.domain.supplier.Supplier;
import sk.tany.rest.api.domain.wishlist.Wishlist;
import sk.tany.rest.api.dto.PriceBreakDown;
import sk.tany.rest.api.dto.PriceItem;
import sk.tany.rest.api.dto.PriceItemType;

// TODO can be removed
public class NitriteReflectionHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        registerEntities(hints,
            // Entities
            Supplier.class,
            HomepageGrid.class,
            OneDriveToken.class,
            EmailNotification.class,
            JwkKey.class,
            CartDiscount.class,
            Sequence.class,
            FilterParameter.class,
            FilterParameterValue.class,
            Product.class,
            MagicLinkToken.class,
            AuthorizationCode.class,
            Review.class,
            Wishlist.class,
            PageContent.class,
            ProductSales.class,
            ProductLabel.class,
            ShopSettings.class,
            Category.class,
            Carrier.class,
            Blog.class,
            Customer.class,
            Brand.class,
            Order.class,
            Cart.class,
            Payment.class,
            BesteronPayment.class,
            GlobalPaymentsPayment.class,

            // Supporting Classes
            SortOrder.class,
            SortField.class,
            DiscountType.class,
            FilterParameterType.class,
            ProductFilterParameter.class,
            ProductStatus.class,
            MagicLinkTokenState.class,
            ProductLabelPosition.class,
            CarrierType.class,
            CarrierPrice.class,
            CarrierPriceRange.class,
            Address.class,
            Role.class,
            OrderStatusHistory.class,
            OrderItem.class,
            OrderStatus.class,
            CartItem.class,
            PaymentStatus.class,
            PaymentType.class,
            PriceBreakDown.class,
            PriceItem.class,
            PriceItemType.class
        );
    }

    private void registerEntities(RuntimeHints hints, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            hints.reflection().registerType(clazz,
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS,
                    MemberCategory.DECLARED_FIELDS);
        }
    }
}
