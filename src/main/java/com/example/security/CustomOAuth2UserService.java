package com.example.security;

import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    private final OidcUserService oidcUserService = new OidcUserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User;
        
        // Check if this is an OIDC request
        if (userRequest instanceof OidcUserRequest oidcUserRequest) {
            oauth2User = oidcUserService.loadUser(oidcUserRequest);
        } else {
            oauth2User = super.loadUser(userRequest);
        }
        
        return processOAuth2User(userRequest, oauth2User);
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        // Extract user information
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String preferredUsername = oauth2User.getAttribute("preferred_username");
        String givenName = oauth2User.getAttribute("given_name");
        String familyName = oauth2User.getAttribute("family_name");
        
        // Determine username
        String username = preferredUsername != null ? preferredUsername : 
                         (name != null ? name : email);
        
        // Create or update user in database
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> userRepository.findByUsername(username)
                .orElseGet(() -> createNewUser(username, email, givenName, familyName)));
        
        // Update login info
        user.setLastLogin(LocalDateTime.now());
        String authMethod = "OAUTH2_" + userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        if (user.getAuthMethod() == null || !user.getAuthMethod().startsWith("OAUTH2")) {
            user.setAuthMethod(authMethod);
        }
        userRepository.save(user);
        
        return new CustomOAuth2User(oauth2User, user);
    }
    
    private User createNewUser(String username, String email, String firstName, String lastName) {
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setFirstName(firstName != null ? firstName : "Unknown");
        newUser.setLastName(lastName != null ? lastName : "User");
        newUser.setRoles(Set.of(User.Role.USER)); // Default Role
        newUser.setAuthMethod("OAUTH2");
        return userRepository.save(newUser);
    }
}