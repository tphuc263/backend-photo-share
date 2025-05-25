package share_app.tphucshareapp.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import share_app.tphucshareapp.service.photo.NewsfeedService;

/**
 * Event listener for photo-related events
 * Handles newsfeed updates asynchronously when photos are created
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PhotoEventListener {

    private final NewsfeedService newsfeedService;

    /**
     * Handle photo creation event by updating followers' newsfeeds
     * Runs asynchronously to avoid blocking photo creation
     */
    @EventListener
    @Async("eventExecutor")
    public void handlePhotoCreated(PhotoCreatedEvent event) {
        log.info("Handling photo created event - photoId: {}, authorId: {}",
                event.getPhotoId(), event.getAuthorId());

        try {
            newsfeedService.updateFollowersFeeds(event.getPhotoId(), event.getAuthorId());
            log.info("Successfully updated followers' feeds for photo: {}", event.getPhotoId());
        } catch (Exception e) {
            log.error("Error updating followers' feeds for photo: {}", event.getPhotoId(), e);
            // In production, you might want to retry or send to dead letter queue
        }
    }
}
