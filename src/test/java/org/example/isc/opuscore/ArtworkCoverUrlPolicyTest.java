package org.example.isc.opuscore;

import org.example.isc.opuscore.service.ArtworkCoverUrlPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArtworkCoverUrlPolicyTest {

    @Test
    void rejectsLegacySharedReviewCoverUrl() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ArtworkCoverUrlPolicy.normalizeArtworkCoverUrl(
                        "https://res.cloudinary.com/dbd7wyltm/image/upload/v1775074506/reviews/review_7.jpg"
                )
        );
    }

    @Test
    void allowsUniqueArtworkCoverUrl() {
        String url = "https://res.cloudinary.com/dbd7wyltm/image/upload/v1775300000/artwork-covers/artwork_cover_7_1775300000000_abcd1234.jpg";
        assertEquals(url, ArtworkCoverUrlPolicy.normalizeArtworkCoverUrl(url));
    }

    @Test
    void allowsUniqueReviewImageUrl() {
        String url = "https://res.cloudinary.com/dbd7wyltm/image/upload/v1775300000/reviews/review_7_1775300000000_abcd1234.jpg";
        assertEquals(url, ArtworkCoverUrlPolicy.normalizeArtworkCoverUrl(url));
    }

    @Test
    void returnsNullForBlankUrl() {
        assertNull(ArtworkCoverUrlPolicy.normalizeArtworkCoverUrl("   "));
    }
}
