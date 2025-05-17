package share_app.tphucshareapp.security.userdetails;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import share_app.tphucshareapp.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class AppUserDetails implements UserDetails, OAuth2User {

    private final Long id;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    private Map<String, Object> attributes;

    public AppUserDetails(Long id, String email, String password, boolean enabled, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
        this.attributes = attributes;
    }

    // Factory method cho đăng nhập thông thường (từ User entity)
    public static AppUserDetails buildUserDetails(User user) {
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(user.getRole().getName().name()));
        // Truyền trạng thái active của User vào constructor
        return new AppUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.isActive(), // Sử dụng trạng thái active từ User
                authorities,
                null // Không có attributes OAuth2 trong trường hợp này
        );
    }

    // Factory method cho đăng nhập OAuth2 (từ User entity và attributes)
    public static AppUserDetails buildOAuth2UserDetails(User user, Map<String, Object> attributes) {
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(user.getRole().getName().name()));
        return new AppUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.isActive(),
                authorities,
                attributes
        );
    }

    @Override
    public String getUsername() {
        return email;
    }
}
