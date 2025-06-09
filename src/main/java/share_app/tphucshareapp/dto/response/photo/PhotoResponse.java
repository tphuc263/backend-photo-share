package share_app.tphucshareapp.dto.response.photo;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class PhotoResponse {
    private String id;
    private String userId;
    private String username;
    private String userImageUrl;
    private String imageUrl;
    private String caption;
    private Instant createdAt;
    private int likeCount;
    private int commentCount;
    private boolean isLikedByCurrentUser;
    private List<String> tags;
}
