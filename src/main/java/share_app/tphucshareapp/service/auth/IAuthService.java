package share_app.tphucshareapp.service.auth;

import share_app.tphucshareapp.dto.request.auth.LoginRequest;
import share_app.tphucshareapp.dto.request.auth.RegisterRequest;
import share_app.tphucshareapp.dto.response.auth.LoginResponse;

public interface IAuthService {
    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);
}
