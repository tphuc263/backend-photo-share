package share_app.tphucshareapp.dto.response.photo;

import lombok.Data;

import java.time.Instant;

@Data
public class PhotoResponse {

    private String id;
    private String userId;
    private String username; // Username của người đăng
    private String userImageUrl; // Ảnh đại diện của người đăng
    private String imageURL;
    private String caption;
    private long likeCount;
    private long commentCount;
    private boolean isLiked; // Người dùng hiện tại đã like hay chưa
    private Instant createdAt;
}