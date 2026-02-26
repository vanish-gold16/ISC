package org.example.isc.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final Cloudinary cloudinary;

    public String uploadAvatar(MultipartFile file, Long userId) throws IOException {
        if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
            throw new IllegalArgumentException("Only images allowed");
        }

        if (file.getSize() > 5_000_000) {
            throw new IllegalArgumentException("File too large");
        }

        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                Map.of(
                        "folder", "avatars",
                        "public_id", "avatar_" + userId,
                        "overwrite", true,
                        "transformation",
                        new Transformation<>()
                                .width(512)
                                .height(512)
                                .crop("fill")
                                .gravity("face")
                )
        );

        return uploadResult.get("secure_url").toString();
    }

    public String uploadCover(MultipartFile file, Long userId) throws IOException {
        if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
            throw new IllegalArgumentException("Only images allowed");
        }

        if (file.getSize() > 5_000_000) {
            throw new IllegalArgumentException("File too large");
        }

        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                Map.of(
                        "folder", "covers",
                        "public_id", "cover_" + userId,
                        "overwrite", true,
                        "transformation",
                        new Transformation<>()
                                .width(1600)
                                .height(400)
                                .crop("fill")
                                .gravity("auto")
                )
        );

        return uploadResult.get("secure_url").toString();
    }

}