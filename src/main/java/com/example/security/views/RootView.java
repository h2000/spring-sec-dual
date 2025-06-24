package com.example.security.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;

@Route("")
@AnonymousAllowed
public class RootView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthenticationContext authenticationContext;

    public RootView(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        // If user is authenticated and not anonymous, redirect to dashboard
        if (authenticationContext.isAuthenticated()) {
            event.forwardTo("dashboard");
        } else {
            // Not authenticated, redirect to login
            event.forwardTo("login");
        }
    }
}
