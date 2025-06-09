package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
}
