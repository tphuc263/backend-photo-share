package share_app.tphucshareapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import share_app.tphucshareapp.dto.request.comment.CommentCreateRequest;
import share_app.tphucshareapp.dto.request.comment.CommentUpdateRequest;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.dto.response.comment.CommentResponse;
import share_app.tphucshareapp.security.userdetails.AppUserDetails;
import share_app.tphucshareapp.service.CommentService;

@RestController
@RequestMapping("${api.prefix}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * Tạo bình luận mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @Valid @RequestBody CommentCreateRequest request,
            Authentication authentication) {

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        String userId = userDetails.getId();

        CommentResponse comment = commentService.createComment(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(comment, "Tạo bình luận thành công"));
    }

    /**
     * Lấy danh sách bình luận của ảnh
     */
    @GetMapping("/photo/{photoId}")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getPhotoComments(
            @PathVariable String photoId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        Page<CommentResponse> comments = commentService.getPhotoComments(photoId, pageable);

        return ResponseEntity.ok(ApiResponse.success(comments, "Lấy danh sách bình luận thành công"));
    }

    /**
     * Cập nhật bình luận
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable String id,
            @Valid @RequestBody CommentUpdateRequest request,
            Authentication authentication) {

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        String userId = userDetails.getId();

        CommentResponse comment = commentService.updateComment(id, request, userId);

        return ResponseEntity.ok(ApiResponse.success(comment, "Cập nhật bình luận thành công"));
    }

    /**
     * Xóa bình luận
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable String id,
            Authentication authentication) {

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        String userId = userDetails.getId();

        commentService.deleteComment(id, userId);

        return ResponseEntity.ok(ApiResponse.success(null, "Xóa bình luận thành công"));
    }
}
