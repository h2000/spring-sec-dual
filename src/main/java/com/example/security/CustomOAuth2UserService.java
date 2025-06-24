package com.example.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        // Verarbeite OAuth2 User und erstelle/update lokalen User
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String preferredUsername = oauth2User.getAttribute("preferred_username");
        
        // Verwende preferred_username oder name als Username
        String username = preferredUsername != null ? preferredUsername : 
                         (name != null ? name : email);
        
        // Erstelle oder update User in deiner DB
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> userRepository.findByUsername(username)
                .orElseGet(() -> createNewUser(username, email, name)));
        
        // Update login info
        user.setLastLogin(LocalDateTime.now());
        if (user.getAuthMethod() == null || !user.getAuthMethod().startsWith("OAUTH2")) {
            user.setAuthMethod("OAUTH2_" + userRequest.getClientRegistration().getRegistrationId().toUpperCase());
        }
        userRepository.save(user);
        
        return new CustomOAuth2User(oauth2User, user);
    }
    
    private User createNewUser(String username, String email, String name) {
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        
        if (name != null && name.contains(" ")) {
            String[] parts = name.split(" ", 2);
            newUser.setFirstName(parts[0]);
            newUser.setLastName(parts[1]);
        } else {
            newUser.setFirstName(name);
        }
        
        newUser.setRoles(Set.of(User.Role.USER)); // Default Role
        newUser.setAuthMethod("OAUTH2");
        return userRepository.save(newUser);
    }
}
