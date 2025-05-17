package share_app.tphucshareapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.PhotoTag;
import share_app.tphucshareapp.service.image.CloudinaryService;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final TagRepository tagRepository;
    private final PhotoTagRepository photoTagRepository;
    private final FollowRepository followRepository;
    private final CloudinaryService cloudinaryService;
    private final ModelMapper modelMapper;

    /**
     * Tạo mới ảnh và lưu trữ trên Cloudinary
     */
    @Transactional
    public PhotoResponse createPhoto(MultipartFile file, PhotoCreateRequest request, String userId) {
        try {
            // Tải ảnh lên Cloudinary
            String imageUrl = cloudinaryService.uploadImage(file);

            // Tạo entity Photo
            Photo photo = new Photo();
            photo.setUserId(userId);
            photo.setCaption(request.getCaption());
            photo.setImageURL(imageUrl);
            photo.setCreatedAt(Instant.now());

            // Lưu vào DB
            photo = photoRepository.save(photo);

            // Xử lý hashtags
            processHashtags(photo.getId(), request.getCaption());

            // Chuyển đổi sang DTO và trả về
            return modelMapper.map(photo, PhotoResponse.class);
        } catch (IOException e) {
            log.error("Lỗi khi tải ảnh lên: ", e);
            throw new RuntimeException("Không thể tải ảnh lên. Vui lòng thử lại sau.");
        }
    }

    /**
     * Lấy danh sách tất cả ảnh, sắp xếp theo thời gian tạo
     */
    public Page<PhotoResponse> getAllPhotos(Pageable pageable) {
        Page<Photo> photos = photoRepository.findAllByOrderByCreatedAtDesc(pageable);
        return photos.map(photo -> modelMapper.map(photo, PhotoResponse.class));
    }

    /**
     * Lấy danh sách ảnh của một người dùng
     */
    public Page<PhotoResponse> getUserPhotos(String userId, Pageable pageable) {
        Page<Photo> photos = photoRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return photos.map(photo -> modelMapper.map(photo, PhotoResponse.class));
    }

    /**
     * Lấy feed ảnh cho người dùng (ảnh từ người dùng đang follow)
     */
    public Page<PhotoResponse> getPhotoFeed(String userId, Pageable pageable) {
        // Lấy danh sách userId của những người mà user đang follow
        List<String> followingIds = followRepository.findFollowingIdsByFollowerId(userId);
        // Thêm cả userId của chính user vào để hiển thị ảnh của mình
        followingIds.add(userId);

        // Lấy ảnh của tất cả người mà user đang follow
        Page<Photo> photos = photoRepository.findByUserIdInOrderByCreatedAtDesc(followingIds, pageable);
        return photos.map(photo -> modelMapper.map(photo, PhotoResponse.class));
    }

    /**
     * Lấy chi tiết ảnh bao gồm số like, comment
     */
    public PhotoDetailResponse getPhotoDetail(String id) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));

        // Lấy thông tin chi tiết về ảnh
        PhotoDetailResponse response = modelMapper.map(photo, PhotoDetailResponse.class);

        // Bổ sung thông tin:
        // Số lượng like
        long likeCount = photoRepository.countLikes(id);
        response.setLikeCount(likeCount);

        // Số lượng comment
        long commentCount = photoRepository.countComments(id);
        response.setCommentCount(commentCount);

        // Tags
        List<String> tags = photoTagRepository.findTagNamesByPhotoId(id);
        response.setTags(tags);

        return response;
    }

    /**
     * Cập nhật thông tin ảnh
     */
    @Transactional
    public PhotoResponse updatePhoto(String id, PhotoUpdateRequest request, String userId) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));

        // Kiểm tra quyền sở hữu
        if (!photo.getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền cập nhật ảnh này");
        }

        // Cập nhật thông tin
        photo.setCaption(request.getCaption());

        // Lưu thay đổi
        photo = photoRepository.save(photo);

        // Xử lý lại hashtags
        photoTagRepository.deleteByPhotoId(id); // Xóa tất cả tag cũ
        processHashtags(id, request.getCaption()); // Tạo tag mới

        return modelMapper.map(photo, PhotoResponse.class);
    }

    /**
     * Xóa ảnh
     */
    @Transactional
    public void deletePhoto(String id, String userId) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));

        // Kiểm tra quyền sở hữu
        if (!photo.getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xóa ảnh này");
        }

        // Xóa liên kết tags
        photoTagRepository.deleteByPhotoId(id);

        // Xóa comments
        // commentRepository.deleteByPhotoId(id);

        // Xóa likes
        // likeRepository.deleteByPhotoId(id);

        // Xóa ảnh từ Cloudinary
        String publicId = extractPublicIdFromUrl(photo.getImageURL());
        if (publicId != null) {
            try {
                cloudinaryService.deleteImage(publicId);
            } catch (IOException e) {
                log.error("Lỗi khi xóa ảnh từ Cloudinary: ", e);
                // Tiếp tục xóa record trong DB ngay cả khi xóa từ cloud thất bại
            }
        }

        // Xóa ảnh từ DB
        photoRepository.deleteById(id);
    }

    /**
     * Tìm kiếm ảnh theo caption hoặc tag
     */
    public Page<PhotoResponse> searchPhotos(String query, Pageable pageable) {
        Page<Photo> photos = photoRepository.findByCaptionContainingIgnoreCase(query, pageable);
        return photos.map(photo -> modelMapper.map(photo, PhotoResponse.class));
    }

    /**
     * Lấy ảnh theo hashtag
     */
    public Page<PhotoResponse> getPhotosByTag(String tagName, Pageable pageable) {
        // Tìm tag theo tên
        Tag tag = tagRepository.findByName(tagName)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hashtag"));

        // Lấy danh sách photo ID có tag này
        List<String> photoIds = photoTagRepository.findPhotoIdsByTagId(tag.getId());

        // Lấy danh sách photos
        Page<Photo> photos = photoRepository.findByIdInOrderByCreatedAtDesc(photoIds, pageable);

        return photos.map(photo -> modelMapper.map(photo, PhotoResponse.class));
    }

    /**
     * Xử lý hashtags từ caption và lưu vào DB
     */
    private void processHashtags(String photoId, String caption) {
        if (caption == null || caption.isEmpty()) {
            return;
        }

        // Tìm tất cả hashtags
        Set<String> hashtags = extractHashtags(caption);

        for (String hashtag : hashtags) {
            // Tìm hoặc tạo mới tag
            Tag tag = tagRepository.findByName(hashtag)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(hashtag);
                        newTag.setCreatedAt(Instant.now());
                        return tagRepository.save(newTag);
                    });

            // Tạo liên kết giữa photo và tag
            PhotoTag photoTag = new PhotoTag();
            photoTag.setPhotoId(photoId);
            photoTag.setTagId(tag.getId());
            photoTag.setCreatedAt(Instant.now());

            photoTagRepository.save(photoTag);
        }
    }

    /**
     * Trích xuất hashtags từ text
     */
    private Set<String> extractHashtags(String text) {
        Set<String> hashtags = new HashSet<>();
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            hashtags.add(matcher.group(1).toLowerCase());
        }

        return hashtags;
    }

    /**
     * Trích xuất public ID từ URL Cloudinary
     */
    private String extractPublicIdFromUrl(String url) {
        if (url == null || !url.contains("cloudinary.com")) {
            return null;
        }

        try {
            // Format của URL Cloudinary: https://res.cloudinary.com/[cloud_name]/image/upload/v[version]/[public_id].[ext]
            String path = url.substring(url.indexOf("/upload/") + 8);
            int lastDotIndex = path.lastIndexOf(".");

            if (lastDotIndex > 0) {
                return path.substring(0, lastDotIndex);
            }

            return path;
        } catch (Exception e) {
            log.error("Lỗi khi trích xuất public ID từ URL: {}", url, e);
            return null;
        }
    }
}
