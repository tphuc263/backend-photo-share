package share_app.tphucshareapp.security.userdetails;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DisabledException {
        // Tìm user theo username hoặc email
        User user = userRepository.findByUsername(username);

        if (user == null) {
            user = userRepository.findByEmail(username);
        }

        if (user == null) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng: " + username);
        }

        return AppUserDetails.buildUserDetails(user);
    }
}
