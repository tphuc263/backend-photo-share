package share_app.tphucshareapp.service.user;

import share_app.tphucshareapp.dto.request.user.UpdateProfileRequest;
import share_app.tphucshareapp.dto.response.user.UserProfileResponse;
import share_app.tphucshareapp.model.User;

public interface IUserService {
    UserProfileResponse getUserProfileById(String userId);
    UserProfileResponse getCurrentUserProfile();
    UserProfileResponse updateProfile(UpdateProfileRequest request);
}
