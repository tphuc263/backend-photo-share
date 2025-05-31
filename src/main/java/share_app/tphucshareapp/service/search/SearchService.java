package share_app.tphucshareapp.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.request.search.SearchRequest;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.dto.response.search.SearchResultResponse;
import share_app.tphucshareapp.dto.response.search.UserSearchResponse;
import share_app.tphucshareapp.dto.response.user.UserProfileResponse;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.PhotoTag;
import share_app.tphucshareapp.model.Tag;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.*;
import share_app.tphucshareapp.service.follow.FollowService;
import share_app.tphucshareapp.service.photo.PhotoConversionService;
import share_app.tphucshareapp.service.user.UserService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService implements ISearchService {

    private final UserRepository userRepository;
    private final PhotoRepository photoRepository;
    private final TagRepository tagRepository;
    private final PhotoTagRepository photoTagRepository;
    private final FollowRepository followRepository;
    private final MongoTemplate mongoTemplate;
    private final ModelMapper modelMapper;
    private final PhotoConversionService photoConversionService;
    private final UserService userService;
    private final FollowService followService;

    @Override
    public SearchResultResponse searchAll(SearchRequest request) {
        log.info("Performing comprehensive search for: {}", request.getQuery());

        String query = sanitizeSearchQuery(request.getQuery());
        if (query.isEmpty()) {
            return new SearchResultResponse();
        }

        SearchResultResponse result = new SearchResultResponse();
        result.setQuery(request.getQuery());

        // Search users (limit to small number for "all" search)
        Page<UserSearchResponse> users = searchUsers(query, 0, 5);
        result.setUsers(users.getContent().stream()
                .map(this::convertToUserProfileResponse)
                .toList());
        result.setTotalUsers(users.getTotalElements());

        // Search photos
        Page<PhotoResponse> photos = searchPhotos(query, 0, 10);
        result.setPhotos(photos.getContent());
        result.setTotalPhotos(photos.getTotalElements());

        // Search tags
        List<Tag> tags = searchTags(query, 5);
        result.setTags(tags);
        result.setTotalTags(tags.size());

        return result;
    }

    @Override
    public Page<UserSearchResponse> searchUsers(String query, int page, int size) {
        log.info("Searching users for: {}", query);

        String sanitizedQuery = sanitizeSearchQuery(query);
        if (sanitizedQuery.isEmpty()) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size);

        // Priority 1: Search by username first (most relevant)
        Page<User> users = userRepository.findByUsernameContaining(sanitizedQuery, pageable);

        // If no results and query is longer, try extended search
        if (users.isEmpty() && sanitizedQuery.length() > 2) {
            users = userRepository.findByNameFields(sanitizedQuery, pageable);
        }

        return users.map(this::convertToUserSearchResponse);
    }

    @Override
    public Page<PhotoResponse> searchPhotos(String query, int page, int size) {
        log.info("Searching photos for: {}", query);

        String sanitizedQuery = sanitizeSearchQuery(query);
        if (sanitizedQuery.isEmpty()) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Try text search first
        try {
            Page<Photo> photos = photoRepository.findByTextSearch(sanitizedQuery, pageable);
            if (!photos.isEmpty()) {
                return photos.map(photoConversionService::convertToPhotoResponse);
            }
        } catch (Exception e) {
            log.warn("Photo text search failed, falling back to regex search: {}", e.getMessage());
        }

        // Fallback to caption search
        Page<Photo> photos = photoRepository.findByCaptionContainingIgnoreCase(sanitizedQuery, pageable);
        return photos.map(photoConversionService::convertToPhotoResponse);
    }

    @Override
    public Page<PhotoResponse> searchPhotosByTags(String query, int page, int size) {
        log.info("Searching photos by tags for: {}", query);

        String sanitizedQuery = sanitizeSearchQuery(query);
        if (sanitizedQuery.isEmpty()) {
            return Page.empty();
        }

        // Find matching tags
        List<Tag> matchingTags = tagRepository.findByNameContainingIgnoreCase(sanitizedQuery);
        if (matchingTags.isEmpty()) {
            return Page.empty();
        }

        // Get tag IDs
        List<String> tagIds = matchingTags.stream()
                .map(Tag::getId)
                .toList();

        // Find photos with these tags
        List<PhotoTag> photoTags = photoTagRepository.findByTagIdIn(tagIds);
        List<String> photoIds = photoTags.stream()
                .map(PhotoTag::getPhotoId)
                .distinct()
                .toList();

        if (photoIds.isEmpty()) {
            return Page.empty();
        }

        // Get photos and convert to response
        Pageable pageable = PageRequest.of(page, size);
        Page<Photo> photos = photoRepository.findByPhotoIds(photoIds, pageable);
        return photos.map(photoConversionService::convertToPhotoResponse);
    }

    @Override
    public List<Tag> searchTags(String query, int limit) {
        log.info("Searching tags for: {}", query);

        String sanitizedQuery = sanitizeSearchQuery(query);
        if (sanitizedQuery.isEmpty()) {
            return List.of();
        }

        List<Tag> tags = tagRepository.findByNameContainingIgnoreCase(sanitizedQuery);
        return tags.stream()
                .limit(limit)
                .toList();
    }

    @Override
    public List<String> getSearchSuggestions(String query, int limit) {
        log.info("Getting search suggestions for: {}", query);

        String sanitizedQuery = sanitizeSearchQuery(query);
        if (sanitizedQuery.isEmpty()) {
            return List.of();
        }

        Set<String> suggestions = new java.util.HashSet<>();

        // Get user suggestions
        try {
            List<User> users = userRepository.findByNameFields(sanitizedQuery,
                    PageRequest.of(0, limit / 3)).getContent();
            users.forEach(user -> {
                suggestions.add(user.getUsername());
                if (user.getFirstName() != null) suggestions.add(user.getFirstName());
                if (user.getLastName() != null) suggestions.add(user.getLastName());
            });
        } catch (Exception e) {
            log.warn("Error getting user suggestions: {}", e.getMessage());
        }

        // Get tag suggestions
        try {
            List<Tag> tags = tagRepository.findByNameContainingIgnoreCase(sanitizedQuery);
            tags.stream()
                    .limit(limit / 3)
                    .forEach(tag -> suggestions.add(tag.getName()));
        } catch (Exception e) {
            log.warn("Error getting tag suggestions: {}", e.getMessage());
        }

        return suggestions.stream()
                .limit(limit)
                .sorted()
                .toList();
    }

    // Helper methods
    private String sanitizeSearchQuery(String query) {
        if (query == null) return "";

        // Remove special characters that might interfere with search
        return query.trim()
                .replaceAll("[\"'`]", "") // Remove quotes
                .replaceAll("\\s+", " "); // Normalize whitespace
    }

    private UserSearchResponse convertToUserSearchResponse(User user) {
        UserSearchResponse response = modelMapper.map(user, UserSearchResponse.class);

        // Add follower count
        response.setFollowersCount(followService.getFollowersCount(user.getId()));

        // Check if current user follows this user
        try {
            User currentUser = userService.getCurrentUser();
            response.setFollowedByCurrentUser(
                    followService.isFollowing(currentUser.getId(), user.getId())
            );
        } catch (Exception e) {
            response.setFollowedByCurrentUser(false);
        }

        return response;
    }

    private UserProfileResponse convertToUserProfileResponse(UserSearchResponse userSearchResponse) {
        return modelMapper.map(userSearchResponse, UserProfileResponse.class);
    }
}
