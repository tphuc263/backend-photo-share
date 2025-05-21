package share_app.tphucshareapp.service.photo;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public Map<String, Object> uploadImage(MultipartFile file){
        Map<String, Object> params = new HashMap<>();
        params.put("folder", "share_app");
        params.put("resource_type", "auto");
        params.put("unique_filename", true);

        try {
            return cloudinary.uploader().upload(file.getBytes(), params);
        } catch (IOException e) {
            log.error("Error uploading image to Cloudinary", e);
            throw new RuntimeException("Error uploading image to Cloudinary");
        }
    }

    public Map<String, Object> deleteImage(String publicId){
        try {
            return cloudinary.uploader().destroy(publicId, Map.of());
        } catch (IOException e) {
            log.error("Error deleting image from Cloudinary: {}", publicId, e);
            throw new RuntimeException("Error deleting image from Cloudinary");
        }
    }

    public String extractPublicIdFromUrl(String imageUrl) {
        try {
            if (imageUrl == null || !imageUrl.contains("/upload/")) {
                return null;
            }
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }
            String afterUpload = parts[1];

            // Remove query parameters if present
            if (afterUpload.contains("?")) {
                afterUpload = afterUpload.substring(0, afterUpload.indexOf("?"));
            }

            // Remove file extension (.jpg, .png, etc.)
            int lastDotIndex = afterUpload.lastIndexOf('.');
            if (lastDotIndex > 0) {
                afterUpload = afterUpload.substring(0, lastDotIndex);
            }

            // Cloudinary URLs typically have format: /v1234567890/folder/filename
            // Need to remove version number if present
            if (afterUpload.startsWith("/")) {
                afterUpload = afterUpload.substring(1);
            }

            String[] segments = afterUpload.split("/");
            if (segments.length > 0 && segments[0].startsWith("v") && segments[0].substring(1).matches("\\d+")) {
                // Remove version part
                afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
            }

            return afterUpload;
        } catch (Exception e) {
            log.error("Error extracting publicId from URL: {}", imageUrl, e);
            return null;
        }
    }
}
