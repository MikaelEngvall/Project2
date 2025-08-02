package se.duggals.dfrm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import se.duggals.dfrm.security.JwtAuthenticationFilter;
import se.duggals.dfrm.security.JwtAuthenticationEntryPoint;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                         JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(authz -> authz
                // Publika endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/actuator/health").permitAll()
                .requestMatchers("/api/actuator/info").permitAll()
                .requestMatchers("/error").permitAll()
                
                // API endpoints - kräver autentisering
                .requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN", "SUPERADMIN")
                .requestMatchers("/api/apartments/**").hasAnyRole("USER", "ADMIN", "SUPERADMIN")
                .requestMatchers("/api/tenants/**").hasAnyRole("USER", "ADMIN", "SUPERADMIN")
                .requestMatchers("/api/interests/**").hasAnyRole("USER", "ADMIN", "SUPERADMIN")
                .requestMatchers("/api/issues/**").hasAnyRole("USER", "ADMIN", "SUPERADMIN")
                .requestMatchers("/api/tasks/**").hasAnyRole("USER", "ADMIN", "SUPERADMIN")
                
                // Admin endpoints
                .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPERADMIN")
                .requestMatchers("/api/actuator/**").hasAnyRole("ADMIN", "SUPERADMIN")
                
                // Superadmin endpoints
                .requestMatchers("/api/superadmin/**").hasRole("SUPERADMIN")
                
                // Alla andra requests kräver autentisering
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) throws Exception {
        var authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authManagerBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
        return authManagerBuilder.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Tillåtna origins
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:3000",
            "http://localhost:3001", 
            "https://dfrm.duggalsfastigheter.se",
            "https://*.duggalsfastigheter.se"
        ));
        
        // Tillåtna metoder
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Tillåtna headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", 
            "Accept", "Origin", "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Exponera headers
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        
        // Tillåt credentials
        configuration.setAllowCredentials(true);
        
        // Max ålder för preflight requests
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
} 