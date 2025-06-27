
package com.example.security;

import com.example.security.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;

@Configuration
public class VaadinSecurityConfig extends VaadinWebSecurity {

    @Autowired
    private HeaderAuthenticationFilter headerAuthenticationFilter;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Configure specific endpoints BEFORE calling super.configure()
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
            .requestMatchers("/api/**").authenticated()
            .requestMatchers("/dashboard").authenticated()
        );

        // Apply Vaadin security defaults
        super.configure(http);

        // Set the login view
        setLoginView(http, LoginView.class);

        // Add header authentication filter before OAuth2
        http.addFilterBefore(headerAuthenticationFilter, OAuth2LoginAuthenticationFilter.class);

        // Configure OAuth2 login
        http.oauth2Login(oauth2 -> oauth2
            .loginPage("/login")
            .defaultSuccessUrl("/dashboard", true)
            .failureUrl("/login?error")
            .userInfoEndpoint(userInfo -> userInfo
                .userService(customOAuth2UserService)
                .oidcUserService(oidcUserService())
            )
        );

        // Configure logout
        http.logout(logout -> logout
            .logoutSuccessUrl("/login?logout")
            .invalidateHttpSession(true)
            .clearAuthentication(true)
        );
    }

    @Bean
    public OidcUserService oidcUserService() {
        return new OidcUserService();
    }
}