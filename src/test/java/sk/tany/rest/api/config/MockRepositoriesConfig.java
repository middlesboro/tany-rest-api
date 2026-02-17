package sk.tany.rest.api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @MockBean
    private BlogRepository blogRepository;
    @MockBean
    private BrandRepository brandRepository;
    @MockBean
    private CarrierRepository carrierRepository;
    @MockBean
    private CartRepository cartRepository;
    @MockBean
    private CartDiscountRepository cartDiscountRepository;
    @MockBean
    private CategoryRepository categoryRepository;
    @MockBean
    private SequenceRepository sequenceRepository;
    @MockBean
    private CustomerRepository customerRepository;
    @MockBean
    private EmailNotificationRepository emailNotificationRepository;
    @MockBean
    private FilterParameterRepository filterParameterRepository;
    @MockBean
    private FilterParameterValueRepository filterParameterValueRepository;
    @MockBean
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

    @MockBean
    private OneDriveTokenRepository oneDriveTokenRepository;
    @MockBean
    private OrderRepository orderRepository;
    @MockBean
    private PageContentRepository pageContentRepository;
    @MockBean
    private BesteronPaymentRepository besteronPaymentRepository;
    @MockBean
    private GlobalPaymentsPaymentRepository globalPaymentsPaymentRepository;
    @MockBean
    private PaymentRepository paymentRepository;
    @MockBean
    private ProductRepository productRepository;
    @MockBean
    private ProductLabelRepository productLabelRepository;
    @MockBean
    private ProductSalesRepository productSalesRepository;
    @MockBean
    private ReviewRepository reviewRepository;
    @MockBean
    private ShopSettingsRepository shopSettingsRepository;
    @MockBean
    private SupplierRepository supplierRepository;
    @MockBean
    private WishlistRepository wishlistRepository;
    @MockBean
    private AuthorizationCodeRepository authorizationCodeRepository;
    @MockBean
    private MagicLinkTokenRepository magicLinkTokenRepository;

    @MockBean
    private com.mongodb.client.MongoClient mongoClient;

    @MockBean
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
