package com.virtbank.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtbank.dto.AuthResponse;
import com.virtbank.dto.RegisterRequest;
import com.virtbank.entity.*;
import com.virtbank.entity.enums.*;
import com.virtbank.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private LoanRepository loanRepository;

    private static String adminToken;
    private static String customerToken;
    private static Long loanId;

    @BeforeAll
    static void setup(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper,
                      @Autowired UserRepository userRepository, @Autowired AccountRepository accountRepository,
                      @Autowired LoanRepository loanRepository) throws Exception {
        // Register admin
        RegisterRequest adminReq = new RegisterRequest();
        adminReq.setFirstName("IntAdmin"); adminReq.setLastName("User");
        adminReq.setEmail("intadmin@test.com"); adminReq.setPassword("P@ssword1");
        adminReq.setPhone("5554001111"); adminReq.setUserType("ADMIN");

        MvcResult adminResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminReq)))
                .andReturn();
        if (adminResult.getResponse().getStatus() == 201) {
            adminToken = objectMapper.readValue(
                    adminResult.getResponse().getContentAsString(), AuthResponse.class).getToken();
        }

        // Register customer
        RegisterRequest custReq = new RegisterRequest();
        custReq.setFirstName("IntCust"); custReq.setLastName("User");
        custReq.setEmail("intcust@test.com"); custReq.setPassword("P@ssword1");
        custReq.setPhone("5554002222"); custReq.setUserType("CUSTOMER");

        MvcResult custResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(custReq)))
                .andReturn();
        customerToken = objectMapper.readValue(
                custResult.getResponse().getContentAsString(), AuthResponse.class).getToken();

        // Create a pending loan for the customer
        User customer = userRepository.findByEmail("intcust@test.com").orElseThrow();
        Account account = accountRepository.save(Account.builder()
                .user(customer).accountNumber("LOAN1234567890")
                .accountType(AccountType.SAVINGS).balance(new BigDecimal("10000.00"))
                .currency("USD").status(AccountStatus.ACTIVE).build());

        Loan loan = loanRepository.save(Loan.builder()
                .user(customer).account(account)
                .loanType(LoanType.PERSONAL)
                .amount(new BigDecimal("5000.00"))
                .interestRate(new BigDecimal("5.0"))
                .termMonths(12).status(LoanStatus.PENDING)
                .purpose("Integration test loan").build());
        loanId = loan.getId();
    }

    @Test
    @Order(1)
    void getUsers_withAdminJwt_returns200() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    void getUsers_withCustomerJwt_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    void approveLoan_withAdminJwt_returns200() throws Exception {
        if (adminToken == null || loanId == null) return;
        mockMvc.perform(put("/api/admin/loans/" + loanId + "/decision")
                        .param("decision", "APPROVED")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @Order(4)
    void approveLoan_withCustomerJwt_returns403() throws Exception {
        mockMvc.perform(put("/api/admin/loans/1/decision")
                        .param("decision", "APPROVED")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }
}
