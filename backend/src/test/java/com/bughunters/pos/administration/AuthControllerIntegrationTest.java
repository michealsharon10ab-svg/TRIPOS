package com.bughunters.pos.administration;

import com.bughunters.pos.administration.dto.LoginRequest;
import com.bughunters.pos.administration.entity.Branch;
import com.bughunters.pos.administration.entity.BranchType;
import com.bughunters.pos.administration.entity.Role;
import com.bughunters.pos.administration.entity.Tenant;
import com.bughunters.pos.administration.entity.User;
import com.bughunters.pos.administration.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setupData() {
        refreshTokenRepository.deleteAll();
        terminalRepository.deleteAll();
        userRepository.deleteAll();
        branchRepository.deleteAll();
        tenantRepository.deleteAll();
        roleRepository.deleteAll();

        Tenant tenant = new Tenant("LLG", "Lanka Leisure Group", "Test Tenant");
        tenant = tenantRepository.save(tenant);

        Branch branch = new Branch(tenant, "COLOMBO-CITY", "Colombo City Hotel", "Galle Rd", "123", BranchType.HOTEL);
        branch = branchRepository.save(branch);

        Role cashierRole = new Role("ROLE_CASHIER", "Cashier Role");
        cashierRole = roleRepository.save(cashierRole);

        User user = new User();
        user.setUsername("testcashier");
        user.setEmail("cashier@test.com");
        user.setPassword(passwordEncoder.encode("Password@123"));
        user.setFullName("Test Cashier");
        user.setActive(true);
        user.setTenant(tenant);
        user.setRoles(Set.of(cashierRole));
        user.setBranches(Set.of(branch));
        userRepository.save(user);
    }

    @Test
    void testLogin_Success_Returns200WithJwtToken() throws Exception {
        LoginRequest request = new LoginRequest("testcashier", "Password@123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.data.user.username", is("testcashier")))
                .andExpect(jsonPath("$.data.user.roles", hasItem("ROLE_CASHIER")));
    }

    @Test
    void testLogin_InvalidCredentials_Returns401() throws Exception {
        LoginRequest request = new LoginRequest("testcashier", "WrongPassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Invalid username or password")));
    }

    @Test
    void testGetCurrentUser_WithValidToken_ReturnsProfile() throws Exception {
        // Step 1: Login
        LoginRequest loginRequest = new LoginRequest("testcashier", "Password@123");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseBody).get("data").get("accessToken").asText();

        // Step 2: Access /api/v1/auth/me with Bearer token
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", is("testcashier")))
                .andExpect(jsonPath("$.data.email", is("cashier@test.com")));
    }

    @Test
    void testGetCurrentUser_WithoutToken_Returns401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
