package share_app.tphucshareapp.dto.request.photo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreatePhotoRequest {
    private MultipartFile image;
    private String caption;
}
