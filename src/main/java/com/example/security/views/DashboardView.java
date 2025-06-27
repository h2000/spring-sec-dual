package com.example.security.views;

import com.example.security.CustomOAuth2User;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.stream.Collectors;

@Route("dashboard")
@PermitAll
public class DashboardView extends VerticalLayout {

    private final AuthenticationContext authenticationContext;

    public DashboardView(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        createHeader();
        createUserInfo();
        createActions();
    }

    private void createHeader() {
        H1 title = new H1("Dashboard");
        title.getStyle().set("color", "#2563eb").set("margin-bottom", "0");
        
        Paragraph subtitle = new Paragraph("Willkommen in der Dual Authentication Security Anwendung");
        subtitle.getStyle().set("color", "#6b7280").set("margin-top", "0");
        
        add(title, subtitle);
    }

    private void createUserInfo() {
        // Get Authentication from SecurityContext instead of AuthenticationContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            add(new Paragraph("Nicht authentifiziert"));
            return;
        }

        Div userCard = new Div();
        userCard.getStyle()
                .set("background", "white")
                .set("padding", "1.5rem")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)")
                .set("margin-bottom", "1rem");

        H2 userTitle = new H2("Benutzer-Informationen");
        userTitle.getStyle().set("margin-top", "0").set("color", "#374151");

        // Username
        HorizontalLayout usernameLayout = new HorizontalLayout();
        usernameLayout.setSpacing(true);
        usernameLayout.add(
            new Span("Benutzername: "),
            createInfoSpan(authentication.getName())
        );

        // Authentication type and additional info based on principal type
        Object principal = authentication.getPrincipal();
        String authType;
        String email = null;
        String fullName = null;

        if (principal instanceof CustomOAuth2User customOAuth2User) {
            authType = "OAuth2/OIDC (Custom)";
            email = customOAuth2User.getEmail();
            fullName = customOAuth2User.getFullName();
        } else if (principal instanceof OidcUser oidcUser) {
            authType = "OAuth2/OIDC (OIDC)";
            email = oidcUser.getAttribute("email");
            String firstName = oidcUser.getAttribute("given_name");
            String lastName = oidcUser.getAttribute("family_name");
            if (firstName != null || lastName != null) {
                fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                fullName = fullName.trim();
            }
        } else if (principal instanceof OAuth2User oauth2User) {
            authType = "OAuth2/OIDC (OAuth2)";
            email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            if (name != null) {
                fullName = name;
            }
        } else {
            authType = "Header Authentication";
        }

        HorizontalLayout authTypeLayout = new HorizontalLayout();
        authTypeLayout.setSpacing(true);
        authTypeLayout.add(
            new Span("Authentifizierung: "),
            createInfoSpan(authType)
        );

        // Roles
        HorizontalLayout rolesLayout = new HorizontalLayout();
        rolesLayout.setSpacing(true);
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
        rolesLayout.add(
            new Span("Rollen: "),
            createInfoSpan(roles.isEmpty() ? "Keine Rollen" : roles)
        );

        userCard.add(userTitle, usernameLayout, authTypeLayout, rolesLayout);

        // Add email if available
        if (email != null && !email.isEmpty()) {
            HorizontalLayout emailLayout = new HorizontalLayout();
            emailLayout.setSpacing(true);
            emailLayout.add(
                new Span("E-Mail: "),
                createInfoSpan(email)
            );
            userCard.add(emailLayout);
        }

        // Add full name if available
        if (fullName != null && !fullName.trim().isEmpty()) {
            HorizontalLayout nameLayout = new HorizontalLayout();
            nameLayout.setSpacing(true);
            nameLayout.add(
                new Span("Name: "),
                createInfoSpan(fullName)
            );
            userCard.add(nameLayout);
        }

        // Add debug info for OAuth2 users
        if (principal instanceof OAuth2User oauth2User) {
            Div debugInfo = new Div();
            debugInfo.getStyle()
                    .set("margin-top", "1rem")
                    .set("padding", "0.5rem")
                    .set("background", "#f9fafb")
                    .set("border-radius", "4px")
                    .set("font-size", "0.8rem")
                    .set("color", "#6b7280");
            
            debugInfo.add(new Span("Debug - Available attributes: " + 
                oauth2User.getAttributes().keySet().toString()));
            userCard.add(debugInfo);
        }

        add(userCard);
    }

    private Span createInfoSpan(String text) {
        Span span = new Span(text);
        span.getStyle()
                .set("font-weight", "bold")
                .set("color", "#059669");
        return span;
    }

    private void createActions() {
        Div actionsCard = new Div();
        actionsCard.getStyle()
                .set("background", "white")
                .set("padding", "1.5rem")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)");

        H2 actionsTitle = new H2("Aktionen");
        actionsTitle.getStyle().set("margin-top", "0").set("color", "#374151");

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        // API Test Button
        Button apiTestButton = new Button("API Test");
        apiTestButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        apiTestButton.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.navigate("api-test"));
        });

        // Profile Button
        Button profileButton = new Button("Profil");
        profileButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        profileButton.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.navigate("profile"));
        });

        // Logout Button
        Button logoutButton = new Button("Abmelden");
        logoutButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        logoutButton.addClickListener(e -> {
            authenticationContext.logout();
        });

        buttonLayout.add(apiTestButton, profileButton, logoutButton);
        actionsCard.add(actionsTitle, buttonLayout);
        add(actionsCard);
    }
}