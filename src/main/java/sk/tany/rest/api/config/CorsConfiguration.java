package sk.tany.rest.api.config;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
@NoArgsConstructor
public class CorsConfiguration implements WebMvcConfigurer {

    @Value("${cors.configs.allowed-origins:}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (StringUtils.isNotBlank(allowedOrigins)) {
             registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        if (StringUtils.isNotBlank(allowedOrigins)) {
            configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).toList());
        }
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS", "PUT", "PATCH", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
