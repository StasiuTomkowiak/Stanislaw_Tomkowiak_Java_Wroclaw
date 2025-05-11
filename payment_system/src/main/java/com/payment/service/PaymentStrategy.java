package com.payment.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.payment.data.Order;
import com.payment.data.PaymentMethods;

import lombok.Data;

@Data
public class PaymentStrategy implements Comparable<PaymentStrategy>{

    private Order order;
    private Map<PaymentMethods, BigDecimal> payments = new HashMap<>();
    private BigDecimal totalDiscount = BigDecimal.ZERO;
    private BigDecimal finalCost = BigDecimal.ZERO;

    public PaymentStrategy(Order order) {
        this.order = order;
    }

    public void addPayment(PaymentMethods method, BigDecimal amount) {
        payments.put(method, amount);
    }

    @Override
    public int compareTo(PaymentStrategy other) {
        int discountComparison = this.totalDiscount.compareTo(other.totalDiscount);
        if (discountComparison != 0) {
            return discountComparison;
        }
        
        BigDecimal thisPointsUsed = getPointsUsed();
        BigDecimal otherPointsUsed = other.getPointsUsed();
        
        return thisPointsUsed.compareTo(otherPointsUsed);
    }

    public BigDecimal getPointsUsed() {
        return payments.entrySet().stream()
                .filter(entry -> "PUNKTY".equals(entry.getKey().getPaymentId()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

}
