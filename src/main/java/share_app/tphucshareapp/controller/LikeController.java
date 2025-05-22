package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.dto.response.like.LikeResponse;
import share_app.tphucshareapp.service.like.LikeService;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/likes")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    // Toggle like/unlike a photo
    @PostMapping("/photo/{photoId}")
    public ResponseEntity<ApiResponse<Void>> toggleLike(@PathVariable String photoId) {
        likeService.toggleLike(photoId);
        return ResponseEntity.ok(ApiResponse.success(null, "Like toggled successfully"));
    }

    // Get all likes for a photo
    @GetMapping("/photo/{photoId}")
    public ResponseEntity<ApiResponse<List<LikeResponse>>> getPhotoLikes(@PathVariable String photoId) {
        List<LikeResponse> likes = likeService.getPhotoLikes(photoId);
        return ResponseEntity.ok(ApiResponse.success(likes, "Photo likes retrieved successfully"));
    }

    // Get likes count for a photo
    @GetMapping("/photo/{photoId}/count")
    public ResponseEntity<ApiResponse<Long>> getPhotoLikesCount(@PathVariable String photoId) {
        long count = likeService.getPhotoLikesCount(photoId);
        return ResponseEntity.ok(ApiResponse.success(count, "Photo likes count retrieved successfully"));
    }
}
