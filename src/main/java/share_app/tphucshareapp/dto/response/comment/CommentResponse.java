package share_app.tphucshareapp.dto.response.comment;

import lombok.Data;
import share_app.tphucshareapp.dto.request.user.UserBasicInfo;

import java.time.Instant;

@Data
public class CommentResponse {

    private String id;
    private UserBasicInfo user;
    private String text;
    private Instant createdAt;
}
