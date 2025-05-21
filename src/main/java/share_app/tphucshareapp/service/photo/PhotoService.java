package share_app.tphucshareapp.service.photo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.request.photo.CreatePhotoRequest;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.PhotoRepository;
import share_app.tphucshareapp.service.user.UserService;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoService implements IPhotoService {
    private final CloudinaryService cloudinaryService;
    private final PhotoRepository photoRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;


    @Override
    public PhotoResponse createPhoto(CreatePhotoRequest request) {
        Map uploadResult = cloudinaryService.uploadImage(request.getImage());
        String imageUrl = (String) uploadResult.get("secure_url");

        // Create photo entity
        User currentUser = userService.getCurrentUser();
        Photo photo = new Photo();
        photo.setUserId(currentUser.getId());
        photo.setImageURL(imageUrl);
        photo.setCaption(request.getCaption());
        photo.setCreatedAt(Instant.now());
        // Save photo to database
        Photo savedPhoto = photoRepository.save(photo);

        return modelMapper.map(savedPhoto, PhotoResponse.class);
    }
}
