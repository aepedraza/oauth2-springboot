package com.appsdeveloperblog.ws.api.ResourceServer.security;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableMethodSecurity(securedEnabled = true) // Already includes @Configuration
@EnableWebSecurity // May not be necessary, check Spring Documentation
public class WebSecurity {

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());

        // Configure HTTP Security
        http.authorizeHttpRequests(auth ->
                        // Indicates that GET /users must have a profile scope of access (checks JWT.scope)
                        // "SCOPE" is a must when Spring Security creates a list of authorities based on scopes
                        // appends "SCOPE_" when need to check scope information in JWT
                        // Not needed when JWT acquired initially in HTTP requests
                        auth.requestMatchers(HttpMethod.GET, "/users/**")
                                //.access(hasScope("profile"))
                                .hasRole("developer") // .hasAnyRole(String...) also available
                                .anyRequest().authenticated())
                // Indicates that the app is an OAuth2.0 auth server and expects JWT
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        // CORS configuration example (if direct communication with ResourceServer is needed)
        // If you use Spring MVC’s CORS support, you can omit specifying the CorsConfigurationSource and
        // Spring Security uses the CORS configuration provided to Spring MVC
//        http.cors(Customizer.withDefaults())
//                .authorizeHttpRequests(auth ->
//                        auth.requestMatchers(HttpMethod.GET, "users/**")
//                                .hasRole("developer")
//                                .anyRequest().authenticated())
//                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }

    /**
     * Integrates {@link org.springframework.web.filter.CorsFilter} with Spring security
     * to ensure CORS is handled first and pre-flight request not rejected
     *
     * @return CORS configuration source
     * @see <a href="https://docs.spring.io/spring-security/reference/6.2/servlet/integrations/cors.html#page-title">Spring Security documentation - CORS configuration</a>
     */
//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration corsConfiguration = new CorsConfiguration();
//        corsConfiguration.setAllowedOrigins(List.of("*"));
//        corsConfiguration.setAllowedMethods(List.of("GET", "POST"));
//        corsConfiguration.setAllowedHeaders(List.of("*"));
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", corsConfiguration);
//
//        return source;
//    }
}
