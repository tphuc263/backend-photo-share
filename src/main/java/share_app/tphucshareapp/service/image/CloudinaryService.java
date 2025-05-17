package share_app.tphucshareapp.service.image;

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
    private static final String DEFAULT_FOLDER = "share_app";

    public Map<String, Object> uploadImage(MultipartFile file) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("folder", DEFAULT_FOLDER);
        params.put("resource_type", "auto");
        params.put("unique_filename", true);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), params);
            return result;
        } catch (IOException e) {
            log.error("Lỗi khi tải hình ảnh lên Cloudinary", e);
            throw e;
        }
    }

    public Map<String, Object> deleteImage(String publicId) throws IOException {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, Map.of());
            return result;
        } catch (IOException e) {
            log.error("Lỗi khi xóa hình ảnh từ Cloudinary: {}", publicId, e);
            throw e;
        }
    }

    /**
     * Trích xuất publicId từ URL ảnh Cloudinary.
     * @param imageUrl URL đầy đủ của ảnh trên Cloudinary
     * @return publicId (để dùng cho xoá ảnh, tạo lại URL, v.v.), hoặc null nếu không hợp lệ
     */
    public String extractPublicIdFromUrl(String imageUrl) {
        try {
            if (imageUrl == null || !imageUrl.contains("/upload/")) {
                return null;
            }

            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) return null;

            String afterUpload = parts[1];

            // Lọc bỏ các tham số query nếu có
            if (afterUpload.contains("?")) {
                afterUpload = afterUpload.substring(0, afterUpload.indexOf("?"));
            }

            // Lọc bỏ phần định dạng file (.jpg, .png, etc.)
            int lastDotIndex = afterUpload.lastIndexOf('.');
            if (lastDotIndex > 0) {
                afterUpload = afterUpload.substring(0, lastDotIndex);
            }

            // URL Cloudinary thường có format như: /v1234567890/folder/filename
            // Nếu có phiên bản (v1234567890), cần loại bỏ
            if (afterUpload.startsWith("/")) {
                afterUpload = afterUpload.substring(1);
            }

            String[] segments = afterUpload.split("/");
            if (segments.length > 0 && segments[0].startsWith("v") && segments[0].substring(1).matches("\\d+")) {
                // Loại bỏ phần phiên bản
                afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
            }

            return afterUpload ;
        } catch (Exception e) {
            log.error("Lỗi khi trích xuất publicId từ URL: {}", imageUrl, e);
            return null;
        }
    }
}
