package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import share_app.tphucshareapp.dto.request.search.SearchRequest;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.dto.response.search.SearchResultResponse;
import share_app.tphucshareapp.dto.response.search.UserSearchResponse;
import share_app.tphucshareapp.model.Tag;
import share_app.tphucshareapp.service.search.SearchService;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * Comprehensive search endpoint
     * GET /api/v1/search?q=query&type=all&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<SearchResultResponse>> search(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setType(type);
        request.setPage(page);
        request.setSize(size);

        SearchResultResponse result = searchService.searchAll(request);
        return ResponseEntity.ok(
                ApiResponse.success(result, "Search completed successfully")
        );
    }

    /**
     * Search users specifically
     * GET /api/v1/search/users?q=query&page=0&size=20
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserSearchResponse>>> searchUsers(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<UserSearchResponse> users = searchService.searchUsers(query, page, size);
        return ResponseEntity.ok(
                ApiResponse.success(users, "User search completed successfully")
        );
    }

    /**
     * Search photos by caption
     * GET /api/v1/search/photos?q=query&page=0&size=20
     */
    @GetMapping("/photos")
    public ResponseEntity<ApiResponse<Page<PhotoResponse>>> searchPhotos(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<PhotoResponse> photos = searchService.searchPhotos(query, page, size);
        return ResponseEntity.ok(
                ApiResponse.success(photos, "Photo search completed successfully")
        );
    }

    /**
     * Search photos by tags
     * GET /api/v1/search/photos/tags?q=query&page=0&size=20
     */
    @GetMapping("/photos/tags")
    public ResponseEntity<ApiResponse<Page<PhotoResponse>>> searchPhotosByTags(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<PhotoResponse> photos = searchService.searchPhotosByTags(query, page, size);
        return ResponseEntity.ok(
                ApiResponse.success(photos, "Photo tag search completed successfully")
        );
    }

    /**
     * Search tags
     * GET /api/v1/search/tags?q=query&limit=10
     */
    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<Tag>>> searchTags(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "10") int limit) {

        List<Tag> tags = searchService.searchTags(query, limit);
        return ResponseEntity.ok(
                ApiResponse.success(tags, "Tag search completed successfully")
        );
    }

    /**
     * Get search suggestions for autocomplete
     * GET /api/v1/search/suggestions?q=query&limit=10
     */
    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<String>>> getSearchSuggestions(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "10") int limit) {

        List<String> suggestions = searchService.getSearchSuggestions(query, limit);
        return ResponseEntity.ok(
                ApiResponse.success(suggestions, "Search suggestions retrieved successfully")
        );
    }
}