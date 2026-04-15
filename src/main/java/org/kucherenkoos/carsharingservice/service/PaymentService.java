package org.kucherenkoos.carsharingservice.service;

import java.util.List;
import org.kucherenkoos.carsharingservice.dto.payment.PaymentRequestDto;
import org.kucherenkoos.carsharingservice.dto.payment.PaymentResponseDto;
import org.kucherenkoos.carsharingservice.model.User;

public interface PaymentService {
    PaymentResponseDto createPaymentSession(PaymentRequestDto requestDto);

    List<PaymentResponseDto> getPayments(Long userId, User currentUser);

    void processSuccessPayment(String sessionId);

    void processCancelPayment(String sessionId);

    PaymentResponseDto renewPaymentSession(Long paymentId);
}
