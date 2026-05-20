package com.capstone.iamservice.service;

import com.capstone.iamservice.exception.AppException;
import com.capstone.iamservice.exception.ErrorCode;
import com.capstone.iamservice.entity.OrganizationProfile;
import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final Cloudinary cloudinary;

    @Async
    public CompletableFuture<Void> uploadImageAsync(OrganizationProfile organization, byte[] imageBytes, String type) {
        String folder = "organization/" + organization.getId() + "/" + type + "/";
        String publicId = UUID.randomUUID().toString();

        Map<String, Object> options = new HashMap<>();
        options.put("resource_type", "image");
        options.put("folder", folder);
        options.put("public_id", publicId);
        options.put("overwrite", true);

        try {
            var uploadResult = cloudinary.uploader().upload(imageBytes, options);
            String url = uploadResult.get("url").toString();
            switch (type) {
                case "logo":
                    organization.setLogoUrl(url);
                    break;
                case "license":
                    organization.setBusinessLicenseUrl(url);
                    break;
                case "cover":
                    organization.setCoverUrl(url);
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            throw new AppException(ErrorCode.IO_EXCEPTION, "Không thể tải file lên Cloudinary: " + e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }
}
