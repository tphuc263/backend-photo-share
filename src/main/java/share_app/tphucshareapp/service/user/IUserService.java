package share_app.tphucshareapp.service.user;

import org.springframework.data.domain.Page;
import share_app.tphucshareapp.dto.request.user.UpdateProfileRequest;
import share_app.tphucshareapp.dto.response.user.UserProfileResponse;

public interface IUserService {
    UserProfileResponse getUserProfileById(String userId);

    UserProfileResponse getCurrentUserProfile();

    UserProfileResponse updateProfile(UpdateProfileRequest request);

    Page<UserProfileResponse> getAllUsers(int page, int size);
}
