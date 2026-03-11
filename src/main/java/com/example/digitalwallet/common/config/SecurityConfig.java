package com.example.digitalwallet.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
// WHY @Configuration?
// Tells Spring: this class contains @Bean definitions.
// Spring reads it at startup and registers all beans.
// Different from @Component — specifically for config classes.

@EnableWebSecurity
// WHY @EnableWebSecurity?
// Activates Spring Security's web security support.
// Imports all necessary Spring Security infrastructure.
// Without it, your SecurityFilterChain bean is ignored.

@EnableMethodSecurity
// WHY @EnableMethodSecurity?
// Enables method-level security annotations:
// @PreAuthorize("hasRole('ADMIN')") on controller methods
// Without this, those annotations do nothing silently.
// Phase 5 when we add roles — this is already ready.

@Slf4j
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        // Constructor injection — Spring provides JwtAuthFilter bean.
        // JwtAuthFilter was marked @Component — Spring already has it.
    }

    // ================================================================
    // PUBLIC ROUTES — no token required
    // ================================================================

    // WHY a separate constant array?
    // Public routes are referenced in SecurityFilterChain.
    // Keeping them here makes it obvious what's public.
    // Easy to add new public routes in one place.
    private static final String[] PUBLIC_ROUTES = {
            "/api/v1/auth/**",
            // WHY **?
            // Matches any path after /auth/
            // /auth/login, /auth/register, /auth/refresh
            // all covered by one pattern

            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            // Swagger documentation routes
            // Developers need these without authenticating.
            // Remove in production if API docs should be private.
    };

    // ================================================================
    // CORE SECURITY CONFIGURATION
    // ================================================================

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // CSRF — disable for JWT stateless API
                .csrf(AbstractHttpConfigurer::disable)
                // WHY AbstractHttpConfigurer::disable?
                // Method reference syntax for:
                // csrf -> csrf.disable()
                // Modern Spring Security 6.x style.
                // CSRF irrelevant for JWT as explained above.

                // SESSION — stateless, no server-side sessions
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                // WHY STATELESS?
                // Every request authenticated via JWT alone.
                // Server creates NO sessions.
                // No memory wasted on session storage.
                // Scales horizontally — any server handles any request.

                // ROUTE AUTHORIZATION RULES
                .authorizeHttpRequests(auth -> auth

                                // Public routes — no token needed
                                .requestMatchers(PUBLIC_ROUTES)
                                .permitAll()
                                // permitAll() = allow everyone, authenticated or not

                                // All other routes — must be authenticated
                                .anyRequest()
                                .authenticated()
                        // authenticated() = must have valid authentication
                        // in SecurityContextHolder (set by JwtAuthFilter)
                        // If not authenticated → 401 Unauthorized
                )

                // REGISTER OUR JWT FILTER
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );
        // WHY addFilterBefore?
        // We insert JwtAuthFilter BEFORE
        // UsernamePasswordAuthenticationFilter in the chain.
        // WHY before that specific filter?
        // UsernamePasswordAuthenticationFilter handles
        // form-based login (username/password in form).
        // We don't use form login — we use JWT.
        // Our filter must run first to set authentication
        // before Spring's default filters check for it.
        //
        // Filter chain order after our addition:
        // ... → JwtAuthFilter → UsernamePasswordAuthFilter → ...
        //        (ours)          (Spring's, effectively skipped)

        return http.build();
    }

    // ================================================================
    // PASSWORD ENCODER BEAN
    // ================================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        // WHY BCrypt specifically?
        // ─────────────────────────────
        // 1. ADAPTIVE COST FACTOR
        //    BCrypt has a "work factor" (default 10).
        //    Each increment doubles computation time.
        //    As hardware gets faster, increase work factor.
        //    Keeps hashing slow against brute force forever.
        //
        // 2. BUILT-IN SALT
        //    Salt = random bytes added before hashing.
        //    "password" + salt1 → hash1
        //    "password" + salt2 → hash2
        //    Same password → different hashes every time.
        //    Prevents rainbow table attacks.
        //    BCrypt generates and stores salt automatically.
        //    You never manage salt manually.
        //
        // 3. INDUSTRY STANDARD
        //    Used by Spring Security by default.
        //    Battle-tested for 25+ years.
        //    OWASP recommended.
        //
        // BCrypt hash looks like:
        // $2a$10$N9qo8uLOickgx2ZMRZoMye...
        //  ↑  ↑  ↑
        //  │  │  └── salt (22 chars) + hash
        //  │  └───── work factor (10 = 2^10 iterations)
        //  └──────── BCrypt version
    }

    // ================================================================
    // AUTHENTICATION MANAGER BEAN
    // ================================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
        // WHY AuthenticationManager?
        // Spring's core authentication coordinator.
        // UserServiceImpl uses this during login to:
        // 1. Take email + password
        // 2. Find user by email
        // 3. Verify password with BCrypt
        // 4. Return authenticated user or throw exception
        //
        // We don't implement authentication logic manually.
        // AuthenticationManager orchestrates it using:
        // - UserDetailsService (finds user by email)
        // - PasswordEncoder (verifies password)
        // Together they handle login verification cleanly.
    }
}
