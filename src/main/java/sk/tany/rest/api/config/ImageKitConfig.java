package sk.tany.rest.api.config;

import io.imagekit.sdk.ImageKit;
import io.imagekit.sdk.config.Configuration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageKitConfig {

    private final ImageKitProperties imageKitProperties;

    @Bean
    public ImageKit imageKit() {
        ImageKit imageKit = ImageKit.getInstance();
        Configuration config = new Configuration(
                imageKitProperties.getPublicKey(),
                imageKitProperties.getPrivateKey(),
                imageKitProperties.getUrlEndpoint()
        );
        imageKit.setConfig(config);
        return imageKit;
    }
}
