package share_app.tphucshareapp.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import share_app.tphucshareapp.security.userdetails.AppUserDetailsService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final AppUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);

            if (log.isDebugEnabled()) {
                log.debug("Processing request: {} {} with Content-Type: {}",
                        request.getMethod(), request.getRequestURI(), request.getContentType());
                log.debug("JWT token present: {}", jwt != null);
            }

            if (StringUtils.hasText(jwt) && jwtUtils.validateToken(jwt)) {
                authenticateUser(jwt);
            } else if (log.isDebugEnabled()) {
                log.debug("JWT token invalid or not present, authentication will be anonymous");
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication for request: {} {}",
                    request.getMethod(), request.getRequestURI(), e);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(String jwt) {
        try {
            String username = jwtUtils.getEmailFromToken(jwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            var authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            if (log.isDebugEnabled()) {
                log.debug("Authentication set successfully for user: {}", username);
            }
        } catch (Exception e) {
            log.error("Failed to authenticate user from JWT token", e);
            throw e;
        }
    }

    public String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
