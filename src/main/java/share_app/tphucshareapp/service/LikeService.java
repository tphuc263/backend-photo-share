package share_app.tphucshareapp.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import share_app.tphucshareapp.dto.request.user.UserBasicInfo;
import share_app.tphucshareapp.model.Like;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.LikeRepository;
import share_app.tphucshareapp.repository.PhotoRepository;
import share_app.tphucshareapp.repository.UserRepository;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Transactional
    @CacheEvict(value = "photoLikeCount", key = "#photoId")
    public void likePhoto(String photoId, String userId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));

        Optional<Like> existingLike = likeRepository.findByPhotoIdAndUserId(photoId, userId);

        if (existingLike.isPresent()) {
            return;
        }

        // Tạo like mới
        Like like = new Like();
        like.setPhotoId(photoId);
        like.setUserId(userId);
        like.setCreatedAt(Instant.now());

        // Lưu vào DB
        likeRepository.save(like);
    }

    /**
     * Bỏ thích một ảnh
     */
    @Transactional
    @CacheEvict(value = "photoLikeCount", key = "#photoId")
    public void unlikePhoto(String photoId, String userId) {
        // Kiểm tra like có tồn tại
        Like like = likeRepository.findByPhotoIdAndUserId(photoId, userId)
                .orElseThrow(() -> new RuntimeException("Chưa thích ảnh này"));

        // Xóa like
        likeRepository.delete(like);
    }

    /**
     * Kiểm tra người dùng đã thích ảnh chưa
     */
    public boolean isLiked(String photoId, String userId) {
        return likeRepository.existsByPhotoIdAndUserId(photoId, userId);
    }

    /**
     * Lấy danh sách người đã thích ảnh
     */
    public Page<UserBasicInfo> getLikesByPhoto(String photoId, Pageable pageable) {
        // Lấy danh sách like của ảnh
        Page<Like> likes = likeRepository.findByPhotoId(photoId, pageable);

        // Chuyển đổi sang thông tin user
        return likes.map(like -> {
            User user = userRepository.findById(like.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            return modelMapper.map(user, UserBasicInfo.class);
        });
    }

    /**
     * Lấy số lượng like của một ảnh
     */
    @Cacheable(value = "photoLikeCount", key = "#photoId")
    public long getLikeCount(String photoId) {
        return likeRepository.countByPhotoId(photoId);
    }
}
