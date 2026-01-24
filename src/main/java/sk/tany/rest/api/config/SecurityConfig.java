package sk.tany.rest.api.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import sk.tany.rest.api.config.security.MagicLinkLoginFilter;
import sk.tany.rest.api.config.security.PublicUrlTokenIgnorerFilter;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.jwk.JwkKey;
import sk.tany.rest.api.domain.jwk.JwkKeyRepository;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwkKeyRepository jwkKeyRepository;
    private final SecurityProperties securityProperties;

    @Value("${eshop.frontend-url}")
    private String frontendUrl;

    @Value("${eshop.frontend-admin-url}")
    private String frontendAdminUrl;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            MagicLinkLoginFilter magicLinkLoginFilter,
            SecurityContextRepository repo) throws Exception {

        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());

        // Integrácia tvojho Magic Linku priamo do Auth procesu
        http.addFilterBefore(magicLinkLoginFilter, SecurityContextHolderFilter.class);

        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint(frontendUrl + "/login"))
        );

        http.securityContext(context -> context.securityContextRepository(repo));

        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        );

        return http.build();
    }

    // --- 2. FILTER CHAIN PRE TVOJE API (RESOURCE SERVER) ---
    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, SecurityContextRepository repo) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .securityContext(context -> context.securityContextRepository(repo))
                .addFilterBefore(new PublicUrlTokenIgnorerFilter(securityProperties), BearerTokenAuthenticationFilter.class)
                .authorizeHttpRequests(authorize -> {
                    // Tvoje dynamické výnimky zo SecurityProperties
                    securityProperties.getExcludedUrls().forEach(url -> {
                        String[] parts = url.split(" ");
                        if (parts.length > 1) {
                            authorize.requestMatchers(HttpMethod.valueOf(parts[0]), parts[1]).permitAll();
                        } else {
                            authorize.requestMatchers(url).permitAll();
                        }
                    });
                    authorize.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
                        })
                );

        return http.build();
    }

    // --- ZDIEĽANÉ BEANY ---

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient publicClient = createClient("public-client", frontendUrl, "openid", "profile");
        RegisteredClient adminClient = createClient("admin-client", frontendAdminUrl, "openid", "profile");
        return new InMemoryRegisteredClientRepository(publicClient, adminClient);
    }

    private RegisteredClient createClient(String clientId, String redirectUri, String... scopes) {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(redirectUri + "/oauth/callback")
                .scopes(s -> s.addAll(List.of(scopes)))
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(securityProperties.getAccessTokenValidity()))
                        .build())
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .requireProofKey(true)
                        .build())
                .build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        JwkKey jwkKey = jwkKeyRepository.findAll().stream()
                .findFirst()
                .orElseGet(this::generateAndSaveRsaKey);

        RSAKey rsaKey = new RSAKey.Builder(getPublicKey(jwkKey.getPublicKey()))
                .privateKey(getPrivateKey(jwkKey.getPrivateKey()))
                .keyID(jwkKey.getKeyId())
                .build();

        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return (context) -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType()) || "id_token".equals(context.getTokenType().getValue())) {
                Authentication principal = context.getPrincipal();

                if (principal.getPrincipal() instanceof Customer customer) {
                    context.getClaims().subject(customer.getEmail());

                    context.getClaims().claims(claims -> {
                        claims.put("customerId", customer.getId());
                        Set<String> roles = AuthorityUtils.authorityListToSet(principal.getAuthorities());
                        claims.put("roles", roles);
                    });
                }
            }
        };
    }

    @Bean
    static GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    // --- POMOCNÉ METÓDY PRE KĽÚČE (Logika z pôvodnej triedy) ---

    private JwkKey generateAndSaveRsaKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            JwkKey jwkKey = new JwkKey();
            jwkKey.setKeyId(UUID.randomUUID().toString());
            jwkKey.setPublicKey(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
            jwkKey.setPrivateKey(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
            return jwkKeyRepository.save(jwkKey);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate keys", ex);
        }
    }

    private RSAPublicKey getPublicKey(String key) {
        try {
            byte[] byteKey = Base64.getDecoder().decode(key);
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(byteKey));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    private RSAPrivateKey getPrivateKey(String key) {
        try {
            byte[] byteKey = Base64.getDecoder().decode(key);
            return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(byteKey));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

}
