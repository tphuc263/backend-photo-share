package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import share_app.tphucshareapp.dto.request.photo.PhotoCreateRequest;
import share_app.tphucshareapp.dto.request.photo.PhotoUpdateRequest;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.dto.response.photo.PhotoDetailResponse;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.security.userdetails.AppUserDetails;
import share_app.tphucshareapp.service.PhotoService;

@RestController
@RequestMapping("${api.prefix}/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PhotoResponse>> createPhoto(
            @RequestPart("photo") MultipartFile file,
            @RequestPart("data") PhotoCreateRequest request,
            Authentication authentication) {

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        String userId = userDetails.getId();

        PhotoResponse photo = photoService.createPhoto(file, request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(photo, "Tạo ảnh thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PhotoResponse>>> getAllPhotos(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        Page<PhotoResponse> photos = photoService.getAllPhotos(pageable);

        return ResponseEntity.ok(ApiResponse.success(photos, "Lấy danh sách ảnh thành công"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<PhotoResponse>>> getUserPhotos(
            @PathVariable String userId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        Page<PhotoResponse> photos = photoService.getUserPhotos(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(photos, "Lấy danh sách ảnh của người dùng thành công"));
    }

    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<Page<PhotoResponse>>> getPhotoFeed(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        String userId = userDetails.getId();

        Page<PhotoResponse> photos = photoService.getPhotoFeed(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(photos, "Lấy feed ảnh thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PhotoDetailResponse>> getPhotoDetail(@PathVariable String id) {
        PhotoDetailResponse photo = photoService.getPhotoDetail(id);

        return ResponseEntity.ok(ApiResponse.success(photo, "Lấy chi tiết ảnh thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PhotoResponse>> updatePhoto(
            @PathVariable String id,
            @RequestBody PhotoUpdateRequest request,
            Authentication authentication) {

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        String userId = userDetails.getId();

        PhotoResponse photo = photoService.updatePhoto(id, request, userId);

        return ResponseEntity.ok(ApiResponse.success(photo, "Cập nhật ảnh thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePhoto(
            @PathVariable String id,
            Authentication authentication) {

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        String userId = userDetails.getId();

        photoService.deletePhoto(id, userId);

        return ResponseEntity.ok(ApiResponse.success(null, "Xóa ảnh thành công"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PhotoResponse>>> searchPhotos(
            @RequestParam String query,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<PhotoResponse> photos = photoService.searchPhotos(query, pageable);

        return ResponseEntity.ok(ApiResponse.success(photos, "Tìm kiếm ảnh thành công"));
    }

    @GetMapping("/hashtag/{tag}")
    public ResponseEntity<ApiResponse<Page<PhotoResponse>>> getPhotosByTag(
            @PathVariable String tag,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<PhotoResponse> photos = photoService.getPhotosByTag(tag, pageable);

        return ResponseEntity.ok(ApiResponse.success(photos, "Lấy ảnh theo hashtag thành công"));
    }
}
