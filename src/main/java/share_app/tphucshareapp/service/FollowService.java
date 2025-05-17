package share_app.tphucshareapp.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import share_app.tphucshareapp.dto.request.user.UserBasicInfo;
import share_app.tphucshareapp.model.Follow;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.FollowRepository;
import share_app.tphucshareapp.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    /**
     * Theo dõi một người dùng
     */
    @Transactional
    @CacheEvict(value = {"userFollowers", "userFollowing"}, key = "#followerId + '-' + #followingId")
    public void followUser(String followerId, String followingId) {
        // Kiểm tra người dùng có tồn tại
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng theo dõi"));

        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng được theo dõi"));

        // Kiểm tra đã follow chưa
        Optional<Follow> existingFollow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId);

        if (existingFollow.isPresent()) {
            throw new RuntimeException("Đã theo dõi người dùng này rồi");
        }

        // Không thể tự follow chính mình
        if (followerId.equals(followingId)) {
            throw new RuntimeException("Không thể tự theo dõi chính mình");
        }

        // Tạo follow mới
        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);

        // Lưu vào DB
        followRepository.save(follow);
    }

    /**
     * Hủy theo dõi một người dùng
     */
    @Transactional
    @CacheEvict(value = {"userFollowers", "userFollowing"}, key = "#followerId + '-' + #followingId")
    public void unfollowUser(String followerId, String followingId) {
        // Kiểm tra follow có tồn tại
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new RuntimeException("Chưa theo dõi người dùng này"));

        // Xóa follow
        followRepository.delete(follow);
    }

    /**
     * Lấy danh sách người đang theo dõi user
     */
    @Cacheable(value = "userFollowers", key = "#userId")
    public List<UserBasicInfo> getUserFollowers(String userId) {
        // Lấy danh sách follower ID
        List<String> followerIds = followRepository.findFollowerIdsByFollowingId(userId);

        List<UserBasicInfo> followers = new ArrayList<>();

        // Lấy thông tin chi tiết của từng follower
        for (String followerId : followerIds) {
            userRepository.findById(followerId).ifPresent(user -> {
                UserBasicInfo userInfo = modelMapper.map(user, UserBasicInfo.class);
                followers.add(userInfo);
            });
        }

        return followers;
    }

    /**
     * Lấy danh sách người mà user đang theo dõi
     */
    @Cacheable(value = "userFollowing", key = "#userId")
    public List<UserBasicInfo> getUserFollowing(String userId) {
        // Lấy danh sách following ID
        List<String> followingIds = followRepository.findFollowingIdsByFollowerId(userId);

        List<UserBasicInfo> following = new ArrayList<>();

        // Lấy thông tin chi tiết của từng following
        for (String followingId : followingIds) {
            userRepository.findById(followingId).ifPresent(user -> {
                UserBasicInfo userInfo = modelMapper.map(user, UserBasicInfo.class);
                following.add(userInfo);
            });
        }

        return following;
    }

    /**
     * Kiểm tra user có đang follow một user khác không
     */
    public boolean isFollowing(String followerId, String followingId) {
        return followRepository.findByFollowerIdAndFollowingId(followerId, followingId).isPresent();
    }
}
