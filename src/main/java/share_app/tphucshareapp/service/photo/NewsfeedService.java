package share_app.tphucshareapp.service.photo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.model.Follow;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.FollowRepository;
import share_app.tphucshareapp.service.user.UserService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsfeedService implements INewsfeedService {
    private final UserService userService;
    private final FollowRepository followRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public Page<PhotoResponse> getNewsfeed(int page, int size) {
        log.info("Fetching newsfeed for page: {}, size: {}", page, size);

        try {
            User currentUser = userService.getCurrentUser();

            // Get list of users that current user follows
            List<Follow> followings = followRepository.findByFollowerId(currentUser.getId());

            if (followings.isEmpty()) {
                log.info("User {} doesn't follow anyone, returning trending photos", currentUser.getId());
                return getTrendingPhotos(page, size);
            }

            // Extract following user IDs
            List<String> followingUserIds = followings.stream()
                    .map(Follow::getFollowingId)
                    .collect(Collectors.toList());

            log.info("User {} follows {} users", currentUser.getId(), followingUserIds.size());

            // Use MongoDB aggregation for efficient newsfeed query
            return getNewsfeedUsingAggregation(followingUserIds, page, size);

        } catch (Exception e) {
            log.error("Error generating newsfeed", e);
            // Fallback to trending photos if error occurs
            return getTrendingPhotos(page, size);
        }
    }

    @Override
    public Page<PhotoResponse> getRecommendations(int page, int size) {
        log.info("Fetching recommendations for page: {}, size: {}", page, size);

        try {
            User currentUser = userService.getCurrentUser();

            // Simple recommendation: photos from users with similar interests
            // This can be enhanced with ML algorithms later

            // For now, return photos from users that current user's followings also follow
            List<Follow> currentUserFollowings = followRepository.findByFollowerId(currentUser.getId());

            if (currentUserFollowings.isEmpty()) {
                return getTrendingPhotos(page, size);
            }

            // Get users that are followed by people current user follows
            // This creates a "friends of friends" recommendation
            return getRecommendationsUsingAggregation(currentUserFollowings, currentUser.getId(), page, size);

        } catch (Exception e) {
            log.error("Error generating recommendations", e);
            return getTrendingPhotos(page, size);
        }
    }

    @Override
    public Page<PhotoResponse> getTrendingPhotos(int page, int size) {
        log.info("Fetching trending photos for page: {}, size: {}", page, size);

        // Trending = photos with most likes in last 7 days
        Instant weekAgo = Instant.now().minus(7, ChronoUnit.DAYS);

        return getTrendingUsingAggregation(weekAgo, page, size);
    }

    // helper methods
    /**
     * Use MongoDB aggregation to efficiently get newsfeed
     * This joins photos with user info and like/comment counts
     */
    private Page<PhotoResponse> getNewsfeedUsingAggregation(List<String> followingUserIds, int page, int size) {
        // Create aggregation pipeline
        Aggregation aggregation = Aggregation.newAggregation(
                // Stage 1: Match photos from followed users
                Aggregation.match(Criteria.where("userId").in(followingUserIds)),

                // Stage 2: Sort by creation time (newest first)
                Aggregation.sort(DESC, "createdAt"),

                // Stage 3: Skip for pagination
                Aggregation.skip((long) page * size),

                // Stage 4: Limit for pagination
                Aggregation.limit(size),

                // Stage 5: Lookup user information
                Aggregation.lookup("users", "userId", "_id", "userInfo"),

                // Stage 6: Unwind user info
                Aggregation.unwind("userInfo"),

                // Stage 7: Lookup likes count
                Aggregation.lookup("likes", "_id", "photoId", "likes"),

                // Stage 8: Lookup comments count
                Aggregation.lookup("comments", "_id", "photoId", "comments"),

                // Stage 9: Add computed fields
                Aggregation.project()
                        .and("_id").as("id")
                        .and("userId").as("userId")
                        .and("userInfo.username").as("username")
                        .and("userInfo.imageUrl").as("userImageUrl")
                        .and("imageURL").as("imageURL")
                        .and("caption").as("caption")
                        .and("createdAt").as("createdAt")
                        .and(ArrayOperators.Size.lengthOfArray("likes")).as("likesCount")
                        .and(ArrayOperators.Size.lengthOfArray("comments")).as("commentsCount")
        );

        AggregationResults<PhotoResponse> results = mongoTemplate.aggregate(
                aggregation, "photos", PhotoResponse.class);

        List<PhotoResponse> photoResponses = results.getMappedResults();

        // Get total count for pagination (this could be optimized with a separate count query)
        long totalElements = getTotalNewsfeedCount(followingUserIds);

        // Set additional fields that aggregation doesn't handle
        enrichPhotoResponses(photoResponses);

        return new PageImpl<>(photoResponses, PageRequest.of(page, size), totalElements);
    }

    /**
     * Get trending photos using aggregation with like counts
     */
    private Page<PhotoResponse> getTrendingUsingAggregation(Instant since, int page, int size) {
        Aggregation aggregation = Aggregation.newAggregation(
                // Stage 1: Match photos since given date
                Aggregation.match(Criteria.where("createdAt").gte(since)),

                // Stage 2: Lookup likes
                Aggregation.lookup("likes", "_id", "photoId", "likes"),

                // Stage 3: Add likes count
                Aggregation.addFields()
                        .addField("likesCount").withValue(ArrayOperators.Size.lengthOfArray("likes"))
                        .build(),

                // Stage 4: Sort by likes count desc, then by creation time desc
                Aggregation.sort(
                        DESC, "likesCount",
                        DESC, "createdAt"
                ),

                // Stage 5: Skip and limit for pagination
                Aggregation.skip((long) page * size),
                Aggregation.limit(size),

                // Stage 6: Lookup user info
                Aggregation.lookup("users", "userId", "_id", "userInfo"),
                Aggregation.unwind("userInfo"),

                // Stage 7: Lookup comments
                Aggregation.lookup("comments", "_id", "photoId", "comments"),

                // Stage 8: Project final fields
                Aggregation.project()
                        .and("_id").as("id")
                        .and("userId").as("userId")
                        .and("userInfo.username").as("username")
                        .and("userInfo.imageUrl").as("userImageUrl")
                        .and("imageURL").as("imageURL")
                        .and("caption").as("caption")
                        .and("createdAt").as("createdAt")
                        .and("likesCount").as("likesCount")
                        .and(ArrayOperators.Size.lengthOfArray("comments")).as("commentsCount")
        );

        AggregationResults<PhotoResponse> results = mongoTemplate.aggregate(
                aggregation, "photos", PhotoResponse.class);

        List<PhotoResponse> photoResponses = results.getMappedResults();

        // Get total trending photos count
        long totalElements = getTotalTrendingCount(since);

        enrichPhotoResponses(photoResponses);

        return new PageImpl<>(photoResponses, PageRequest.of(page, size), totalElements);
    }

    /**
     * Get recommendations using aggregation for "friends of friends"
     */
    private Page<PhotoResponse> getRecommendationsUsingAggregation(
            List<Follow> currentUserFollowings, String currentUserId, int page, int size) {

        List<String> followingIds = currentUserFollowings.stream()
                .map(Follow::getFollowingId)
                .collect(Collectors.toList());

        // Find users that are followed by current user's followings (but not by current user)
        Aggregation aggregation = Aggregation.newAggregation(
                // Stage 1: Match follows where follower is someone current user follows
                Aggregation.match(Criteria.where("followerId").in(followingIds)),

                // Stage 2: Group by followingId to get recommended users
                Aggregation.group("followingId").count().as("connections"),

                // Stage 3: Sort by number of connections (more mutual connections = better recommendation)
                Aggregation.sort(DESC, "connections"),

                // Stage 4: Project to get user IDs
                Aggregation.project().and("_id").as("userId")
        );

        AggregationResults<RecommendedUser> recommendedUsers = mongoTemplate.aggregate(
                aggregation, "follows", RecommendedUser.class);

        List<String> recommendedUserIds = recommendedUsers.getMappedResults().stream()
                .map(RecommendedUser::getUserId)
                .filter(userId -> !userId.equals(currentUserId)) // Exclude current user
                .filter(userId -> !followingIds.contains(userId)) // Exclude already followed users
                .collect(Collectors.toList());

        if (recommendedUserIds.isEmpty()) {
            return getTrendingPhotos(page, size);
        }

        // Get photos from recommended users
        return getNewsfeedUsingAggregation(recommendedUserIds, page, size);
    }

    // Helper methods
    private long getTotalNewsfeedCount(List<String> followingUserIds) {
        return mongoTemplate.count(
                org.springframework.data.mongodb.core.query.Query.query(
                        Criteria.where("userId").in(followingUserIds)
                ), Photo.class
        );
    }

    private long getTotalTrendingCount(Instant since) {
        return mongoTemplate.count(
                org.springframework.data.mongodb.core.query.Query.query(
                        Criteria.where("createdAt").gte(since)
                ), Photo.class
        );
    }

    private void enrichPhotoResponses(List<PhotoResponse> photoResponses) {
        try {
            userService.getCurrentUser();

            for (PhotoResponse photo : photoResponses) {
                // Set isLikedByCurrentUser
                // This could be optimized by including in aggregation, but for simplicity doing it here
                photo.setLikedByCurrentUser(false); // Will be set properly in PhotoService if needed

                // Set tags - this could also be optimized
                photo.setTags(List.of()); // Placeholder for now
            }
        } catch (Exception e) {
            log.warn("Could not enrich photo responses (user not authenticated)", e);
        }
    }

    // Helper class for aggregation results
    @Setter
    @Getter
    private static class RecommendedUser {
        private String userId;
    }
}
