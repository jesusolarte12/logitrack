package com.proyecto.logitrack.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable());
        
        // Habilitar CORS
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // Rutas públicas y rutas protegidas
        http.authorizeHttpRequests(auth -> auth

        // Recursos estáticos y páginas públicas
        .requestMatchers(
            "/",
            "/templates/**",
            "/css/**",
            "/js/**"
        ).permitAll()

        // Endpoints de autenticacion
        .requestMatchers("/auth/login").permitAll()
        .requestMatchers("/auth/register").hasRole("ADMIN")
        .requestMatchers("/auth/validate", "/auth/userinfo").authenticated()


        // Documentación
        .requestMatchers("/docs").permitAll()
        .requestMatchers("/v3/api-docs/**").permitAll()
        .requestMatchers("/swagger-ui/**").permitAll()

        // Permitir endpoint de bodega desde cualquier lugar TEST
        .requestMatchers("/api/bodega/info").permitAll()

        //Permitir endpoint de inventario detalle desde cualquier lugar TEST
        .requestMatchers("/api/inventario/detalle/**").permitAll()

        // API protegida
        .requestMatchers("/api/**").authenticated()

        // Todo lo demás, también protegido
        .anyRequest().authenticated()
    );


        // Seguridad sin sesiones (modo JWT)
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // Permitir iframes desde el mismo origen
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        // Filtro JWT antes del filtro estándar
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Password encoder para contraseñas encriptadas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager para que AuthController pueda autenticar
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
