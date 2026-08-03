package com.mvc.mock_project.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    /**
     * Upload an avatar image to Cloudinary.
     *
     * @param file      the image file to upload
     * @param accountId the account ID (used for folder organization)
     * @return the secure URL of the uploaded image
     */
    String uploadAvatar(MultipartFile file, Integer accountId);

    /**
     * Delete an image from Cloudinary by its URL.
     *
     * @param imageUrl the URL of the image to delete
     */
    void deleteImage(String imageUrl);

    /**
     * Upload a facility image to Cloudinary.
     *
     * @param file       the image file to upload
     * @param facilityId the facility ID (used for folder organization)
     * @return the secure URL of the uploaded image
     */
    String uploadFacilityImage(MultipartFile file, Integer facilityId);

    /**
     * Upload a product image to Cloudinary.
     *
     * @param file       the image file to upload
     * @param facilityId the facility ID (used for folder organization)
     * @return the secure URL of the uploaded image
     */
    String uploadProductImage(MultipartFile file, Integer facilityId);
}
