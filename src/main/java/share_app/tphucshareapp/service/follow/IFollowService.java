package share_app.tphucshareapp.service.follow;

import share_app.tphucshareapp.dto.response.follow.FollowResponse;
import share_app.tphucshareapp.dto.response.follow.FollowStatsResponse;

import java.util.List;

public interface IFollowService {
    void toggleFollow(String targetUserId);

    List<FollowResponse> getFollowers(String userId, int page, int size);

    List<FollowResponse> getFollowing(String userId, int page, int size);

    FollowStatsResponse getFollowStats(String userId);

    boolean isFollowing(String followerId, String followingId);

    long getFollowersCount(String userId);

    long getFollowingCount(String userId);
}
