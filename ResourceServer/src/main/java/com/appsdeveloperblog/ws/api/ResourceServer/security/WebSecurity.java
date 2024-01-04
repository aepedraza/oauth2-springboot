package com.appsdeveloperblog.ws.api.ResourceServer.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.oauth2.core.authorization.OAuth2AuthorizationManagers.hasScope;

@Configuration
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

        return http.build();
    }
}
