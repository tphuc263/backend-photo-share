package share_app.tphucshareapp.service.like;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.response.like.LikeResponse;
import share_app.tphucshareapp.model.Like;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.LikeRepository;
import share_app.tphucshareapp.repository.PhotoRepository;
import share_app.tphucshareapp.repository.UserRepository;
import share_app.tphucshareapp.service.user.UserService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService implements ILikeService {
    private final LikeRepository likeRepository;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final MongoTemplate mongoTemplate;

    @Override
    public void like(String photoId) {
        User currentUser = userService.getCurrentUser();
        photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));

        boolean alreadyLiked = likeRepository.existsByPhotoIdAndUserId(photoId, currentUser.getId());
        if (alreadyLiked) {
            throw new RuntimeException("You have already liked this photo");
        }

        Like like = new Like();
        like.setPhotoId(photoId);
        like.setUserId(currentUser.getId());
        like.setCreatedAt(Instant.now());

        likeRepository.save(like);

        Query query = new Query(Criteria.where("_id").is(photoId));
        Update update = new Update().inc("likeCount", 1);
        mongoTemplate.updateFirst(query, update, Photo.class);

        log.info("User {} liked photo {}", currentUser.getId(), photoId);
    }

    @Override
    public void unlike(String photoId) {
        User currentUser = userService.getCurrentUser();
        Like like = likeRepository.findByPhotoIdAndUserId(photoId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("You have not liked this photo"));

        likeRepository.delete(like);

        Query query = new Query(Criteria.where("_id").is(photoId));
        Update update = new Update().inc("likeCount", -1);
        mongoTemplate.updateFirst(query, update, Photo.class);

        log.info("User {} unliked photo {}", currentUser.getId(), photoId);
    }

    @Override
    public List<LikeResponse> getPhotoLikes(String photoId) {
        // Validate photo exists
        photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));

        List<Like> likes = likeRepository.findByPhotoIdOrderByCreatedAtDesc(photoId);
        return convertToLikeResponses(likes);
    }

    @Override
    public long getPhotoLikesCount(String photoId) {
        return photoRepository.findById(photoId)
                .map(Photo::getLikeCount)
                .orElse(0L);
    }

    // Helper method
    private List<LikeResponse> convertToLikeResponses(List<Like> likes) {
        // Get all user IDs and fetch users in batch for performance
        List<String> userIds = likes.stream()
                .map(Like::getUserId)
                .distinct()
                .toList();

        Map<String, User> usersMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        return likes.stream()
                .map(like -> {
                    LikeResponse response = modelMapper.map(like, LikeResponse.class);
                    User user = usersMap.get(like.getUserId());
                    if (user != null) {
                        response.setUsername(user.getUsername());
                        response.setUserImageUrl(user.getImageUrl());
                    }
                    return response;
                })
                .toList();
    }
}
