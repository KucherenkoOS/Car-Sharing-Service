package org.kucherenkoos.carsharingservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kucherenkoos.carsharingservice.dto.payment.PaymentRequestDto;
import org.kucherenkoos.carsharingservice.dto.payment.PaymentResponseDto;
import org.kucherenkoos.carsharingservice.model.User;
import org.kucherenkoos.carsharingservice.service.PaymentService;
import org.kucherenkoos.carsharingservice.util.TestDataHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    private User mockUser;
    private User mockManager;
    private Authentication userAuth;
    private Authentication managerAuth;

    @BeforeEach
    void setUp() {
        mockUser = TestDataHelper.createTestUser();
        userAuth = new UsernamePasswordAuthenticationToken(
                mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        mockManager = TestDataHelper.createTestManager();
        managerAuth = new UsernamePasswordAuthenticationToken(
                mockManager, null, List.of(new SimpleGrantedAuthority("ROLE_MANAGER")));
    }

    @Test
    @DisplayName("Create payment session - Success")
    void createPaymentSession_ValidRequest_ReturnsResponseDto() throws Exception {
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setRentalId(1L);

        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setId(10L);
        responseDto.setSessionUrl("https://checkout.stripe.com/...");

        when(paymentService.createPaymentSession(any(PaymentRequestDto.class))).thenReturn(responseDto);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/payments/")
                        .with(authentication(userAuth))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.sessionUrl").value("https://checkout.stripe.com/..."));
    }

    @Test
    @DisplayName("Get payments as USER - Returns list of payments")
    void getPayments_AsUser_ReturnsList() throws Exception {
        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setId(10L);

        when(paymentService.getPayments(null, mockUser)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/payments/")
                        .with(authentication(userAuth))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L));
    }

    @Test
    @DisplayName("Get payments as MANAGER with specific user_id - Returns list")
    void getPayments_AsManagerWithUserId_ReturnsList() throws Exception {
        Long targetUserId = 5L;
        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setId(10L);

        when(paymentService.getPayments(targetUserId, mockManager)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/payments/")
                        .with(authentication(managerAuth))
                        .param("user_id", targetUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L));
    }

    @Test
    @DisplayName("Handle success payment (Stripe callback) - Returns success message")
    void handleSuccessPayment_ValidSessionId_ReturnsMessage() throws Exception {
        String sessionId = "cs_test_123";

        mockMvc.perform(get("/payments/success/")
                        .param("session_id", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment successful!"));

        verify(paymentService).processSuccessPayment(sessionId);
    }

    @Test
    @DisplayName("Handle cancel payment (Stripe callback) - Returns cancel message")
    void handleCancelPayment_ValidSessionId_ReturnsMessage() throws Exception {
        String sessionId = "cs_test_123";
        String expectedMessage = "Payment paused. You can complete the transaction within 24 hours via the session link.";

        mockMvc.perform(get("/payments/cancel/")
                        .param("session_id", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedMessage));

        verify(paymentService).processCancelPayment(sessionId);
    }

    @Test
    @DisplayName("Renew expired payment session - Success")
    void renewPayment_ValidId_ReturnsResponseDto() throws Exception {
        Long paymentId = 1L;
        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setId(paymentId);
        responseDto.setSessionUrl("https://checkout.stripe.com/new_session");

        when(paymentService.renewPaymentSession(paymentId)).thenReturn(responseDto);

        mockMvc.perform(post("/payments/{id}/renew", paymentId)
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId))
                .andExpect(jsonPath("$.sessionUrl").value("https://checkout.stripe.com/new_session"));
    }
}
