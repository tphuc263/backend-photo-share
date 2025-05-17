package share_app.tphucshareapp.dto.request.photo;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PhotoCreateRequest {

    @Size(max = 2000, message = "Caption không được vượt quá 2000 ký tự")
    private String caption;
}
