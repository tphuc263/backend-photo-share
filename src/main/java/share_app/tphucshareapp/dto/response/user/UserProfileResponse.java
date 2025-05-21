package share_app.tphucshareapp.dto.response.user;

import lombok.Data;

@Data
public class UserProfileResponse {
    private String id;
    private String username;
    private String email;
    private String imageUrl;
    private String firstName;
    private String lastName;
    private String bio;
    private String role;
}
