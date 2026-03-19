package com.virtbank.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtbank.dto.AuthResponse;
import com.virtbank.dto.RegisterRequest;
import com.virtbank.dto.TransferRequest;
import com.virtbank.entity.Account;
import com.virtbank.entity.User;
import com.virtbank.entity.enums.AccountStatus;
import com.virtbank.entity.enums.AccountType;
import com.virtbank.repository.AccountRepository;
import com.virtbank.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;

    private static String customerToken;
    private static String adminToken;
    private static Long sourceAccountId;
    private static Long destAccountId;

    @BeforeAll
    static void setup(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper,
                      @Autowired UserRepository userRepository, @Autowired AccountRepository accountRepository) throws Exception {
        // Register customer
        RegisterRequest custReq = new RegisterRequest();
        custReq.setFirstName("TxCust"); custReq.setLastName("User");
        custReq.setEmail("txcust@test.com"); custReq.setPassword("P@ssword1");
        custReq.setPhone("5553001111"); custReq.setUserType("CUSTOMER");

        MvcResult custResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(custReq)))
                .andReturn();
        customerToken = objectMapper.readValue(
                custResult.getResponse().getContentAsString(), AuthResponse.class).getToken();

        // Register admin
        RegisterRequest adminReq = new RegisterRequest();
        adminReq.setFirstName("TxAdmin"); adminReq.setLastName("User");
        adminReq.setEmail("txadmin@test.com"); adminReq.setPassword("P@ssword1");
        adminReq.setPhone("5553002222"); adminReq.setUserType("ADMIN");

        MvcResult adminResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminReq)))
                .andReturn();
        if (adminResult.getResponse().getStatus() == 201) {
            adminToken = objectMapper.readValue(
                    adminResult.getResponse().getContentAsString(), AuthResponse.class).getToken();
        }

        // Create two accounts for the customer
        User customer = userRepository.findByEmail("txcust@test.com").orElseThrow();
        Account source = accountRepository.save(Account.builder()
                .user(customer).accountNumber("SRC1234567890")
                .accountType(AccountType.SAVINGS).balance(new BigDecimal("5000.00"))
                .currency("USD").status(AccountStatus.ACTIVE).build());
        Account dest = accountRepository.save(Account.builder()
                .user(customer).accountNumber("DST0987654321")
                .accountType(AccountType.SAVINGS).balance(new BigDecimal("1000.00"))
                .currency("USD").status(AccountStatus.ACTIVE).build());
        sourceAccountId = source.getId();
        destAccountId = dest.getId();
    }

    @Test
    @Order(1)
    void transfer_withoutJwt_returns401() throws Exception {
        TransferRequest req = new TransferRequest();
        req.setSourceAccountId(sourceAccountId);
        req.setDestinationAccountId(destAccountId);
        req.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/customer/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    void transfer_withCustomerJwt_returns200() throws Exception {
        TransferRequest req = new TransferRequest();
        req.setSourceAccountId(sourceAccountId);
        req.setDestinationAccountId(destAccountId);
        req.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/customer/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + customerToken)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void transfer_withAdminJwt_onCustomerAccount_returns403() throws Exception {
        if (adminToken == null) return;
        TransferRequest req = new TransferRequest();
        req.setSourceAccountId(sourceAccountId);
        req.setDestinationAccountId(destAccountId);
        req.setAmount(new BigDecimal("50.00"));

        mockMvc.perform(post("/api/customer/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
