package com.example.security.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.util.Optional;
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
        
        getStyle()
            .set("background", "#f8fafc")
            .set("min-height", "100vh");
    }

    private void createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        header.getStyle()
            .set("background", "white")
            .set("padding", "1rem 2rem")
            .set("border-radius", "8px")
            .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)")
            .set("margin-bottom", "2rem");

        H1 title = new H1("Dashboard");
        title.getStyle().set("color", "#1f2937").set("margin", "0");

        Button logoutButton = new Button("Abmelden", new Icon(VaadinIcon.SIGN_OUT));
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logoutButton.addClickListener(e -> logout());

        header.add(title, logoutButton);
        add(header);
    }

    private void createUserInfo() {
        authenticationContext.getAuthenticatedUser(User.class).ifPresent(auth -> {
            VerticalLayout userCard = new VerticalLayout();
            userCard.getStyle()
                .set("background", "white")
                .set("padding", "2rem")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)")
                .set("margin-bottom", "2rem");

            H2 welcomeTitle = new H2("Willkommen!");
            welcomeTitle.getStyle().set("color", "#1f2937").set("margin-top", "0");

            // User information
            Div userInfo = new Div();
            userInfo.getStyle().set("margin-bottom", "1rem");

            String username = auth.getUsername();
            // String authMethod = getAuthenticationMethod(auth);
            
            Paragraph userPara = new Paragraph("Benutzer: " + username);
            userPara.getStyle().set("font-weight", "bold").set("margin", "0.5rem 0");
            
            Paragraph authPara = new Paragraph("Authentifizierungsmethode: " ); // TODO + authMethod);
            // authPara.getStyle().set("color", "#6b7280").set("margin", "0.5rem 0");

            // Roles
            String roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
            
            Paragraph rolesPara = new Paragraph("Rollen: " + roles);
            rolesPara.getStyle().set("color", "#059669").set("margin", "0.5rem 0");

            userInfo.add(userPara, authPara, rolesPara);

            // Additional OAuth2 info if available
            // if (auth.getPrincipal() instanceof OAuth2User oauth2User) {
            //     String email = oauth2User.getAttribute("email");
            //     if (email != null) {
            //         Paragraph emailPara = new Paragraph("E-Mail: " + email);
            //         emailPara.getStyle().set("color", "#6b7280").set("margin", "0.5rem 0");
            //         userInfo.add(emailPara);
            //     }
            // }

            userCard.add(welcomeTitle, userInfo);
            add(userCard);
        });
    }

    private void createActions() {
        VerticalLayout actionsCard = new VerticalLayout();
        actionsCard.getStyle()
            .set("background", "white")
            .set("padding", "2rem")
            .set("border-radius", "8px")
            .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)");

        H2 actionsTitle = new H2("Verfügbare Aktionen");
        actionsTitle.getStyle().set("color", "#1f2937").set("margin-top", "0");

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        Button apiTestButton = new Button("API Test", new Icon(VaadinIcon.CONNECT));
        apiTestButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        apiTestButton.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.getPage().open("/api/user/info", "_blank"));
        });

        Button userManagementButton = new Button("Benutzerverwaltung", new Icon(VaadinIcon.USERS));
        userManagementButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        userManagementButton.setEnabled(hasRole("ROLE_ADMIN"));
        userManagementButton.addClickListener(e -> {
            // TODO: Navigate to user management view
            Notification.show("Benutzerverwaltung noch nicht implementiert");
        });

        buttonLayout.add(apiTestButton, userManagementButton);

        // Info section
        Div infoSection = new Div();
        infoSection.getStyle()
            .set("margin-top", "1.5rem")
            .set("padding", "1rem")
            .set("background", "#f3f4f6")
            .set("border-radius", "4px");

        Paragraph infoText = new Paragraph(
            "Diese Anwendung demonstriert die Dual-Authentifizierung mit Header-basierter " +
            "Authentifizierung (für AD-Integration) und OAuth2 (Google). " +
            "Die Header-Authentifizierung wird automatisch verwendet, wenn entsprechende Header gesetzt sind."
        );
        infoText.getStyle().set("color", "#6b7280").set("margin", "0");

        infoSection.add(infoText);

        actionsCard.add(actionsTitle, buttonLayout, infoSection);
        add(actionsCard);
    }

    // private String getAuthenticationMethod(User auth) {
    //     if (auth.getPrincipal() instanceof OAuth2User) {
    //         return "OAuth2 (Google)";
    //     } else if (auth.getDetails() != null && auth.getDetails().toString().contains("Header")) {
    //         return "Header Authentication";
    //     } else {
    //         return "Standard Authentication";
    //     }
    // }

    private boolean hasRole(String role) {
        Optional<User> userO = authenticationContext.getAuthenticatedUser(User.class);    
        if (userO.isEmpty()) {
            return false;
        }
        User user = userO.get();
        var roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return roles.contains(role);
            
    }

    private void logout() {
        VaadinServletRequest request = VaadinServletRequest.getCurrent();
        if (request != null) {
            try {
                SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
                logoutHandler.logout(request.getHttpServletRequest(), null, null);
                getUI().ifPresent(ui -> ui.getPage().setLocation("/login?logout"));
            } catch (Exception e) {
                getUI().ifPresent(ui -> ui.getPage().setLocation("/login"));
            }
        }
    }
}
