package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import share_app.tphucshareapp.dto.request.user.UserBasicInfo;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.security.userdetails.AppUserDetails;
import share_app.tphucshareapp.service.LikeService;

@RestController
@RequestMapping("${api.prefix}/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /**
     * Thích một bài đăng
     */
    @PostMapping("/{photoId}")
    public ResponseEntity<ApiResponse<Void>> likePhoto(
            @PathVariable String photoId,
            Authentication authentication) {

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        String userId = userDetails.getId();

        likeService.likePhoto(photoId, userId);

        return ResponseEntity.ok(ApiResponse.success(null, "Đã thích ảnh"));
    }

    /**
     * Bỏ thích một bài đăng
     */
    @DeleteMapping("/{photoId}")
    public ResponseEntity<ApiResponse<Void>> unlikePhoto(
            @PathVariable String photoId,
            Authentication authentication) {

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        String userId = userDetails.getId();

        likeService.unlikePhoto(photoId, userId);

        return ResponseEntity.ok(ApiResponse.success(null, "Đã bỏ thích ảnh"));
    }

    /**
     * Kiểm tra người dùng đã thích ảnh chưa
     */
    @GetMapping("/check/{photoId}")
    public ResponseEntity<ApiResponse<Boolean>> checkLiked(
            @PathVariable String photoId,
            Authentication authentication) {

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        String userId = userDetails.getId();

        boolean isLiked = likeService.isLiked(photoId, userId);

        return ResponseEntity.ok(ApiResponse.success(isLiked, "Kiểm tra thích thành công"));
    }

    /**
     * Lấy danh sách người đã thích ảnh
     */
    @GetMapping("/photo/{photoId}")
    public ResponseEntity<ApiResponse<Page<UserBasicInfo>>> getLikesByPhoto(
            @PathVariable String photoId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<UserBasicInfo> likes = likeService.getLikesByPhoto(photoId, pageable);

        return ResponseEntity.ok(ApiResponse.success(likes, "Lấy danh sách thích thành công"));
    }

    /**
     * Lấy số lượng like của một ảnh
     */
    @GetMapping("/count/{photoId}")
    public ResponseEntity<ApiResponse<Long>> getLikeCount(@PathVariable String photoId) {
        long count = likeService.getLikeCount(photoId);

        return ResponseEntity.ok(ApiResponse.success(count, "Lấy số lượng thích thành công"));
    }
}
