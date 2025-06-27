package com.example.security;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.spring.annotation.EnableVaadin;
import com.vaadin.flow.theme.Theme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Theme(value = "dual-auth-security")
@PWA(
    name = "Dual Authentication Security",
    shortName = "DualAuth",
    description = "Spring Boot Security with Header and OAuth2 Authentication"
)
@EnableVaadin
@Import({VaadinSecurityConfig.class})
public class DualAuthSecurityApplication implements AppShellConfigurator {

    private static final Logger logger = LoggerFactory.getLogger(DualAuthSecurityApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DualAuthSecurityApplication.class, args);
    }

    /**
     * Initialisiert Demo-Daten beim Start der Anwendung
     */
    @Bean
    CommandLineRunner initDatabase(@Autowired UserRepository userRepository) {
        return args -> {
            logger.info("Initializing demo data...");

            // Erstelle Demo-User falls keine existieren
            if (userRepository.count() == 0) {
                // Admin User
                User admin = new User("admin", "admin@example.com");
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.addRole(User.Role.ADMIN);
                admin.addRole(User.Role.USER);
                admin.setAuthMethod("DEMO");
                userRepository.save(admin);

                // Regular User
                User user = new User("user", "user@example.com");
                user.setFirstName("Test");
                user.setLastName("User");
                user.addRole(User.Role.USER);
                user.setAuthMethod("DEMO");
                userRepository.save(user);

                // Manager User
                User manager = new User("manager", "manager@example.com");
                manager.setFirstName("Manager");
                manager.setLastName("User");
                manager.addRole(User.Role.MANAGER);
                manager.addRole(User.Role.USER);
                manager.setAuthMethod("DEMO");
                userRepository.save(manager);

                // Header Auth User (simuliert AD-User)
                User adUser = new User("john.doe", "john.doe@company.com");
                adUser.setFirstName("John");
                adUser.setLastName("Doe");
                adUser.addRole(User.Role.USER);
                adUser.setAuthMethod("HEADER");
                userRepository.save(adUser);

                logger.info("Demo users created:");
                logger.info("- admin/admin@example.com (ADMIN, USER)");
                logger.info("- user/user@example.com (USER)");
                logger.info("- manager/manager@example.com (MANAGER, USER)");
                logger.info("- john.doe/john.doe@company.com (USER, Header Auth)");
            }

            logger.info("Application started successfully!");
            logger.info("Access the application at: http://localhost:8080");
            logger.info("Login page: http://localhost:8080/login");
            logger.info("Dashboard: http://localhost:8080/dashboard");
            logger.info("");
            logger.info("Test Header Authentication:");
            logger.info("curl -H 'X_AUTH_USER: john.doe' http://localhost:8080/dashboard");
            logger.info("");
            logger.info("Test API:");
            logger.info("curl -H 'X_AUTH_USER: admin' http://localhost:8080/api/user/info");
        };
    }
}
