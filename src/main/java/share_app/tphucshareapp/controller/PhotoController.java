package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import share_app.tphucshareapp.dto.request.photo.CreatePhotoRequest;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.service.photo.PhotoService;

@RestController
@RequestMapping("${api.prefix}/photos")
@RequiredArgsConstructor
public class PhotoController {
    private final PhotoService photoService;

    // Create a photo
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PhotoResponse>> createPhoto(
            @ModelAttribute CreatePhotoRequest request) {
        PhotoResponse photo = photoService.createPhoto(request);
        return ResponseEntity.ok(ApiResponse.success(photo, "Photo created successfully"));
    }
}
