package com.example.security;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class CustomOAuth2User implements OidcUser {
    
    private final OAuth2User oauth2User;
    private final User user;
    private final Collection<GrantedAuthority> authorities;

    public CustomOAuth2User(OAuth2User oauth2User, User user) {
        this.oauth2User = oauth2User;
        this.user = user;
        
        // Convert user roles to Spring Security authorities
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public Map<String, Object> getClaims() {
        if (oauth2User instanceof OidcUser oidcUser) {
            return oidcUser.getClaims();
        }
        return oauth2User.getAttributes();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        if (oauth2User instanceof OidcUser oidcUser) {
            return oidcUser.getUserInfo();
        }
        return null;
    }

    @Override
    public OidcIdToken getIdToken() {
        if (oauth2User instanceof OidcUser oidcUser) {
            return oidcUser.getIdToken();
        }
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    // Getter für den lokalen User
    public User getUser() {
        return user;
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getFullName() {
        return user.getFirstName() + " " + user.getLastName();
    }
}