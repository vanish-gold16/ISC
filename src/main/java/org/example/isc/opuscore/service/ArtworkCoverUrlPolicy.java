package org.example.isc.opuscore.service;

import java.util.regex.Pattern;

public final class ArtworkCoverUrlPolicy {

    private static final Pattern LEGACY_SHARED_REVIEW_COVER_URL = Pattern.compile(
            "^https?://res\\.cloudinary\\.com/[^/]+/image/upload(?:/v\\d+)?/reviews/review_\\d+\\.(?:jpg|jpeg|png|webp|gif|avif)(?:\\?.*)?$",
            Pattern.CASE_INSENSITIVE
    );

    private ArtworkCoverUrlPolicy() {
    }

    public static String normalizeArtworkCoverUrl(String url) {
        String normalized = blankToNull(url);
        if (normalized == null) {
            return null;
        }

        if (isLegacySharedReviewCoverUrl(normalized)) {
            throw new IllegalArgumentException("This cover uses an old shared image. Upload the cover again so the artwork gets its own file.");
        }

        return normalized;
    }

    static boolean isLegacySharedReviewCoverUrl(String url) {
        return url != null && LEGACY_SHARED_REVIEW_COVER_URL.matcher(url.trim()).matches();
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
