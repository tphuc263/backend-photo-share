package share_app.tphucshareapp.controller;


import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import share_app.tphucshareapp.dto.request.auth.RegisterRequest;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.security.jwt.JwtUtils;
import share_app.tphucshareapp.service.auth.AuthService;
import share_app.tphucshareapp.utils.CookieUtils;

@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final CookieUtils cookieUtils;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerUser(@Valid @RequestBody RegisterRequest request) {
        authService.registerUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Đăng ký thành công. Vui lòng kiểm tra email để kích hoạt tài khoản"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        cookieUtils.deleteRefreshTokenCookie(response);
        return ResponseEntity.ok(ApiResponse.success(null, "Đăng xuất thành công"));
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyAccount(@RequestParam("token") String token) {
        boolean verified = authService.verifyAccount(token);

        if (verified) {
            return ResponseEntity.ok(ApiResponse.success(null, "Xác thực tài khoản thành công"));
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Token không hợp lệ hoặc đã hết hạn"));
        }
    }
}