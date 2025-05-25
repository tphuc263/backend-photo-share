package share_app.tphucshareapp.service.search;

import org.springframework.data.domain.Page;
import share_app.tphucshareapp.dto.request.search.SearchRequest;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.dto.response.search.SearchResultResponse;
import share_app.tphucshareapp.dto.response.search.UserSearchResponse;
import share_app.tphucshareapp.model.Tag;

import java.util.List;

public interface ISearchService {

    /**
     * Comprehensive search across all content types
     */
    SearchResultResponse searchAll(SearchRequest request);

    /**
     * Search for users by username, first name, last name, or bio
     */
    Page<UserSearchResponse> searchUsers(String query, int page, int size);

    /**
     * Search for photos by caption content
     */
    Page<PhotoResponse> searchPhotos(String query, int page, int size);

    /**
     * Search for photos by tag names
     */
    Page<PhotoResponse> searchPhotosByTags(String query, int page, int size);

    /**
     * Search for tags by name
     */
    List<Tag> searchTags(String query, int limit);

    /**
     * Get search suggestions for autocomplete
     */
    List<String> getSearchSuggestions(String query, int limit);
}
