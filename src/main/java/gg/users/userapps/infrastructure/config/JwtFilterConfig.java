package gg.users.userapps.infrastructure.config;

import gg.users.userapps.domain.ports.out.JwtServicePort;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilterConfig extends OncePerRequestFilter {
    private final JwtServicePort jwtServicePort;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try{
            Claims claims = jwtServicePort.validateToken(token);

            if (claims == null) {
                this.tokenIsInvalid(response, "inválid/expired token");
                return;
            }

            if(SecurityContextHolder.getContext().getAuthentication() == null){
                String username = claims.getSubject();

                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);

                Collection<SimpleGrantedAuthority> permisos = roles != null
                        ? roles.stream().map(SimpleGrantedAuthority::new).toList()
                        : List.of();

                var auth = new UsernamePasswordAuthenticationToken(username, token, permisos);

                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (Exception e) {
            this.tokenIsInvalid(response, "Error validating token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void tokenIsInvalid(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();

        var errorString = """
                {
                    "error":"%s"
                }
                """.formatted(message);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(errorString);
    }
}
