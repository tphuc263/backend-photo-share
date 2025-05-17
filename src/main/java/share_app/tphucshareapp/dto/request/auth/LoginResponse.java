package share_app.tphucshareapp.dto.request.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String id;
    private String username;
    private String email;
    private List<String> roles;
}
