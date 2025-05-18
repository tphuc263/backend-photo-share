package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import share_app.tphucshareapp.dto.request.user.UserBasicInfo;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.security.userdetails.AppUserDetails;
import share_app.tphucshareapp.service.FollowService;

@RestController
@RequestMapping("${api.prefix}/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * Theo dõi một người dùng
     */
    @PostMapping("/{followingId}")
    public ResponseEntity<ApiResponse<Void>> followUser(
            @PathVariable String followingId,
            Authentication authentication) {

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        String followerId = userDetails.getId();

        followService.followUser(followerId, followingId);

        return ResponseEntity.ok(ApiResponse.success(null, "Theo dõi người dùng thành công"));
    }

    /**
     * Hủy theo dõi một người dùng
     */
    @DeleteMapping("/{followingId}")
    public ResponseEntity<ApiResponse<Void>> unfollowUser(
            @PathVariable String followingId,
            Authentication authentication) {

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        String followerId = userDetails.getId();

        followService.unfollowUser(followerId, followingId);

        return ResponseEntity.ok(ApiResponse.success(null, "Hủy theo dõi người dùng thành công"));
    }

    /**
     * Lấy danh sách người đang theo dõi một người dùng
     */
    @GetMapping("/followers/{userId}")
    public ResponseEntity<ApiResponse<List<UserBasicInfo>>> getUserFollowers(
            @PathVariable String userId) {

        List<UserBasicInfo> followers = followService.getUserFollowers(userId);

        return ResponseEntity.ok(ApiResponse.success(followers, "Lấy danh sách người theo dõi thành công"));
    }

    /**
     * Lấy danh sách người mà một người dùng đang theo dõi
     */
    @GetMapping("/following/{userId}")
    public ResponseEntity<ApiResponse<List<UserBasicInfo>>> getUserFollowing(
            @PathVariable String userId) {

        List<UserBasicInfo> following = followService.getUserFollowing(userId);

        return ResponseEntity.ok(ApiResponse.success(following, "Lấy danh sách đang theo dõi thành công"));
    }

    /**
     * Kiểm tra người dùng hiện tại có đang theo dõi một người dùng khác không
     */
    @GetMapping("/check/{followingId}")
    public ResponseEntity<ApiResponse<Boolean>> checkFollowing(
            @PathVariable String followingId,
            Authentication authentication) {

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        String followerId = userDetails.getId();

        boolean isFollowing = followService.isFollowing(followerId, followingId);

        return ResponseEntity.ok(ApiResponse.success(isFollowing, "Kiểm tra theo dõi thành công"));
    }
}
