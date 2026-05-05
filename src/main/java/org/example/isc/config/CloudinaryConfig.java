package org.example.isc.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.url:}")
    private String cloudinaryUrl;

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        applyCloudinaryUrl(config);
        putIfPresent(config, "cloud_name", cloudName);
        putIfPresent(config, "api_key", apiKey);
        putIfPresent(config, "api_secret", apiSecret);
        config.put("secure", "true");

        return new Cloudinary(config);
    }

    private void applyCloudinaryUrl(Map<String, String> config) {
        if (!StringUtils.hasText(cloudinaryUrl)) {
            return;
        }

        URI uri = URI.create(cloudinaryUrl);
        if (!"cloudinary".equals(uri.getScheme())) {
            throw new IllegalStateException("CLOUDINARY_URL must start with cloudinary://");
        }

        String userInfo = uri.getRawUserInfo();
        if (!StringUtils.hasText(userInfo) || !StringUtils.hasText(uri.getHost())) {
            throw new IllegalStateException("CLOUDINARY_URL must include api key, api secret, and cloud name");
        }

        int separator = userInfo.indexOf(':');
        if (separator < 0) {
            throw new IllegalStateException("CLOUDINARY_URL must include both api key and api secret");
        }

        config.put("api_key", decode(userInfo.substring(0, separator)));
        config.put("api_secret", decode(userInfo.substring(separator + 1)));
        config.put("cloud_name", decode(uri.getHost()));
    }

    private void putIfPresent(Map<String, String> config, String key, String value) {
        if (StringUtils.hasText(value)) {
            config.put(key, value);
        }
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
