package share_app.tphucshareapp.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.PartialIndexFilter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MongoIndexConfig {

    private final MongoTemplate mongoTemplate;

    @Bean
    public CommandLineRunner createIndexes() {
        return args -> {
            log.info("Creating MongoDB indexes for optimal performance...");

            createUserIndexes();
            createPhotoIndexes();
            createLikeIndexes();
            createCommentIndexes();
            createFollowIndexes();
            createTagIndexes();
            createPhotoTagIndexes();

            log.info("MongoDB indexes created successfully!");
        };
    }

    private void createUserIndexes() {
        String collection = "users";

        // Basic lookup indexes
        ensureIndex(collection,
                new Index().on("username", Sort.Direction.ASC).unique());

        ensureIndex(collection,
                new Index().on("email", Sort.Direction.ASC).unique().sparse());

        // User listing/search
        ensureIndex(collection,
                new Index().on("createdAt", Sort.Direction.DESC));

        log.info("✓ User indexes created");
    }

    private void createPhotoIndexes() {
        String collection = "photos";

        // Core newsfeed query: user's photos by time
        ensureIndex(collection,
                new CompoundIndexDefinition(
                        new org.bson.Document("userId", 1)
                                .append("createdAt", -1)
                ));

        // Global feed: all photos by time
        ensureIndex(collection,
                new Index().on("createdAt", Sort.Direction.DESC));

        // Newsfeed optimization: recent photos only (partial index)
        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        ensureIndex(collection,
                new CompoundIndexDefinition(
                        new org.bson.Document("userId", 1)
                                .append("createdAt", -1)
                ).partial(
                        PartialIndexFilter.of(
                                new org.bson.Document("createdAt",
                                        new org.bson.Document("$gte", sevenDaysAgo))
                        )
                ));

        // Photo owner lookup
        ensureIndex(collection,
                new Index().on("userId", Sort.Direction.ASC));

        log.info("✓ Photo indexes created");
    }

    private void createLikeIndexes() {
        String collection = "likes";

        // Primary: prevent duplicate likes
        ensureIndex(collection,
                new CompoundIndexDefinition(
                        new org.bson.Document("photoId", 1)
                                .append("userId", 1)
                ).unique());

        // Like counting (most frequent query)
        ensureIndex(collection,
                new Index().on("photoId", Sort.Direction.ASC));

        // Photo likes with time ordering
        ensureIndex(collection,
                new CompoundIndexDefinition(
                        new org.bson.Document("photoId", 1)
                                .append("createdAt", -1)
                ));

        // User's liked photos
        ensureIndex(collection,
                new CompoundIndexDefinition(
                        new org.bson.Document("userId", 1)
                                .append("createdAt", -1)
                ));

        log.info("✓ Like indexes created");
    }

    private void createCommentIndexes() {
        String collection = "comments";

        // Photo comments (ordered chronologically)
        ensureIndex(collection,
                new CompoundIndexDefinition(
                        new org.bson.Document("photoId", 1)
                                .append("createdAt", 1)  // ASC for chronological order
                ));

        // Comment counting
        ensureIndex(collection,
                new Index().on("photoId", Sort.Direction.ASC));

        // User's comments
        ensureIndex(collection,
                new CompoundIndexDefinition(
                        new org.bson.Document("userId", 1)
                                .append("createdAt", -1)
                ));

        log.info("✓ Comment indexes created");
    }

    private void createFollowIndexes() {
        String collection = "follows";

        // Primary: prevent duplicate follows
        ensureIndex(collection,
                new CompoundIndexDefinition(
                        new org.bson.Document("followerId", 1)
                                .append("followingId", 1)
                ).unique());

        // Get user's followers (with time)
        ensureIndex(collection,
                new CompoundIndexDefinition(
                        new org.bson.Document("followingId", 1)
                                .append("createdAt", -1)
                ));

        // Get who user follows (with time)
        ensureIndex(collection,
                new CompoundIndexDefinition(
                        new org.bson.Document("followerId", 1)
                                .append("createdAt", -1)
                ));

        // Count followers/following
        ensureIndex(collection,
                new Index().on("followingId", Sort.Direction.ASC));

        ensureIndex(collection,
                new Index().on("followerId", Sort.Direction.ASC));

        log.info("✓ Follow indexes created");
    }

    private void createTagIndexes() {
        String collection = "tags";

        // Tag name lookup (unique)
        ensureIndex(collection,
                new Index().on("name", Sort.Direction.ASC).unique());

        // Tag search (case-insensitive)
        ensureIndex(collection,
                new Index().on("name", Sort.Direction.ASC));

        // Recent tags
        ensureIndex(collection,
                new Index().on("createdAt", Sort.Direction.DESC));

        log.info("✓ Tag indexes created");
    }

    private void createPhotoTagIndexes() {
        String collection = "photo_tag";

        // Primary: prevent duplicate photo-tag pairs
        ensureIndex(collection,
                new CompoundIndexDefinition(
                        new org.bson.Document("photoId", 1)
                                .append("tagId", 1)
                ).unique());

        // Get photo's tags
        ensureIndex(collection,
                new Index().on("photoId", Sort.Direction.ASC));

        // Get photos by tag
        ensureIndex(collection,
                new CompoundIndexDefinition(
                        new org.bson.Document("tagId", 1)
                                .append("createdAt", -1)
                ));

        log.info("✓ PhotoTag indexes created");
    }

    private void createSearchIndexes() {
        String userCollection = "users";
        String photoCollection = "photos";

        // Text indexes for full-text search
        try {
            // User text index
            ensureIndex(userCollection,
                    new CompoundIndexDefinition(
                            new org.bson.Document()
                                    .append("username", "text")
                                    .append("firstName", "text")
                                    .append("lastName", "text")
                                    .append("bio", "text")
                    ).weights(new org.bson.Document()
                            .append("username", 3)
                            .append("firstName", 2)
                            .append("lastName", 2)
                            .append("bio", 1)
                    ));

            // Photo text index
            ensureIndex(photoCollection,
                    new CompoundIndexDefinition(
                            new org.bson.Document("caption", "text")
                    ).weights(new org.bson.Document("caption", 2)));

            log.info("✓ Text search indexes created");

        } catch (Exception e) {
            log.warn("Text index creation failed, using fallback regex search: {}", e.getMessage());
        }

        // Regex search fallback indexes
        ensureIndex(userCollection,
                new Index().on("username", Sort.Direction.ASC));

        ensureIndex(userCollection,
                new Index().on("firstName", Sort.Direction.ASC));

        ensureIndex(userCollection,
                new Index().on("lastName", Sort.Direction.ASC));

        ensureIndex(photoCollection,
                new Index().on("caption", Sort.Direction.ASC));

        log.info("✓ Search fallback indexes created");
    }

    /**
     * Helper method to safely create indexes
     */
    private void ensureIndex(String collection, IndexDefinition indexDefinition) {
        try {
            mongoTemplate.indexOps(collection).ensureIndex(indexDefinition);
        } catch (Exception e) {
            log.warn("Index creation failed for collection {}: {}", collection, e.getMessage());
        }
    }
}
