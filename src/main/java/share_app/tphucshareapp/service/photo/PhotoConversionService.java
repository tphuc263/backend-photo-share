package share_app.tphucshareapp.service.photo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.Tag;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.CommentRepository;
import share_app.tphucshareapp.repository.LikeRepository;
import share_app.tphucshareapp.repository.UserRepository;
import share_app.tphucshareapp.service.tag.TagService;
import share_app.tphucshareapp.service.user.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoConversionService {

    private final ModelMapper modelMapper;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final TagService tagService;
    private final UserService userService;

    /**
     * Convert Photo entity to PhotoResponse DTO
     */
    public PhotoResponse convertToPhotoResponse(Photo photo) {
        User photoOwner = userRepository.findById(photo.getUserId())
                .orElseThrow(() -> new RuntimeException("Photo owner not found"));
        return convertToPhotoResponse(photo, photoOwner);
    }

    /**
     * Convert Photo entity to PhotoResponse DTO with provided user info
     */
    public PhotoResponse convertToPhotoResponse(Photo photo, User photoOwner) {
        PhotoResponse response = modelMapper.map(photo, PhotoResponse.class);
        response.setUsername(photoOwner.getUsername());
        response.setUserImageUrl(photoOwner.getImageUrl());

        // Set interaction counts
        response.setLikesCount((int) likeRepository.countByPhotoId(photo.getId()));
        response.setCommentsCount((int) commentRepository.countByPhotoId(photo.getId()));

        // Check if current user liked this photo
        setCurrentUserLikeStatus(response, photo.getId());

        // Add tags
        List<Tag> tags = tagService.getPhotoTags(photo.getId());
        response.setTags(tags.stream().map(Tag::getName).toList());

        return response;
    }

    /**
     * Set like status for current authenticated user
     */
    private void setCurrentUserLikeStatus(PhotoResponse response, String photoId) {
        try {
            User currentUser = userService.getCurrentUser();
            response.setLikedByCurrentUser(
                    likeRepository.existsByPhotoIdAndUserId(photoId, currentUser.getId())
            );
        } catch (Exception e) {
            // User not authenticated, set to false
            response.setLikedByCurrentUser(false);
        }
    }
}
