package share_app.tphucshareapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import share_app.tphucshareapp.dto.request.user.UserProfileRequest;
import share_app.tphucshareapp.dto.response.user.UserDetailResponse;
import share_app.tphucshareapp.dto.response.user.UserStatsResponse;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.FollowRepository;
import share_app.tphucshareapp.repository.PhotoRepository;
import share_app.tphucshareapp.repository.UserRepository;
import share_app.tphucshareapp.security.userdetails.AppUserDetails;
import share_app.tphucshareapp.service.image.CloudinaryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PhotoRepository photoRepository;
    private final FollowRepository followRepository;
    private final ModelMapper modelMapper;
    private final CloudinaryService cloudinaryService;

    /**
     * Lấy thông tin chi tiết người dùng
     */
    public UserDetailResponse getUserDetail(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        UserDetailResponse response = modelMapper.map(user, UserDetailResponse.class);

        // Kiểm tra người dùng hiện tại có đang follow người này không
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AppUserDetails) {
            AppUserDetails currentUser = (AppUserDetails) authentication.getPrincipal();
            String currentUserId = currentUser.getId();

            if (!currentUserId.equals(userId)) {
                boolean isFollowing = followRepository.findByFollowerIdAndFollowingId(currentUserId, userId).isPresent();
                response.setFollowing(isFollowing);
            }
        }

        return response;
    }

    /**
     * Cập nhật thông tin cá nhân
     */
    @Transactional
    public UserDetailResponse updateProfile(String userId, UserProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Cập nhật thông tin
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setBio(request.getBio());

        // Lưu vào DB
        user = userRepository.save(user);

        return modelMapper.map(user, UserDetailResponse.class);
    }

    /**
     * Cập nhật ảnh đại diện
     */
    @Transactional
    public String updateAvatar(String userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        try {
            // Xóa ảnh cũ nếu có
            if (user.getImageUrl() != null && user.getImageUrl().contains("cloudinary.com")) {
                String publicId = extractPublicIdFromUrl(user.getImageUrl());
                if (publicId != null) {
                    try {
                        cloudinaryService.deleteImage(publicId);
                    } catch (IOException e) {
                        log.error("Lỗi khi xóa ảnh đại diện cũ: ", e);
                        // Tiếp tục cập nhật ảnh mới ngay cả khi xóa ảnh cũ thất bại
                    }
                }
            }

            // Tải ảnh mới lên Cloudinary
            String imageUrl = cloudinaryService.uploadImage(file);

            // Cập nhật URL ảnh đại diện
            user.setImageUrl(imageUrl);
            userRepository.save(user);

            return imageUrl;
        } catch (IOException e) {
            log.error("Lỗi khi tải ảnh đại diện lên: ", e);
            throw new RuntimeException("Không thể tải ảnh đại diện lên. Vui lòng thử lại sau.");
        }
    }

    /**
     * Lấy thống kê người dùng
     */
    public UserStatsResponse getUserStats(String userId) {
        // Đếm số bài đăng
        long postCount = photoRepository.countByUserId(userId);

        // Đếm số người theo dõi
        long followerCount = followRepository.countFollowersByFollowingId(userId);

        // Đếm số người đang theo dõi
        long followingCount = followRepository.countFollowingByFollowerId(userId);

        return new UserStatsResponse(postCount, followerCount, followingCount);
    }

    /**
     * Tìm kiếm người dùng theo tên
     */
    public List<UserDetailResponse> searchUsers(String query) {
        List<User> users = userRepository.findByUsernameContainingOrFirstNameContainingOrLastNameContaining(
                query, query, query);

        return users.stream()
                .map(user -> modelMapper.map(user, UserDetailResponse.class))
                .collect(Collectors.toList());
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
