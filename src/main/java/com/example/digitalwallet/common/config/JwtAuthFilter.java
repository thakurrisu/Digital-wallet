package com.example.digitalwallet.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private JwtService jwtService;

    private RedisService redisService;

    public JwtAuthFilter(JwtService jwtService ,  RedisService redisService) {

        this.jwtService = jwtService;
        this.redisService = redisService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // STEP 1: Try to extract token from request
        String jwt = extractTokenFromRequest(request);

        // STEP 2: If no token found, just move on.
        // Don't reject here — SecurityConfig handles that.
        // WHY? Because public routes (/auth/login, /auth/register)
        // have no token. We don't want to reject them here.
        // We simply don't authenticate. SecurityConfig
        // allows public routes even without authentication.
        if (jwt == null) {
            log.debug("No JWT token found in request to: {}",
                    request.getRequestURI());
            filterChain.doFilter(request, response);
            // filterChain.doFilter() = "pass this request
            // to the next filter in the chain"
            // Without this call → request is STUCK here forever.
            // No response sent. Client waits forever. Never forget this.
            return;
            // WHY return after doFilter()?
            // Once we call doFilter, execution continues in other filters.
            // When those finish and response comes BACK through the chain,
            // execution resumes HERE after doFilter().
            // return prevents executing code below
            // after the request has already been handled.
        }

        // STEP 3: Validate the token
        if (!jwtService.isTokenValid(jwt)) {
            // Token exists but is invalid/expired.
            // Again — don't reject here.
            // Just don't authenticate.
            // SecurityConfig rejects unauthenticated requests
            // to protected routes.
            log.warn("Invalid JWT token for request to: {}",
                    request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        //check if token is blacklisted
        if(isTokenBlacklisted(jwt)){
            log.warn("Blacklisted JWT token for request to: {}",
                    request.getRequestURI());
           filterChain.doFilter(request, response);
           return;
        }

        // STEP 4: Token is valid — extract userId
        UUID userId = jwtService.extractUserId(jwt);
        String email = jwtService.extractEmail(jwt);

        log.debug("Valid JWT for userId: {}, accessing: {}",
                userId, request.getRequestURI());

        // STEP 5: Check if already authenticated
        // WHY this check?
        // SecurityContextHolder might already have authentication
        // if another filter ran before ours.
        // We don't want to overwrite valid existing authentication.
        if (SecurityContextHolder.getContext()
                .getAuthentication() == null) {

            // STEP 6: Create Authentication object
            // This is what goes INTO SecurityContextHolder.
            // It represents "who is making this request".
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            // WHY userId as principal (first param)?
                            // Principal = the identity of the authenticated user.
                            // We use UUID because:
                            // - Immutable identifier
                            // - Services need userId to query DB
                            // - Any controller can get userId from SecurityContext

                            null,
                            // WHY null as credentials (second param)?
                            // Credentials = password.
                            // We already validated via JWT — no password needed.
                            // Best practice: null credentials after authentication.
                            // Never keep passwords in memory longer than needed.

                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                            // WHY authorities (third param)?
                            // Authorities = what this user is ALLOWED to do.
                            // "ROLE_USER" = standard user role.
                            // Spring Security uses this for @PreAuthorize checks.
                            // e.g @PreAuthorize("hasRole('ADMIN')")
                            // Phase 5: We'll add proper role management.
                            // For now every authenticated user is ROLE_USER.
                    );

            // STEP 7: Add request details to authentication
            authToken.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );
            // WHY setDetails?
            // Attaches HTTP request details to authentication:
            // - IP address of client
            // - Session ID
            // Useful for security auditing:
            // "User abc-123 logged in from IP 192.168.1.1"
            // Spring Security audit logs use this automatically.

            // STEP 8: THE CRITICAL STEP
            // Write authentication to SecurityContextHolder.
            // This is what tells Spring Security:
            // "This request is authenticated. I know who it is."
            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authToken);
            // After this line:
            // - Request is marked as authenticated
            // - Any controller can get userId via SecurityContextHolder
            // - Spring Security allows access to protected routes
            // - @PreAuthorize annotations work correctly
        }

        // STEP 9: Pass to next filter / controller
        filterChain.doFilter(request, response);
        // WHY here and not in the if block?
        // We ALWAYS pass forward — authenticated or not.
        // We just may or may not have set authentication.
        // SecurityConfig decides what happens next based on
        // whether authentication is set or not.
    }

    private String extractTokenFromRequest(HttpServletRequest request) {

        // Get the Authorization header
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) &&
                bearerToken.startsWith("Bearer ")) {

            return bearerToken.substring(7);
        }

        return null;
    }
    private Boolean isTokenBlacklisted(String token) {
        try{
            return redisService.isTokenBlacklisted(token);
        } catch(Exception ex){
            log.error("CRITICAL: Redis unavailable for "
                    + "blacklist check. Failing open." ,ex.getMessage());
            return false;
        }

    }

}


