package org.example.isc.config;

import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CloudinaryConfigTest {

    @Test
    void configuresCloudinaryFromUrl() {
        CloudinaryConfig config = new CloudinaryConfig();
        ReflectionTestUtils.setField(config, "cloudinaryUrl", "cloudinary://key:secret@demo-cloud");

        Cloudinary cloudinary = config.cloudinary();

        assertThat(cloudinary.config.apiKey).isEqualTo("key");
        assertThat(cloudinary.config.apiSecret).isEqualTo("secret");
        assertThat(cloudinary.config.cloudName).isEqualTo("demo-cloud");
        assertThat(cloudinary.config.secure).isTrue();
    }

    @Test
    void splitPropertiesOverrideCloudinaryUrl() {
        CloudinaryConfig config = new CloudinaryConfig();
        ReflectionTestUtils.setField(config, "cloudinaryUrl", "cloudinary://key:secret@demo-cloud");
        ReflectionTestUtils.setField(config, "cloudName", "override-cloud");
        ReflectionTestUtils.setField(config, "apiKey", "override-key");
        ReflectionTestUtils.setField(config, "apiSecret", "override-secret");

        Cloudinary cloudinary = config.cloudinary();

        assertThat(cloudinary.config.apiKey).isEqualTo("override-key");
        assertThat(cloudinary.config.apiSecret).isEqualTo("override-secret");
        assertThat(cloudinary.config.cloudName).isEqualTo("override-cloud");
    }

    @Test
    void rejectsInvalidCloudinaryUrl() {
        CloudinaryConfig config = new CloudinaryConfig();
        ReflectionTestUtils.setField(config, "cloudinaryUrl", "https://example.com");

        assertThatThrownBy(config::cloudinary)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cloudinary://");
    }
}
