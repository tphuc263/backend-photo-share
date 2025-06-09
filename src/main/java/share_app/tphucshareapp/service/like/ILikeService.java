package share_app.tphucshareapp.service.like;

import share_app.tphucshareapp.dto.response.like.LikeResponse;

import java.util.List;

public interface ILikeService {
    void like(String photoId);

    void unlike(String photoId);

    List<LikeResponse> getPhotoLikes(String photoId);

    long getPhotoLikesCount(String photoId);

    boolean isPhotoLikedByUser(String photoId, String userId);
}
