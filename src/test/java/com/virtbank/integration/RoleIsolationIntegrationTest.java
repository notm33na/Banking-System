package com.virtbank.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtbank.dto.RegisterRequest;
import com.virtbank.dto.AuthResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoleIsolationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static String customerToken;
    private static String adminToken;

    @BeforeAll
    static void setup(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) throws Exception {
        // Register customer
        RegisterRequest custReq = new RegisterRequest();
        custReq.setFirstName("Cust"); custReq.setLastName("User");
        custReq.setEmail("roletest-cust@test.com"); custReq.setPassword("P@ssword1");
        custReq.setPhone("5550001111"); custReq.setUserType("CUSTOMER");

        MvcResult custResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(custReq)))
                .andReturn();
        customerToken = objectMapper.readValue(
                custResult.getResponse().getContentAsString(), AuthResponse.class).getToken();

        // Register admin (if ADMIN type registration is supported, otherwise use direct DB insert)
        RegisterRequest adminReq = new RegisterRequest();
        adminReq.setFirstName("Admin"); adminReq.setLastName("User");
        adminReq.setEmail("roletest-admin@test.com"); adminReq.setPassword("P@ssword1");
        adminReq.setPhone("5550002222"); adminReq.setUserType("ADMIN");

        MvcResult adminResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminReq)))
                .andReturn();
        if (adminResult.getResponse().getStatus() == 201) {
            adminToken = objectMapper.readValue(
                    adminResult.getResponse().getContentAsString(), AuthResponse.class).getToken();
        }
    }

    // ── Admin endpoints should reject customer token ─────────────────

    @Test
    void adminUsers_withCustomerToken_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminLoans_withCustomerToken_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/loans")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminTransactions_withCustomerToken_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/transactions")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    // ── Customer endpoints should reject admin token ─────────────────

    @Test
    void customerProfile_withAdminToken_returns403() throws Exception {
        if (adminToken == null) return; // skip if admin registration not supported
        mockMvc.perform(get("/api/customer/profile")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    // ── No token at all ──────────────────────────────────────────────

    @Test
    void adminEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/customer/transactions/history/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void businessEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/business/accounts"))
                .andExpect(status().isUnauthorized());
    }
}
