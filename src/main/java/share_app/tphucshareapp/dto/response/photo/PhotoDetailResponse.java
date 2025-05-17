package share_app.tphucshareapp.dto.response.photo;

import lombok.Data;
import share_app.tphucshareapp.dto.request.user.UserBasicInfo;
import share_app.tphucshareapp.dto.response.comment.CommentResponse;

import java.time.Instant;
import java.util.List;

@Data
public class PhotoDetailResponse {

    private String id;
    private UserBasicInfo user; // Thông tin cơ bản về người đăng
    private String imageURL;
    private String caption;
    private long likeCount;
    private long commentCount;
    private boolean isLiked; // Người dùng hiện tại đã like hay chưa
    private List<String> tags; // Danh sách hashtags
    private List<CommentResponse> recentComments; // Các comment gần đây
    private Instant createdAt;
}
