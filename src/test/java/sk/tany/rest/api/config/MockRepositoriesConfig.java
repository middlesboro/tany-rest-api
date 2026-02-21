package sk.tany.rest.api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import sk.tany.rest.api.domain.blog.BlogRepository;
import sk.tany.rest.api.domain.jwk.JwkKey;
import java.util.Collections;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import sk.tany.rest.api.domain.brand.BrandRepository;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.cart.CartRepository;
import sk.tany.rest.api.domain.cartdiscount.CartDiscountRepository;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.domain.common.SequenceRepository;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.emailnotification.EmailNotificationRepository;
import sk.tany.rest.api.domain.filter.FilterParameterRepository;
import sk.tany.rest.api.domain.filter.FilterParameterValueRepository;
import sk.tany.rest.api.domain.homepage.HomepageGridRepository;
import sk.tany.rest.api.domain.jwk.JwkKeyRepository;
import sk.tany.rest.api.domain.onedrive.OneDriveTokenRepository;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.pagecontent.PageContentRepository;
import sk.tany.rest.api.domain.payment.BesteronPaymentRepository;
import sk.tany.rest.api.domain.payment.GlobalPaymentsPaymentRepository;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.productlabel.ProductLabelRepository;
import sk.tany.rest.api.domain.productsales.ProductSalesRepository;
import sk.tany.rest.api.domain.review.ReviewRepository;
import sk.tany.rest.api.domain.shopsettings.ShopSettingsRepository;
import sk.tany.rest.api.domain.supplier.SupplierRepository;
import sk.tany.rest.api.domain.wishlist.WishlistRepository;
import sk.tany.rest.api.domain.auth.AuthorizationCodeRepository;
import sk.tany.rest.api.domain.auth.MagicLinkTokenRepository;

@TestConfiguration
public class MockRepositoriesConfig {

    @MockitoBean
    private BlogRepository blogRepository;
    @MockitoBean
    private BrandRepository brandRepository;
    @MockitoBean
    private CarrierRepository carrierRepository;
    @MockitoBean
    private CartRepository cartRepository;
    @MockitoBean
    private CartDiscountRepository cartDiscountRepository;
    @MockitoBean
    private CategoryRepository categoryRepository;
    @MockitoBean
    private SequenceRepository sequenceRepository;
    @MockitoBean
    private CustomerRepository customerRepository;
    @MockitoBean
    private EmailNotificationRepository emailNotificationRepository;
    @MockitoBean
    private FilterParameterRepository filterParameterRepository;
    @MockitoBean
    private FilterParameterValueRepository filterParameterValueRepository;
    @MockitoBean
    private HomepageGridRepository homepageGridRepository;
    @Bean
    @Primary
    public JwkKeyRepository jwkKeyRepository() {
        JwkKeyRepository repo = mock(JwkKeyRepository.class);
        JwkKey key = new JwkKey();
        key.setKeyId("test-id");
        try {
            java.security.KeyPairGenerator keyPairGenerator = java.security.KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            java.security.KeyPair keyPair = keyPairGenerator.generateKeyPair();

            key.setPublicKey(java.util.Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
            key.setPrivateKey(java.util.Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate test keys", e);
        }

        when(repo.findAll()).thenReturn(Collections.singletonList(key));
        when(repo.save(org.mockito.ArgumentMatchers.any())).thenAnswer(i -> i.getArgument(0));
        return repo;
    }

    @MockitoBean
    private OneDriveTokenRepository oneDriveTokenRepository;
    @MockitoBean
    private OrderRepository orderRepository;
    @MockitoBean
    private PageContentRepository pageContentRepository;
    @MockitoBean
    private BesteronPaymentRepository besteronPaymentRepository;
    @MockitoBean
    private GlobalPaymentsPaymentRepository globalPaymentsPaymentRepository;
    @MockitoBean
    private PaymentRepository paymentRepository;
    @MockitoBean
    private ProductRepository productRepository;
    @MockitoBean
    private ProductLabelRepository productLabelRepository;
    @MockitoBean
    private ProductSalesRepository productSalesRepository;
    @MockitoBean
    private ReviewRepository reviewRepository;
    @MockitoBean
    private ShopSettingsRepository shopSettingsRepository;
    @MockitoBean
    private SupplierRepository supplierRepository;
    @MockitoBean
    private WishlistRepository wishlistRepository;
    @MockitoBean
    private AuthorizationCodeRepository authorizationCodeRepository;
    @MockitoBean
    private MagicLinkTokenRepository magicLinkTokenRepository;

    @MockitoBean
    private com.mongodb.client.MongoClient mongoClient;

    @MockitoBean
    private MongoDatabaseFactory mongoDatabaseFactory;

    @Bean
    @Primary
    public MongoMappingContext mongoMappingContext() {
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setSimpleTypeHolder(SimpleTypeHolder.DEFAULT);
        return mappingContext;
    }

    @Bean
    @Primary
    public MongoTemplate mongoTemplate(MongoMappingContext mappingContext) {
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        MongoConverter mongoConverter = mock(MongoConverter.class);

        when(mongoTemplate.getConverter()).thenReturn(mongoConverter);
        doReturn(mappingContext).when(mongoConverter).getMappingContext();
        return mongoTemplate;
    }

}
