package share_app.tphucshareapp.service.photo;

import org.springframework.data.domain.Page;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;

public interface INewsfeedService {
    Page<PhotoResponse> getNewsfeed(int page, int size);

    Page<PhotoResponse> getRecommendations(int page, int size);

    Page<PhotoResponse> getTrendingPhotos(int page, int size);
}
