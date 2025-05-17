package share_app.tphucshareapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import share_app.tphucshareapp.dto.request.user.UserProfileRequest;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.dto.response.user.UserDetailResponse;
import share_app.tphucshareapp.dto.response.user.UserStatsResponse;
import share_app.tphucshareapp.security.userdetails.AppUserDetails;
import share_app.tphucshareapp.service.UserService;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Lấy thông tin chi tiết người dùng
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetail(@PathVariable String userId) {
        UserDetailResponse user = userService.getUserDetail(userId);

        return ResponseEntity.ok(ApiResponse.success(user, "Lấy thông tin người dùng thành công"));
    }

    /**
     * Lấy thông tin người dùng hiện tại
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getCurrentUserDetail(Authentication authentication) {
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        UserDetailResponse user = userService.getUserDetail(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success(user, "Lấy thông tin người dùng thành công"));
    }

    /**
     * Cập nhật thông tin cá nhân
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDetailResponse>> updateProfile(
            @Valid @RequestBody UserProfileRequest request,
            Authentication authentication) {

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        String userId = userDetails.getId();

        UserDetailResponse user = userService.updateProfile(userId, request);

        return ResponseEntity.ok(ApiResponse.success(user, "Cập nhật thông tin thành công"));
    }

    /**
     * Cập nhật ảnh đại diện
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> updateAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        String userId = userDetails.getId();

        String avatarUrl = userService.updateAvatar(userId, file);

        return ResponseEntity.ok(ApiResponse.success(avatarUrl, "Cập nhật ảnh đại diện thành công"));
    }

    /**
     * Lấy thống kê người dùng (số bài đăng, số người theo dõi, số đang theo dõi)
     */
    @GetMapping("/stats/{userId}")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats(@PathVariable String userId) {
        UserStatsResponse stats = userService.getUserStats(userId);

        return ResponseEntity.ok(ApiResponse.success(stats, "Lấy thống kê người dùng thành công"));
    }

    /**
     * Tìm kiếm người dùng theo tên
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<java.util.List<UserDetailResponse>>> searchUsers(
            @RequestParam String query) {

        java.util.List<UserDetailResponse> users = userService.searchUsers(query);

        return ResponseEntity.ok(ApiResponse.success(users, "Tìm kiếm người dùng thành công"));
    }
}
