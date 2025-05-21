package share_app.tphucshareapp.service.auth;

import share_app.tphucshareapp.dto.request.auth.LoginRequest;
import share_app.tphucshareapp.dto.response.auth.LoginResponse;
import share_app.tphucshareapp.dto.request.auth.RegisterRequest;

public interface IAuthService {
    LoginResponse login(LoginRequest request);
    void register(RegisterRequest request);
}
