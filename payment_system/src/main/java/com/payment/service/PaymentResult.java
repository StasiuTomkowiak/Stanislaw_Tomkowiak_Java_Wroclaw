package com.payment.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class PaymentResult {
    private Map<String, BigDecimal> paymentAmounts = new HashMap<>();
    
    public void addPayment(String paymentMethodId, BigDecimal amount) {
    
        paymentAmounts.merge(paymentMethodId, amount, BigDecimal::add);
    }
}
