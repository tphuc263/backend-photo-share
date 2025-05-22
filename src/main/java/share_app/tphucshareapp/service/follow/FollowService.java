package share_app.tphucshareapp.service.follow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.response.follow.FollowResponse;
import share_app.tphucshareapp.dto.response.follow.FollowStatsResponse;
import share_app.tphucshareapp.model.Follow;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.FollowRepository;
import share_app.tphucshareapp.repository.UserRepository;
import share_app.tphucshareapp.service.user.UserService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService implements IFollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    public void toggleFollow(String targetUserId) {
        // Validate target user exists
        userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + targetUserId));

        User currentUser = userService.getCurrentUser();

        // Prevent self-following
        if (currentUser.getId().equals(targetUserId)) {
            throw new RuntimeException("You cannot follow yourself");
        }

        Optional<Follow> existingFollow = followRepository.findByFollowerIdAndFollowingId(
                currentUser.getId(), targetUserId);

        if (existingFollow.isPresent()) {
            // Unfollow: remove existing follow relationship
            followRepository.delete(existingFollow.get());
            log.info("User {} unfollowed user {}", currentUser.getId(), targetUserId);
        } else {
            // Follow: create new follow relationship
            Follow follow = new Follow();
            follow.setFollowerId(currentUser.getId());
            follow.setFollowingId(targetUserId);

            followRepository.save(follow);
            log.info("User {} followed user {}", currentUser.getId(), targetUserId);
        }
    }

    @Override
    public List<FollowResponse> getFollowers(String userId, int page, int size) {
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Pageable pageable = PageRequest.of(page, size);
        Page<Follow> follows = followRepository.findByFollowingIdOrderByCreatedAtDesc(userId, pageable);

        List<String> followerIds = follows.getContent().stream()
                .map(Follow::getFollowerId)
                .toList();

        return convertToFollowResponses(followerIds, true);
    }

    @Override
    public List<FollowResponse> getFollowing(String userId, int page, int size) {
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Pageable pageable = PageRequest.of(page, size);
        Page<Follow> follows = followRepository.findByFollowerIdOrderByCreatedAtDesc(userId, pageable);

        List<String> followingIds = follows.getContent().stream()
                .map(Follow::getFollowingId)
                .toList();

        return convertToFollowResponses(followingIds, false);
    }

    @Override
    public FollowStatsResponse getFollowStats(String userId) {
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        long followersCount = followRepository.countByFollowingId(userId);
        long followingCount = followRepository.countByFollowerId(userId);

        FollowStatsResponse stats = new FollowStatsResponse();
        stats.setFollowersCount(followersCount);
        stats.setFollowingCount(followingCount);

        // Check if current user follows this user
        try {
            User currentUser = userService.getCurrentUser();
            boolean isFollowed = followRepository.existsByFollowerIdAndFollowingId(
                    currentUser.getId(), userId);
            stats.setFollowedByCurrentUser(isFollowed);
        } catch (Exception e) {
            // User not authenticated
            stats.setFollowedByCurrentUser(false);
        }

        return stats;
    }

    @Override
    public boolean isFollowing(String followerId, String followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Override
    public long getFollowersCount(String userId) {
        return followRepository.countByFollowingId(userId);
    }

    @Override
    public long getFollowingCount(String userId) {
        return followRepository.countByFollowerId(userId);
    }

    // Helper methods
    private List<FollowResponse> convertToFollowResponses(List<String> userIds, boolean isFollowersList) {
        if (userIds.isEmpty()) {
            return List.of();
        }

        // Fetch users in batch
        Map<String, User> usersMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // Get current user's following list for follow status
        Set<String> currentUserFollowing = getCurrentUserFollowing();

        return userIds.stream()
                .map(userId -> {
                    User user = usersMap.get(userId);
                    if (user != null) {
                        FollowResponse response = modelMapper.map(user, FollowResponse.class);
                        response.setUserId(user.getId());
                        response.setFollowedByCurrentUser(currentUserFollowing.contains(userId));
                        return response;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private Set<String> getCurrentUserFollowing() {
        try {
            User currentUser = userService.getCurrentUser();
            return followRepository.findByFollowerId(currentUser.getId())
                    .stream()
                    .map(Follow::getFollowingId)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            // User not authenticated
            return Set.of();
        }
    }
}
