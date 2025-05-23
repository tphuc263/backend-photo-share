package share_app.tphucshareapp.service.photo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import share_app.tphucshareapp.dto.request.photo.CreatePhotoRequest;
import share_app.tphucshareapp.dto.response.comment.CommentResponse;
import share_app.tphucshareapp.dto.response.like.LikeResponse;
import share_app.tphucshareapp.dto.response.photo.PhotoDetailResponse;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.model.*;
import share_app.tphucshareapp.repository.CommentRepository;
import share_app.tphucshareapp.repository.LikeRepository;
import share_app.tphucshareapp.repository.PhotoRepository;
import share_app.tphucshareapp.repository.UserRepository;
import share_app.tphucshareapp.service.tag.TagService;
import share_app.tphucshareapp.service.user.UserService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoService implements IPhotoService {
    private final CloudinaryService cloudinaryService;
    private final PhotoRepository photoRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final TagService tagService;


    @Override
    public PhotoResponse createPhoto(CreatePhotoRequest request) {
        log.info("Creating new photo with caption: {}", request.getCaption());
        Map<String, Object> uploadResult = cloudinaryService.uploadImage(request.getImage());
        String imageUrl = (String) uploadResult.get("secure_url");
        log.info("Image uploaded successfully, URL: {}", imageUrl);

        // Create photo entity
        User currentUser = userService.getCurrentUser();
        Photo photo = new Photo();
        photo.setUserId(currentUser.getId());
        photo.setImageURL(imageUrl);
        photo.setCaption(request.getCaption());
        photo.setCreatedAt(Instant.now());

        // Save photo to database
        Photo savedPhoto = photoRepository.save(photo);

        // Handle tags
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            List<Tag> tags = tagService.createOrGetTags(request.getTags());
            tagService.addTagsToPhoto(savedPhoto.getId(), tags);
        }

        return convertToPhotoResponse(savedPhoto, currentUser);
    }

    @Override
    public Page<PhotoResponse> getAllPhotos(int page, int size) {
        log.info("Fetching all photos - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Photo> photos = photoRepository.findAllByOrderByCreatedAtDesc(pageable);

        return photos.map(this::convertToPhotoResponse);
    }

    @Override
    public Page<PhotoResponse> getPhotosByUserId(String userId, int page, int size) {
        log.info("Fetching photos for user ID: {} - page: {}, size: {}", userId, page, size);

        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Pageable pageable = PageRequest.of(page, size);
        Page<Photo> photos = photoRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return photos.map(photo -> convertToPhotoResponse(photo, user));
    }

    @Override
    public PhotoDetailResponse getPhotoById(String photoId) {
        log.info("Fetching photo details for ID: {}", photoId);

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));

        User photoOwner = userRepository.findById(photo.getUserId())
                .orElseThrow(() -> new RuntimeException("Photo owner not found"));

        // Get likes and comments
        List<Like> likes = likeRepository.findByPhotoIdOrderByCreatedAtDesc(photoId);
        List<Comment> comments = commentRepository.findByPhotoIdOrderByCreatedAtAsc(photoId);

        // Convert to response
        PhotoDetailResponse response = convertToPhotoDetailResponse(photo, photoOwner);
        response.setLikes(convertToLikeResponses(likes));
        response.setComments(convertToCommentResponses(comments));

        return response;
    }

    @Override
    @Transactional
    public void deletePhoto(String photoId) {
        log.info("Deleting photo with ID: {}", photoId);

        // Verify photo exists and user has permission to delete
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));

        User currentUser = userService.getCurrentUser();

        // Check if current user is the owner of the photo or admin
        if (!photo.getUserId().equals(currentUser.getId()) &&
                !currentUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new RuntimeException("You don't have permission to delete this photo");
        }

        try {
            // Delete image from Cloudinary
            String publicId = cloudinaryService.extractPublicIdFromUrl(photo.getImageURL());
            if (publicId != null) {
                cloudinaryService.deleteImage(publicId);
                log.info("Image deleted from Cloudinary for photo ID: {}", photoId);
            }

            // Delete related data first (to avoid foreign key constraints)
            // Delete all likes for this photo
            List<Like> likes = likeRepository.findByPhotoIdOrderByCreatedAtDesc(photoId);
            if (!likes.isEmpty()) {
                likeRepository.deleteAll(likes);
                log.info("Deleted {} likes for photo ID: {}", likes.size(), photoId);
            }

            // Delete all comments for this photo
            List<Comment> comments = commentRepository.findByPhotoIdOrderByCreatedAtAsc(photoId);
            if (!comments.isEmpty()) {
                commentRepository.deleteAll(comments);
                log.info("Deleted {} comments for photo ID: {}", comments.size(), photoId);
            }

            // Delete photo tags
            tagService.removeTagsFromPhoto(photoId);

            // Finally delete the photo
            photoRepository.deleteById(photoId);
            log.info("Photo deleted successfully with ID: {}", photoId);

        } catch (Exception e) {
            log.error("Failed to delete photo with ID: {}", photoId, e);
            throw new RuntimeException("Failed to delete photo", e);
        }
    }


    // helper methods
    private PhotoResponse convertToPhotoResponse(Photo photo) {
        User photoOwner = userRepository.findById(photo.getUserId())
                .orElseThrow(() -> new RuntimeException("Photo owner not found"));
        return convertToPhotoResponse(photo, photoOwner);
    }
    private PhotoResponse convertToPhotoResponse(Photo photo, User photoOwner) {
        PhotoResponse response = modelMapper.map(photo, PhotoResponse.class);
        response.setUsername(photoOwner.getUsername());
        response.setUserImageUrl(photoOwner.getImageUrl());

        // Set counts
        response.setLikesCount((int) likeRepository.countByPhotoId(photo.getId()));
        response.setCommentsCount((int) commentRepository.countByPhotoId(photo.getId()));

        // Check if current user liked this photo
        try {
            User currentUser = userService.getCurrentUser();
            response.setLikedByCurrentUser(
                    likeRepository.existsByPhotoIdAndUserId(photo.getId(), currentUser.getId())
            );
        } catch (Exception e) {
            // User not authenticated, set to false
            response.setLikedByCurrentUser(false);
        }

        List<Tag> tags = tagService.getPhotoTags(photo.getId());
        response.setTags(tags.stream().map(Tag::getName).toList());

        return response;
    }

    private PhotoDetailResponse convertToPhotoDetailResponse(Photo photo, User photoOwner) {
        PhotoDetailResponse response = modelMapper.map(photo, PhotoDetailResponse.class);
        response.setUsername(photoOwner.getUsername());
        response.setUserImageUrl(photoOwner.getImageUrl());

        // Set counts
        response.setLikesCount((int) likeRepository.countByPhotoId(photo.getId()));
        response.setCommentsCount((int) commentRepository.countByPhotoId(photo.getId()));

        // Check if current user liked this photo
        try {
            User currentUser = userService.getCurrentUser();
            response.setLikedByCurrentUser(
                    likeRepository.existsByPhotoIdAndUserId(photo.getId(), currentUser.getId())
            );
        } catch (Exception e) {
            // User not authenticated, set to false
            response.setLikedByCurrentUser(false);
        }

        return response;
    }

    private List<LikeResponse> convertToLikeResponses(List<Like> likes) {
        // Get all user IDs and fetch users in batch
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

    private List<CommentResponse> convertToCommentResponses(List<Comment> comments) {
        // Get all user IDs and fetch users in batch
        List<String> userIds = comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .toList();

        Map<String, User> usersMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        return comments.stream()
                .map(comment -> {
                    CommentResponse response = modelMapper.map(comment, CommentResponse.class);
                    User user = usersMap.get(comment.getUserId());
                    if (user != null) {
                        response.setUsername(user.getUsername());
                        response.setUserImageUrl(user.getImageUrl());
                    }
                    return response;
                })
                .toList();
    }
}
