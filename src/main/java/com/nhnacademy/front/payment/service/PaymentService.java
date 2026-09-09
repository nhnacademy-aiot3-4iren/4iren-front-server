package com.nhnacademy.front.payment.service;

import com.nhnacademy.front.payment.client.PaymentClient;
import com.nhnacademy.front.payment.dto.SubscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentClient paymentClient;

    public SubscriptionResponse getCurrentSubscription(){
        return paymentClient.getCurrentSubscription();
    }
}
