package share_app.tphucshareapp.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import share_app.tphucshareapp.dto.request.comment.CommentCreateRequest;
import share_app.tphucshareapp.dto.request.comment.CommentUpdateRequest;
import share_app.tphucshareapp.dto.request.user.UserBasicInfo;
import share_app.tphucshareapp.dto.response.comment.CommentResponse;
import share_app.tphucshareapp.model.Comment;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.CommentRepository;
import share_app.tphucshareapp.repository.PhotoRepository;
import share_app.tphucshareapp.repository.UserRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    /**
     * Tạo bình luận mới
     */
    @Transactional
    public CommentResponse createComment(CommentCreateRequest request, String userId) {
        // Kiểm tra ảnh có tồn tại
        Photo photo = photoRepository.findById(request.getPhotoId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));

        // Tạo comment mới
        Comment comment = new Comment();
        comment.setPhotoId(request.getPhotoId());
        comment.setUserId(userId);
        comment.setText(request.getText());
        comment.setCreatedAt(Instant.now());

        // Lưu vào DB
        comment = commentRepository.save(comment);

        // Chuyển đổi sang CommentResponse
        CommentResponse response = modelMapper.map(comment, CommentResponse.class);

        // Thêm thông tin user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        UserBasicInfo userInfo = modelMapper.map(user, UserBasicInfo.class);
        response.setUser(userInfo);

        return response;
    }

    /**
     * Lấy danh sách bình luận của ảnh
     */
    public Page<CommentResponse> getPhotoComments(String photoId, Pageable pageable) {
        // Lấy danh sách comment của ảnh
        Page<Comment> comments = commentRepository.findByPhotoIdOrderByCreatedAtDesc(photoId, pageable);

        // Chuyển đổi sang CommentResponse
        return comments.map(comment -> {
            CommentResponse response = modelMapper.map(comment, CommentResponse.class);

            // Thêm thông tin user
            User user = userRepository.findById(comment.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            UserBasicInfo userInfo = modelMapper.map(user, UserBasicInfo.class);
            response.setUser(userInfo);

            return response;
        });
    }

    /**
     * Cập nhật bình luận
     */
    @Transactional
    public CommentResponse updateComment(String id, CommentUpdateRequest request, String userId) {
        // Kiểm tra comment có tồn tại
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bình luận"));

        // Kiểm tra quyền sở hữu
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền cập nhật bình luận này");
        }

        // Cập nhật nội dung
        comment.setText(request.getText());

        // Lưu vào DB
        comment = commentRepository.save(comment);

        // Chuyển đổi sang CommentResponse
        CommentResponse response = modelMapper.map(comment, CommentResponse.class);

        // Thêm thông tin user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        UserBasicInfo userInfo = modelMapper.map(user, UserBasicInfo.class);
        response.setUser(userInfo);

        return response;
    }

    /**
     * Xóa bình luận
     */
    @Transactional
    public void deleteComment(String id, String userId) {
        // Kiểm tra comment có tồn tại
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bình luận"));

        // Kiểm tra quyền sở hữu (người tạo comment hoặc chủ ảnh)
        if (!comment.getUserId().equals(userId)) {
            // Kiểm tra xem userId có phải là chủ của ảnh không
            Photo photo = photoRepository.findById(comment.getPhotoId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));

            if (!photo.getUserId().equals(userId)) {
                throw new RuntimeException("Bạn không có quyền xóa bình luận này");
            }
        }

        // Xóa comment
        commentRepository.delete(comment);
    }
}
