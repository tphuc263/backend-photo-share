package share_app.tphucshareapp.dto.response.user;

import lombok.Data;

@Data
public class UserDetailResponse {

    private String id;
    private String username;
    private String email;
    private String imageUrl;
    private String firstName;
    private String lastName;
    private String bio;
    private Instant createdAt;
    private boolean isFollowing; // Người dùng hiện tại có đang follow người này không
}
