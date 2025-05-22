package share_app.tphucshareapp.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.request.user.UpdateProfileRequest;
import share_app.tphucshareapp.dto.response.user.UserProfileResponse;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.UserRepository;
import share_app.tphucshareapp.security.userdetails.AppUserDetails;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserProfileResponse getUserProfileById(String userId) {
        User user = findUserById(userId);
        return modelMapper.map(user, UserProfileResponse.class);
    }

    @Override
    public UserProfileResponse getCurrentUserProfile() {
        User user = getCurrentUser();
        return modelMapper.map(user, UserProfileResponse.class);
    }

    @Override
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();
        updateUserFields(user, request);
        User updatedUser = userRepository.save(user);
        log.info("User profile updated successfully for user ID: {}", updatedUser.getId());
        return modelMapper.map(updatedUser, UserProfileResponse.class);
    }

    // helper methods
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isUserAuthenticated(authentication)) {
            log.warn("User not authenticated properly. Authentication: {}",
                    authentication != null ? authentication.getClass().getSimpleName() : "null");
            throw new RuntimeException("User not authenticated properly");
        }
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        return findUserById(userDetails.getId());
    }

    private boolean isUserAuthenticated(Authentication authentication) {
        return authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof AppUserDetails;
    }

    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new RuntimeException("User not found with ID: " + userId);
                });
    }

    private void updateUserFields(User user, UpdateProfileRequest request) {
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
    }
}
