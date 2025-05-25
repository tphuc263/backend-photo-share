package share_app.tphucshareapp.dto.response.search;

import lombok.Data;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.dto.response.user.UserProfileResponse;
import share_app.tphucshareapp.model.Tag;

import java.util.List;

@Data
public class SearchResultResponse {
    private List<UserProfileResponse> users;
    private List<PhotoResponse> photos;
    private List<Tag> tags;
    private long totalUsers;
    private long totalPhotos;
    private long totalTags;
    private String query;
}
