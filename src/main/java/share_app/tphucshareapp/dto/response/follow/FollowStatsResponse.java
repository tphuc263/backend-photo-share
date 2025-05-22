package share_app.tphucshareapp.dto.response.follow;

import lombok.Data;

@Data
public class FollowStatsResponse {
    private long followersCount;
    private long followingCount;
    private boolean isFollowedByCurrentUser;
}