
// UserDetailsService für Header-Authentifizierung
package com.example.security.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository; // Deine User Repository

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Lade User aus deiner Datenbank oder externem Service
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        if (user.getAuthMethod() == null) {
            user.setAuthMethod("HEADER");
        }
        userRepository.save(user);
        
        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password("") // Kein Passwort nötig bei Header-Auth
            .authorities(mapToAuthorities(user.getRoles()))
            
            // TODO .accountNonExpired(true)
            // TODO .accountNonLocked(true)
            // TODO .credentialsNonExpired(true)
            // TODO .enabled(user.isEnabled())
            .build();
    }
    
    private Collection<? extends GrantedAuthority> mapToAuthorities(java.util.Set<User.Role> roles) {
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
            .collect(Collectors.toList());
    }
}