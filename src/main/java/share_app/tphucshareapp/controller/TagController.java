package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.model.Tag;
import share_app.tphucshareapp.service.tag.TagService;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    // Get all available tags
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Tag>>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        return ResponseEntity.ok(ApiResponse.success(tags, "Tags retrieved successfully"));
    }

    // Search tags by name
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Tag>>> searchTags(
            @RequestParam String query) {
        List<Tag> tags = tagService.searchTags(query);
        return ResponseEntity.ok(ApiResponse.success(tags, "Tags search completed"));
    }

    // Get tags for a specific photo
    @GetMapping("/photo/{photoId}")
    public ResponseEntity<ApiResponse<List<Tag>>> getPhotoTags(
            @PathVariable String photoId) {
        List<Tag> tags = tagService.getPhotoTags(photoId);
        return ResponseEntity.ok(ApiResponse.success(tags, "Photo tags retrieved successfully"));
    }
}
