
package com.example.security.filter;

import com.example.security.user.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Custom Filter für Header-basierte Authentifizierung
@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(HeaderAuthenticationFilter.class);
    private static final String AUTH_HEADER = "X_AUTH_USER";
    
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request, 
        @NonNull HttpServletResponse response, 
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Prüfe ob bereits authentifiziert
        if (SecurityContextHolder.getContext().getAuthentication() != null && 
            SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
            !SecurityContextHolder.getContext().getAuthentication().getName().equals("anonymousUser")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Prüfe Header
        String username = request.getHeader(AUTH_HEADER);
        
        if (username != null && !username.trim().isEmpty()) {
            try {
                // Lade Nutzerdetails
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Erstelle Authentication Token
                PreAuthenticatedAuthenticationToken authToken = 
                    new PreAuthenticatedAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Setze Authentication im SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                // Logge erfolgreiche Header-Authentifizierung
                logger.info("User authenticated via header: " + username);
                
            } catch (UsernameNotFoundException e) {
                logger.warn("Header authentication failed for user: " + username);
                // Weiter zum OAuth2 Flow
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
