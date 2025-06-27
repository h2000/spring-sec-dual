package com.example.security;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.security.user.User;
import com.example.security.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Erstelle Test-User
        if (!userRepository.existsByUsername("testuser")) {
            User testUser = new User("testuser", "test@example.com");
            testUser.addRole(User.Role.USER);
            testUser.setAuthMethod("HEADER");
            userRepository.save(testUser);
        }

        if (!userRepository.existsByUsername("testadmin")) {
            User testAdmin = new User("testadmin", "admin@example.com");
            testAdmin.addRole(User.Role.ADMIN);
            testAdmin.addRole(User.Role.USER);
            testAdmin.setAuthMethod("HEADER");
            userRepository.save(testAdmin);
        }
    }

    @Test
    void whenAccessingPublicEndpoint_thenOk() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void whenAccessingProtectedEndpointWithoutAuth_thenRedirectToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void whenAccessingWithValidHeader_thenAuthenticated() throws Exception {
        mockMvc.perform(get("/dashboard")
                .header("X_AUTH_USER", "testuser"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"));
    }

    @Test
    void whenAccessingWithInvalidHeader_thenRedirectToLogin() throws Exception {
        mockMvc.perform(get("/dashboard")
                .header("X_AUTH_USER", "nonexistentuser"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void whenAccessingApiWithValidHeader_thenReturnsUserInfo() throws Exception {
        mockMvc.perform(get("/api/user/info")
                .header("X_AUTH_USER", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void whenAccessingProtectedApiWithValidHeader_thenOk() throws Exception {
        mockMvc.perform(get("/api/protected")
                .header("X_AUTH_USER", "testuser"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("testuser")));
    }

    @Test
    void whenAccessingAdminApiWithUserRole_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/admin")
                .header("X_AUTH_USER", "testuser"))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenAccessingAdminApiWithAdminRole_thenOk() throws Exception {
        mockMvc.perform(get("/api/admin")
                .header("X_AUTH_USER", "testadmin"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Admin")));
    }

    @Test
    @WithMockUser(username = "mockuser", roles = {"USER"})
    void whenAccessingWithMockUser_thenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/protected"))
                .andExpect(status().isOk());
    }

    @Test
    void whenAccessingAuthStatus_thenReturnsCorrectInfo() throws Exception {
        mockMvc.perform(get("/auth/status")
                .header("X_AUTH_USER", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.authMethod").value(org.hamcrest.Matchers.containsString("Header")));
    }

    @Test
    void whenAccessingAuthStatusWithoutAuth_thenReturnsUnauthenticated() throws Exception {
        mockMvc.perform(get("/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false));
    }

    @Test
    void whenAccessingProfile_thenReturnsUserProfile() throws Exception {
        mockMvc.perform(get("/profile")
                .header("X_AUTH_USER", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.authMethod").value(org.hamcrest.Matchers.containsString("Header")));
    }
}
