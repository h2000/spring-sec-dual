package com.example.security.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;

@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthenticationContext authenticationContext;

    public LoginView(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 title = new H1("Dual Authentication Security");
        title.getStyle().set("color", "#2563eb");

        Div loginOptions = new Div();
        loginOptions.getStyle()
                .set("background", "white")
                .set("padding", "2rem")
                .set("border-radius", "8px")
                .set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.1)")
                .set("text-align", "center")
                .set("min-width", "300px");

        H1 loginTitle = new H1("Anmelden");
        loginTitle.getStyle().set("margin-bottom", "1.5rem");

        Div oauthSection = new Div();
        oauthSection.getStyle().set("margin-bottom", "1rem");

        // Keycloak Login Button
        Button keycloakLoginButton = new Button("Login with Keycloak");
        keycloakLoginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        keycloakLoginButton.getStyle()
                .set("background", "#4c76a6")
                .set("color", "white")
                .set("padding", "0.75rem 1.5rem")
                .set("border-radius", "4px")
                .set("font-weight", "500")
                .set("margin-bottom", "0.5rem")
                .set("width", "100%");
        
        keycloakLoginButton.addClickListener(e -> {
            UI.getCurrent().getPage().setLocation("/oauth2/authorization/keycloak");
        });

        // Azure Login Button
        Button azureLoginButton = new Button("Login with Azure AD");
        azureLoginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        azureLoginButton.getStyle()
                .set("background", "#0078d4")
                .set("color", "white")
                .set("padding", "0.75rem 1.5rem")
                .set("border-radius", "4px")
                .set("font-weight", "500")
                .set("margin", "0.25rem 0")
                .set("width", "100%");
        
        azureLoginButton.addClickListener(e -> {
            UI.getCurrent().getPage().setLocation("/oauth2/authorization/azure");
        });

        Div headerAuthInfo = new Div();
        headerAuthInfo.getStyle()
                .set("margin-top", "1.5rem")
                .set("padding", "1rem")
                .set("background", "#f3f4f6")
                .set("border-radius", "4px")
                .set("font-size", "0.9rem")
                .set("color", "#6b7280");

        headerAuthInfo.add("Header-Authentifizierung ist automatisch aktiv, wenn entsprechende Header gesetzt sind.");

        oauthSection.add(keycloakLoginButton, azureLoginButton);
        loginOptions.add(loginTitle, oauthSection, headerAuthInfo);

        add(title, loginOptions);

        getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("min-height", "100vh");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Check if user is already authenticated via header
        if (authenticationContext.isAuthenticated()) {
            event.forwardTo("dashboard");
        }
    }
}