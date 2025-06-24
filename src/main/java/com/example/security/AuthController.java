package com.example.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @GetMapping("/login2")
    public String login(HttpServletRequest request, Model model) {
        // Prüfe ob Header-Authentication möglich ist
        String headerUser = request.getHeader("X_AUTH_USER");
        
        if (headerUser != null && !headerUser.trim().isEmpty()) {
            // User ist bereits via Header authentifiziert - redirect zu Dashboard
            return "redirect:/dashboard";
        }
        
        // Zeige Login-Seite mit OAuth2 Optionen
        return "login";
    }

    @GetMapping("/dashboard2")
    public String dashboard(Authentication authentication, Model model) {
        String authMethod = determineAuthMethod(authentication);
        String username = authentication.getName();
        
        model.addAttribute("username", username);
        model.addAttribute("authMethod", authMethod);
        model.addAttribute("authorities", authentication.getAuthorities());
        
        logger.info("User {} accessed dashboard via {}", username, authMethod);
        
        return "dashboard";
    }

    @GetMapping("/profile")
    @ResponseBody
    public Map<String, Object> profile(Authentication authentication, HttpServletRequest request) {
        Map<String, Object> profile = new HashMap<>();
        
        profile.put("username", authentication.getName());
        profile.put("authMethod", determineAuthMethod(authentication));
        profile.put("authorities", authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
        
        // Zusätzliche Informationen je nach Auth-Typ
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            profile.put("provider", oauth2Token.getAuthorizedClientRegistrationId());
            profile.put("attributes", oauth2Token.getPrincipal().getAttributes());
        } else if (authentication instanceof PreAuthenticatedAuthenticationToken) {
            profile.put("headerUser", request.getHeader("X_AUTH_USER"));
            profile.put("remoteAddr", request.getRemoteAddr());
        }
        
        return profile;
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // Standard Spring Security Logout
        new SecurityContextLogoutHandler().logout(request, response, 
            SecurityContextHolder.getContext().getAuthentication());
        
        return "redirect:/login?logout";
    }

    @GetMapping("/auth/status")
    @ResponseBody
    public Map<String, Object> authStatus(Authentication authentication, HttpServletRequest request) {
        Map<String, Object> status = new HashMap<>();
        
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            status.put("authenticated", true);
            status.put("username", authentication.getName());
            status.put("authMethod", determineAuthMethod(authentication));
            
            // Prüfe ob Header noch gültig ist (bei Header-Auth)
            if (authentication instanceof PreAuthenticatedAuthenticationToken) {
                String currentHeaderUser = request.getHeader("X_AUTH_USER");
                status.put("headerStillValid", 
                    currentHeaderUser != null && currentHeaderUser.equals(authentication.getName()));
            }
        } else {
            status.put("authenticated", false);
            
            // Prüfe ob Header-Auth möglich wäre
            String headerUser = request.getHeader("X_AUTH_USER");
            status.put("headerAuthAvailable", headerUser != null && !headerUser.trim().isEmpty());
        }
        
        return status;
    }

    private String determineAuthMethod(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            return "OAuth2 (" + oauth2Token.getAuthorizedClientRegistrationId() + ")";
        } else if (authentication instanceof PreAuthenticatedAuthenticationToken) {
            return "Header (Reverse Proxy)";
        } else {
            return "Unknown";
        }
    }
}
