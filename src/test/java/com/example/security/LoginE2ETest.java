package com.example.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Login E2E Tests")
public class LoginE2ETest extends BaseE2ETest {

    @Test
    @DisplayName("Test Header-based Authentication Login")
    void testHeaderBasedLogin() {
        // Set up header authentication context
        context.setExtraHTTPHeaders(java.util.Map.of("X_AUTH_USER", "john.doe"));
        
        // Navigate to the protected page
        page.navigate(getBaseUrl() + "/dashboard");
        waitForPageLoad();
        
        takeScreenshot("header-auth-dashboard");
        
        // Verify we can access the dashboard with header authentication
        assertTrue(page.url().contains("/dashboard") || 
                  page.locator("text=Dashboard").isVisible() ||
                  page.locator("text=Welcome").isVisible());
        
        // Verify no redirect to login page occurred
        assertFalse(page.url().contains("/login"));
    }

    @Test
    @DisplayName("Test Header-based Authentication with Admin User")
    void testHeaderBasedAdminLogin() {
        // Set up admin header authentication
        context.setExtraHTTPHeaders(java.util.Map.of("X_AUTH_USER", "admin"));
        
        // Navigate to admin endpoint
        page.navigate(getBaseUrl() + "/api/admin");
        waitForPageLoad();
        
        takeScreenshot("header-auth-admin-access");
        
        // Should not get 403 Forbidden
        assertFalse(page.content().contains("403") || page.content().contains("Forbidden"));
        
        // Navigate to dashboard as admin
        page.navigate(getBaseUrl() + "/dashboard");
        waitForPageLoad();
        
        // Should have access to dashboard
        assertTrue(page.url().contains("/dashboard") || 
                  page.locator("text=Dashboard").isVisible());
    }

    @Test
    @DisplayName("Test Login Page Access without Authentication")
    void testLoginPageAccess() {
        // Navigate to login page without any authentication
        page.navigate(getBaseUrl() + "/login");
        waitForPageLoad();
        
        takeScreenshot("login-page");
        
        // Should be on login page
        assertTrue(page.url().contains("/login"));
        
        // Check for login form elements
        assertTrue(page.locator("form").isVisible() || 
                  page.locator("text=Login").isVisible() ||
                  page.locator("text=Sign in").isVisible());
    }

    @Test
    @DisplayName("Test Protected Resource Access without Authentication")
    void testProtectedResourceWithoutAuth() {
        // Try to access protected dashboard without authentication
        page.navigate(getBaseUrl() + "/dashboard");
        waitForPageLoad();
        
        takeScreenshot("unauthorized-dashboard-access");
        
        // Should be redirected to login page
        assertTrue(page.url().contains("/login") || 
                  page.url().contains("/oauth2/authorization"));
    }

    @Test
    @DisplayName("Test API Access without Authentication")
    void testApiAccessWithoutAuth() {
        // Try to access protected API without authentication
        page.navigate(getBaseUrl() + "/api/user/info");
        waitForPageLoad();
        
        takeScreenshot("unauthorized-api-access");
        
        // Should be redirected to login or get 401/403
        assertTrue(page.url().contains("/login") || 
                  page.content().contains("401") || 
                  page.content().contains("403") ||
                  page.content().contains("Unauthorized") ||
                  page.content().contains("Forbidden"));
    }

    @Test
    @DisplayName("Test Header Authentication with Invalid User")
    void testHeaderAuthWithInvalidUser() {
        // Set up header with non-existent user
        context.setExtraHTTPHeaders(java.util.Map.of("X_AUTH_USER", "nonexistent.user"));
        
        // Try to access protected resource
        page.navigate(getBaseUrl() + "/dashboard");
        waitForPageLoad();
        
        takeScreenshot("invalid-user-header-auth");
        
        // Should be redirected to login or get unauthorized response
        assertTrue(page.url().contains("/login") || 
                  page.content().contains("401") || 
                  page.content().contains("403") ||
                  page.content().contains("Unauthorized"));
    }

    @Test
    @DisplayName("Test User Profile Access with Header Authentication")
    void testUserProfileWithHeaderAuth() {
        // Set up header authentication
        context.setExtraHTTPHeaders(java.util.Map.of("X_AUTH_USER", "john.doe"));
        
        // Navigate to profile page
        page.navigate(getBaseUrl() + "/profile");
        waitForPageLoad();
        
        takeScreenshot("user-profile-header-auth");
        
        // Should show user profile information
        assertTrue(page.locator("text=john.doe").isVisible() ||
                  page.locator("text=Profile").isVisible() ||
                  page.content().contains("john.doe"));
    }

    @Test
    @DisplayName("Test Logout Functionality")
    void testLogout() {
        // First authenticate with header
        context.setExtraHTTPHeaders(java.util.Map.of("X_AUTH_USER", "john.doe"));
        
        // Navigate to dashboard to confirm we're logged in
        page.navigate(getBaseUrl() + "/dashboard");
        waitForPageLoad();
        
        takeScreenshot("before-logout");
        
        // Now logout
        page.navigate(getBaseUrl() + "/logout");
        waitForPageLoad();
        
        takeScreenshot("after-logout");
        
        // Clear headers to simulate logged out state
        context.setExtraHTTPHeaders(java.util.Map.of());
        
        // Try to access protected page again - should redirect to login
        page.navigate(getBaseUrl() + "/dashboard");
        waitForPageLoad();
        
        takeScreenshot("post-logout-dashboard-access");
        
        // Should be redirected to login page
        assertTrue(page.url().contains("/login"));
    }

    
    @Test
    @DisplayName("Test Keycloak Login Flow")
    void testKeycloakLogin() {
        // Navigate to the protected page - should redirect to login
        page.navigate(getBaseUrl() + "/dashboard");
        
        // Should be redirected to login page
        assertTrue(page.url().contains("/login") || page.url().contains("/oauth2/authorization"));
        
        takeScreenshot("login-page");
        
        // Click on Keycloak login (assuming you have such a button)
        if (page.locator("text=Login with Keycloak").isVisible()) {
            page.click("text=Login with Keycloak");
        } else {
            // Direct OAuth2 authorization
            page.navigate(getBaseUrl() + "/oauth2/authorization/keycloak");
        }
        
        // Wait for Keycloak login page
        takeScreenshot("keycloak-login-page");
        page.waitForURL( url -> url.contains("openid-connect"));

        // Fill in test user credentials
        page.fill("#username", "testuser");
        page.fill("#password", "password");
        
        takeScreenshot("keycloak-credentials-filled");
        
        // Click login
        page.click("#kc-login");
        
        // Wait for redirect back to application
        page.waitForURL(getBaseUrl() + "/**");
        waitForPageLoad();
        
        takeScreenshot("after-keycloak-login");
        
        // Verify we're now authenticated and can access the dashboard
        assertTrue(page.url().contains("/dashboard") || 
                  page.locator("text=Dashboard").isVisible() ||
                  page.locator("text=Welcome").isVisible());
    }

    @Test
    @DisplayName( "Test Keycloak Admin User Login" )
    void testKeycloakAdminLogin() {
        // Test admin user login
        loginWithKeycloak("admin", "admin");
        
        // Navigate to admin endpoint
        page.navigate(getBaseUrl() + "/api/admin");
        
        // Should not get 403 Forbidden
        waitForPageLoad();
        
        // The response should indicate success (not a 403 error page)
        assertFalse(page.content().contains("403") || page.content().contains("Forbidden"));
    }

    @Test
    @DisplayName("Test Keycloak Logout Functionality")
    void testKeycloakLogout() {
        // First login
        loginWithKeycloak("testuser", "password");
        
        // Navigate to a protected page to confirm we're logged in
        page.navigate(getBaseUrl() + "/dashboard");
        waitForPageLoad();
        
        // Now logout
        page.navigate(getBaseUrl() + "/logout");
        
        // Try to access protected page again - should redirect to login
        page.navigate(getBaseUrl() + "/dashboard");
        
        // Should be redirected to login page
        assertTrue(page.url().contains("/login"));
    }

    @Test
    @DisplayName("Test Keycloak Container is Running (Set Breakpoint for Debugging)")
    void testKeycloakContainerIsRunning() {
        assertTrue(keycloakContainer.isRunning());
        assertNotNull(keycloakContainer.getAuthServerUrl());
        
        System.out.println("Keycloak is running at: " + keycloakContainer.getAuthServerUrl());
        System.out.println("Admin console: " + keycloakContainer.getAuthServerUrl() + "/admin");
        System.out.println("Admin user: admin / admin");
    }
}