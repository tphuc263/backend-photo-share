package share_app.tphucshareapp.service.photo;

import org.springframework.data.domain.Page;
import share_app.tphucshareapp.dto.request.photo.CreatePhotoRequest;
import share_app.tphucshareapp.dto.response.photo.PhotoDetailResponse;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;

public interface IPhotoService {
    PhotoResponse createPhoto(CreatePhotoRequest request);

    Page<PhotoResponse> getAllPhotos(int page, int size);

    Page<PhotoResponse> getPhotosByUserId(String userId, int page, int size);

    PhotoDetailResponse getPhotoById(String photoId);

    void deletePhoto(String photoId);
}
