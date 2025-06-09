package share_app.tphucshareapp.service.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.request.comment.CreateCommentRequest;
import share_app.tphucshareapp.dto.request.comment.UpdateCommentRequest;
import share_app.tphucshareapp.dto.response.comment.CommentResponse;
import share_app.tphucshareapp.model.Comment;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.CommentRepository;
import share_app.tphucshareapp.repository.PhotoRepository;
import share_app.tphucshareapp.repository.UserRepository;
import share_app.tphucshareapp.service.user.UserService;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService implements ICommentService {
    private final CommentRepository commentRepository;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final MongoTemplate mongoTemplate;
    private final ModelMapper modelMapper;

    @Override
    public CommentResponse createComment(String photoId, CreateCommentRequest request) {
        // Validate photo exists
        photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));

        User currentUser = userService.getCurrentUser();

        Comment.EmbeddedUser embeddedUser = new Comment.EmbeddedUser();
        embeddedUser.setUsername(currentUser.getUsername());
        embeddedUser.setUserImageUrl(currentUser.getImageUrl());

        // Create comment
        Comment comment = new Comment();
        comment.setPhotoId(photoId);
        comment.setUserId(currentUser.getId());
        comment.setText(request.getText());
        comment.setCreatedAt(Instant.now());
        comment.setUser(embeddedUser);

        Comment savedComment = commentRepository.save(comment);

        Query query = new Query(Criteria.where("_id").is(photoId));
        Update update = new Update().inc("commentCount", 1);
        mongoTemplate.updateFirst(query, update, Photo.class);
        log.info("Comment created successfully by user {} on photo {}", currentUser.getId(), photoId);

        return convertToCommentResponse(savedComment);
    }

    @Override
    public CommentResponse updateComment(String commentId, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + commentId));

        User currentUser = userService.getCurrentUser();

        // Check if current user is the owner of the comment
        if (!comment.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only update your own comments");
        }

        comment.setText(request.getText());
        Comment updatedComment = commentRepository.save(comment);
        log.info("Comment {} updated successfully by user {}", commentId, currentUser.getId());

        return convertToCommentResponse(updatedComment);
    }

    @Override
    public void deleteComment(String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + commentId));

        User currentUser = userService.getCurrentUser();

        // Check if current user is the owner of the comment
        if (!comment.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
        Query query = new Query(Criteria.where("_id").is(comment.getPhotoId()));
        Update update = new Update().inc("commentCount", -1);
        mongoTemplate.updateFirst(query, update, Photo.class);
        log.info("Comment {} deleted successfully by user {}", commentId, currentUser.getId());
    }

    @Override
    public List<CommentResponse> getPhotoComments(String photoId) {
        // Validate photo exists
        photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));

        List<Comment> comments = commentRepository.findByPhotoIdOrderByCreatedAtAsc(photoId);
        return convertToCommentResponses(comments);
    }

    @Override
    public long getPhotoCommentsCount(String photoId) {
        return commentRepository.countByPhotoId(photoId);
    }

    @Override
    public CommentResponse getComment(String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + commentId));

        User user = userRepository.findById(comment.getUserId())
                .orElseThrow(() -> new RuntimeException("Comment owner not found"));

        return convertToCommentResponse(comment);
    }

    // Helper methods
    private CommentResponse convertToCommentResponse(Comment comment) {
        CommentResponse response = modelMapper.map(comment, CommentResponse.class);
        if (comment.getUser() != null) {
            response.setUsername(comment.getUser().getUsername());
            response.setUserImageUrl(comment.getUser().getUserImageUrl());
        }
        return response;
    }

    private List<CommentResponse> convertToCommentResponses(List<Comment> comments) {
        return comments.stream()
                .map(this::convertToCommentResponse)
                .toList();
    }
}
