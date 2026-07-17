package com.mvc.mock_project.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mvc.mock_project.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    @Override
    public String uploadAvatar(MultipartFile file, Integer accountId) {
        validateFile(file);

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "sporthub/avatars",
                    "public_id", "avatar_" + accountId,
                    "overwrite", true,
                    "resource_type", "image",
                    "transformation", "c_fill,w_400,h_400,g_face,q_auto,f_auto"
            ));

            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            log.error("Failed to upload avatar for account {}: {}", accountId, e.getMessage());
            throw new RuntimeException("msg.error.avatar_upload_failed");
        }
    }

    @Override
    public String uploadFacilityImage(MultipartFile file, Integer facilityId) {
        validateFile(file);

        try {
            String publicId = java.util.UUID.randomUUID().toString();
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "sporthub/facility/facility_" + facilityId,
                    "public_id", publicId,
                    "resource_type", "image",
                    "transformation", "q_auto,f_auto"
            ));

            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            log.error("Failed to upload image for facility {}: {}", facilityId, e.getMessage());
            throw new RuntimeException("Lỗi khi tải ảnh lên server");
        }
    }

    @Override
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            // Extract public_id from URL
            String publicId = extractPublicId(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Deleted image from Cloudinary: {}", publicId);
            }
        } catch (IOException e) {
            log.warn("Failed to delete image from Cloudinary: {}", e.getMessage());
            // Non-critical error, don't throw
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("msg.error.avatar_empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("msg.error.avatar_too_large");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("msg.error.avatar_invalid_type");
        }
    }

    /**
     * Extract public_id from a Cloudinary secure URL.
     * Example URL: https://res.cloudinary.com/xxx/image/upload/v123/sporthub/avatars/avatar_1.jpg
     * Returns: sporthub/avatars/avatar_1
     */
    private String extractPublicId(String url) {
        try {
            String[] parts = url.split("/upload/");
            if (parts.length < 2) return null;

            String path = parts[1];
            // Remove version prefix (v1234567890/)
            if (path.matches("^v\\d+/.*")) {
                path = path.substring(path.indexOf('/') + 1);
            }
            // Remove file extension
            int dotIndex = path.lastIndexOf('.');
            if (dotIndex > 0) {
                path = path.substring(0, dotIndex);
            }
            return path;
        } catch (Exception e) {
            log.warn("Could not extract public_id from URL: {}", url);
            return null;
        }
    }
}
