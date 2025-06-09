package share_app.tphucshareapp.service.photo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.model.Follow;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.FollowRepository;
import share_app.tphucshareapp.repository.PhotoRepository;
import share_app.tphucshareapp.service.user.UserService;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsfeedService implements INewsfeedService {

    private final FollowRepository followRepository;
    private final PhotoRepository photoRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PhotoConversionService photoConversionService;
    private final UserService userService;

    // Cache configuration
    private static final String NEWSFEED_CACHE_KEY = "newsfeed:user:";
    private static final Duration CACHE_TTL = Duration.ofHours(2);
    private static final int MAX_CACHED_ITEMS = 200;
    private static final int RELEVANCE_WINDOW_DAYS = 7;

    @Override
    public Page<PhotoResponse> getNewsfeed(String userId, int page, int size) {
        log.info("Generating real-time newsfeed for user: {}", userId);
        User currentUser = userService.findUserById(userId);

        // Step 1: Get users that current user follows
        List<String> followingIds = getFollowingUserIds(userId);
        if (followingIds.isEmpty()) {
            log.info("User {} follows no one, returning empty feed", userId);
            return Page.empty();
        }

        // Step 2: Fetch recent photos from followed users
        List<Photo> candidatePhotos = fetchRecentPhotosFromFollowing(followingIds);

        // Step 3: Apply ranking algorithm
        List<Photo> rankedPhotos = rankPhotos(candidatePhotos);

        // Step 4: Convert to response and paginate
        return paginateAndConvert(rankedPhotos, currentUser, PageRequest.of(page, size));
    }

    @Override
    public Page<PhotoResponse> getCachedNewsfeed(String userId, int page, int size) {
        log.info("Retrieving cached newsfeed for user: {}", userId);
        User currentUser = userService.findUserById(userId);

        String cacheKey = NEWSFEED_CACHE_KEY + userId;

        try {
            // Get cached photo IDs
            List<String> cachedPhotoIds = (List<String>) redisTemplate.opsForValue().get(cacheKey);

            if (cachedPhotoIds == null || cachedPhotoIds.isEmpty()) {
                log.info("No cached feed found for user: {}, generating new one", userId);
                generateNewsfeedCache(userId);
                cachedPhotoIds = (List<String>) redisTemplate.opsForValue().get(cacheKey);
            }

            if (cachedPhotoIds == null || cachedPhotoIds.isEmpty()) {
                return Page.empty();
            }

            // Paginate cached IDs
            Pageable pageable = PageRequest.of(page, size);
            int start = (int) pageable.getOffset();
            int end = Math.min(start + size, cachedPhotoIds.size());

            if (start >= cachedPhotoIds.size()) {
                return Page.empty();
            }

            List<String> pagePhotoIds = cachedPhotoIds.subList(start, end);

            // Fetch photos and convert to response
            List<Photo> photos = photoRepository.findAllById(pagePhotoIds);

            List<PhotoResponse> photoResponses = photos.stream()
                    .map(photo -> photoConversionService.convertToPhotoResponse(photo, currentUser))
                    .toList();

            // Maintain order from cache
            photoResponses.sort((a, b) ->
                    Integer.compare(pagePhotoIds.indexOf(a.getId()), pagePhotoIds.indexOf(b.getId())));

            return new PageImpl<>(photoResponses, pageable, cachedPhotoIds.size());

        } catch (Exception e) {
            log.error("Error retrieving cached newsfeed for user: {}", userId, e);
            // Fallback to real-time generation
            return getNewsfeed(userId, page, size);
        }
    }

    @Override
    public void generateNewsfeedCache(String userId) {
        log.info("Generating newsfeed cache for user: {}", userId);

        try {
            // Generate feed using real-time algorithm
            List<String> followingIds = getFollowingUserIds(userId);
            if (followingIds.isEmpty()) {
                return;
            }

            List<Photo> candidatePhotos = fetchRecentPhotosFromFollowing(followingIds);
            List<Photo> rankedPhotos = rankPhotos(candidatePhotos);

            // Limit cache size for performance
            List<String> photoIds = rankedPhotos.stream()
                    .limit(MAX_CACHED_ITEMS)
                    .map(Photo::getId)
                    .collect(Collectors.toList());

            // Store in cache
            String cacheKey = NEWSFEED_CACHE_KEY + userId;
            redisTemplate.opsForValue().set(cacheKey, photoIds, CACHE_TTL);

            log.info("Cached {} photos for user: {}", photoIds.size(), userId);

        } catch (Exception e) {
            log.error("Error generating newsfeed cache for user: {}", userId, e);
        }
    }

    @Override
    public void updateFollowersFeeds(String photoId, String authorId) {
        log.info("Updating followers' feeds with new photo: {} from author: {}", photoId, authorId);

        try {
            // Get all followers of the photo author
            List<Follow> followers = followRepository.findByFollowingId(authorId);

            for (Follow follow : followers) {
                String followerId = follow.getFollowerId();
                String cacheKey = NEWSFEED_CACHE_KEY + followerId;

                // Get current cached feed
                List<String> cachedPhotoIds = (List<String>) redisTemplate.opsForValue().get(cacheKey);

                if (cachedPhotoIds != null) {
                    // Add new photo to the beginning of feed
                    List<String> updatedFeed = new ArrayList<>();
                    updatedFeed.add(photoId);
                    updatedFeed.addAll(cachedPhotoIds);

                    // Limit size
                    if (updatedFeed.size() > MAX_CACHED_ITEMS) {
                        updatedFeed = updatedFeed.subList(0, MAX_CACHED_ITEMS);
                    }

                    // Update cache
                    redisTemplate.opsForValue().set(cacheKey, updatedFeed, CACHE_TTL);
                }
            }

            log.info("Updated feeds for {} followers", followers.size());

        } catch (Exception e) {
            log.error("Error updating followers' feeds for photo: {}", photoId, e);
        }
    }

    @Override
    public Page<PhotoResponse> getSmartNewsfeed(String userId, int page, int size) {
        log.info("Getting smart newsfeed for user: {}", userId);

        // Try cache first
        try {
            String cacheKey = NEWSFEED_CACHE_KEY + userId;
            List<String> cachedPhotoIds = (List<String>) redisTemplate.opsForValue().get(cacheKey);

            if (cachedPhotoIds != null && !cachedPhotoIds.isEmpty()) {
                // Check cache freshness
                Long cacheTimestamp = redisTemplate.opsForValue().getOperations()
                        .getExpire(cacheKey);

                if (cacheTimestamp != null && cacheTimestamp > Duration.ofMinutes(30).getSeconds()) {
                    return getCachedNewsfeed(userId, page, size);
                }
            }
        } catch (Exception e) {
            log.warn("Cache access failed, falling back to real-time: {}", e.getMessage());
        }

        // Fallback to real-time generation
        return getNewsfeed(userId, page, size);
    }

    // helper methods
    private List<String> getFollowingUserIds(String userId) {
        List<Follow> following = followRepository.findByFollowerId(userId);
        return following.stream()
                .map(Follow::getFollowingId)
                .toList();
    }

    private List<Photo> fetchRecentPhotosFromFollowing(List<String> followingIds) {
        Instant cutoffTime = Instant.now().minus(Duration.ofDays(RELEVANCE_WINDOW_DAYS));
        return photoRepository.findByUser_UserIdInAndCreatedAtAfterOrderByCreatedAtDesc(followingIds, cutoffTime);
    }

    private List<Photo> rankPhotos(List<Photo> photos) {
        return photos.stream()
                .map(photo -> new PhotoWithScore(photo, calculateRelevantScore(photo)))
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .map(photoWithScore -> photoWithScore.photo)
                .toList();
    }

    private double calculateRelevantScore(Photo photo) {
        double score = 0.0;

        // calculate time duration from posted to now
        long hoursOld = Duration.between(photo.getCreatedAt(), Instant.now()).toHours();
        // score range (0, 100), minus 2 each hour
        score += Math.max(0, 100 - hoursOld * 2);
        // add 2 score for each 10 likes
        score += photo.getLikeCount();
        // add 2 score for each 5 comment
        score += photo.getCommentCount() * 2;

        // quality photo signals
        if (photo.getCaption() != null && !photo.getCaption().trim().isEmpty()) {
            score += 10;
        }

        return score;
    }

    private Page<PhotoResponse> paginateAndConvert(List<Photo> photos, User currentUser, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + (int) pageable.getOffset() + pageable.getPageSize(), photos.size());

        if (start >= photos.size()) {
            return Page.empty();
        }

        List<Photo> pagePhotos = photos.subList(start, end);

        List<PhotoResponse> photoResponses = pagePhotos.stream()
                .map(photo -> photoConversionService.convertToPhotoResponse(photo, currentUser))
                .toList();

        return new PageImpl<>(photoResponses, pageable, photos.size());
    }

    // helper class for ranking
    private static class PhotoWithScore {
        final Photo photo;
        final double score;

        PhotoWithScore(Photo photo, double score) {
            this.photo = photo;
            this.score = score;
        }
    }
}