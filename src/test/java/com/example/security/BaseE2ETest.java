package com.example.security;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class BaseE2ETest {

	protected static Playwright playwright;
	protected static Browser browser;
	protected BrowserContext context;
	protected Page page;

	@LocalServerPort
	protected int port;

	@Container
	protected static final KeycloakContainer keycloakContainer = new KeycloakContainer()
		.withRealmImportFile("/docker/keycloak-test-realm.json")
		.withAdminUsername("admin")
		.withAdminPassword("admin");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		// Wait for Keycloak to be ready
		keycloakContainer.start();

		// Configure Keycloak properties dynamically
		String keycloakBaseUrl = keycloakContainer.getAuthServerUrl();
		String realmName = "spring-app-realm";

		System.out.println("Keycloak URL: " + keycloakBaseUrl);
		System.out.println("Realm: " + realmName);

		registry.add("spring.security.oauth2.client.provider.keycloak.issuer-uri",
			() -> keycloakBaseUrl + "/realms/" + realmName);
		registry.add("spring.security.oauth2.client.provider.keycloak.authorization-uri",
			() -> keycloakBaseUrl + "/realms/" + realmName + "/protocol/openid-connect/auth");
		registry.add("spring.security.oauth2.client.provider.keycloak.token-uri",
			() -> keycloakBaseUrl + "/realms/" + realmName + "/protocol/openid-connect/token");
		registry.add("spring.security.oauth2.client.provider.keycloak.user-info-uri",
			() -> keycloakBaseUrl + "/realms/" + realmName + "/protocol/openid-connect/userinfo");
		registry.add("spring.security.oauth2.client.provider.keycloak.user-name-attribute",
			() -> "preferred_username");

		// Override client configuration for testing
		registry.add("spring.security.oauth2.client.registration.keycloak.client-id",
			() -> "spring-boot-app");
		registry.add("spring.security.oauth2.client.registration.keycloak.client-secret",
			() -> "test-client-secret");
		registry.add("spring.security.oauth2.client.registration.keycloak.scope",
			() -> "openid,profile,email,roles");
		registry.add("spring.security.oauth2.client.registration.keycloak.authorization-grant-type",
			() -> "authorization_code");
		registry.add("spring.security.oauth2.client.registration.keycloak.redirect-uri",
			() -> "{baseUrl}/login/oauth2/code/{registrationId}");
	}

	protected String getBaseUrl() {
		return "http://localhost:" + port;
	}

	protected String getKeycloakUrl() {
		return keycloakContainer.getAuthServerUrl();
	}

	protected String getKeycloakRealmUrl() {
		return keycloakContainer.getAuthServerUrl() + "/realms/spring-app-realm";
	}

	@BeforeAll
	static void launchBrowser() {
		playwright = Playwright.create();
		browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
				.setHeadless(true) // Set to false for debugging
				.setTimeout(10_000)
				.setDevtools(false) // Disable devtools in tests
				.setArgs(List.of(
					"--no-sandbox",
					"--disable-dev-shm-usage",
					"--disable-gpu",
					"--disable-background-timer-throttling",
					"--disable-backgrounding-occluded-windows",
					"--disable-renderer-backgrounding"
				))

			//            .setSlowMo(100) // Slow down by 100ms for better visibility during debugging
		);
	}


	@BeforeEach
	void createContextAndPage() {
		context = browser.newContext(new Browser.NewContextOptions()
			.setViewportSize(1280, 720)
			.setLocale("en-US")
			.setIgnoreHTTPSErrors(true)
			.setAcceptDownloads(false));

		// Enable request/response logging for debugging
		context.onRequest(request ->
			System.out.println(">> " + request.method() + " " + request.url()));
		context.onResponse(response ->
			System.out.println("<< " + response.status() + " " + response.url()));

		// Set shorter timeouts to prevent hanging
		context.setDefaultTimeout(30_000); // 30 seconds
		context.setDefaultNavigationTimeout(30_000);

		page = context.newPage();
	}

	@AfterEach
	void closeContext() {
		if (context != null) {
			context.close();
		}
	}

	protected void waitForPageLoad() {
		page.waitForLoadState(LoadState.NETWORKIDLE);
	}

	protected void takeScreenshot(String name) {
		page.screenshot(new Page.ScreenshotOptions()
			.setPath(java.nio.file.Paths.get("target/screenshots/" + name + ".png"))
			.setFullPage(true));
	}

	/**
	 * Helper method to login via Keycloak OAuth2 flow
	 */
	protected void loginWithKeycloak(String username, String password) {
		try {
			// Navigate to your login page that will redirect to Keycloak
			page.navigate(getBaseUrl() + "/oauth2/authorization/keycloak");

			// Wait for either Keycloak login page or already authenticated redirect
			page.waitForURL(url ->
					url.contains("/auth/") ||
						url.contains("/realms/") ||
						url.contains("dashboard") ||
						url.contains(getBaseUrl()),
				new Page.WaitForURLOptions().setTimeout(15000)
			);

			// Check if we're already authenticated (redirected back to app)
			if (page.url().contains(getBaseUrl())) {
				System.out.println("Already authenticated, redirected to: " + page.url());
				return;
			}

			// We should be on Keycloak login page now
			takeScreenshot("keycloak-login-page");

			// Wait for login form to be visible
			page.waitForSelector("#username", new Page.WaitForSelectorOptions().setTimeout(5000));

			// Fill in credentials
			page.fill("#username", username);
			page.fill("#password", password);

			takeScreenshot("keycloak-credentials-filled");

			// Click login button
			page.click("#kc-login");

			// Wait for redirect back to application
			page.waitForURL(url -> url.contains(getBaseUrl()),
				new Page.WaitForURLOptions().setTimeout(15000));

			waitForPageLoad();
			takeScreenshot("after-keycloak-login");

		} catch (Exception e) {
			takeScreenshot("keycloak-login-error");
			System.err.println("Error during Keycloak login: " + e.getMessage());
			System.err.println("Current URL: " + page.url());
			System.err.println("Page content: " + page.content());
			throw e;
		}
	}

	@AfterAll
	static void cleanup() {
		try {
			System.out.println("Starting cleanup...");
			closeBrowser();
			System.out.println("Browser closed");
			// Stop containers
			System.out.println("Stopping containers...");
			if (keycloakContainer.isRunning()) {
				keycloakContainer.stop();
			}
			System.out.println("Cleanup completed");
		} catch (Exception e) {
			System.err.println("Error during cleanup: " + e.getMessage());
		}
	}

	static void closeBrowser() {
		if (browser != null) {
			browser.close();
		}
		if (playwright != null) {
			playwright.close();
		}
	}

}