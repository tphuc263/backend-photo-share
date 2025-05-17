package share_app.tphucshareapp.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentCreateRequest {

    @NotBlank(message = "PhotoId không được để trống")
    private String photoId;

    @NotBlank(message = "Nội dung bình luận không được để trống")
    @Size(max = 500, message = "Bình luận không được vượt quá 500 ký tự")
    private String text;
}
