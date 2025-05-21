package share_app.tphucshareapp.service.photo;

import share_app.tphucshareapp.dto.request.photo.CreatePhotoRequest;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;

import java.io.IOException;

public interface IPhotoService {
    PhotoResponse createPhoto(CreatePhotoRequest request);
}
