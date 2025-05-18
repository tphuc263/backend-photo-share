package share_app.tphucshareapp.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import share_app.tphucshareapp.dto.request.auth.LoginRequest;
import share_app.tphucshareapp.dto.request.auth.LoginResponse;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.security.jwt.JwtUtils;
import share_app.tphucshareapp.security.userdetails.AppUserDetails;

@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateAccessToken(authentication);

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        LoginResponse loginResponse = new LoginResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .toList()
        );

        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Đăng nhập thành công"));
    }
}