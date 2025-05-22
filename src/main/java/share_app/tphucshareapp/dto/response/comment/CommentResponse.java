package share_app.tphucshareapp.dto.response.comment;

import lombok.Data;

import java.time.Instant;

@Data
public class CommentResponse {
    private String id;
    private String userId;
    private String username;
    private String userImageUrl;
    private String text;
    private Instant createdAt;
}
