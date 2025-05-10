package com.payment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.payment.data.Order;
import com.payment.data.PaymentMethods;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DiscountCalculator {

    private static BigDecimal PARTIAL_POINTS_DISCOUNT = new BigDecimal("0.1");
    private static BigDecimal PARTIAL_POINTS_THRESHOLD = new BigDecimal("0.1");


    public static BigDecimal calculateFullCardDiscount(Order order, PaymentMethods paymentMethod) {
        if (!order.hasPromotions(paymentMethod.getPaymentId())|| !paymentMethod.hasEnoughLimit(order.getValueAsBigDecimal())) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal orderValue = new BigDecimal(order.getValue());
        BigDecimal discountRate = new BigDecimal(paymentMethod.getDiscount()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        
        return orderValue.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculatePartialPointsDiscount(Order order) {
        BigDecimal orderValue = new BigDecimal(order.getValue());
        return orderValue.multiply(PARTIAL_POINTS_DISCOUNT).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateFullPointsDiscount(Order order, PaymentMethods pointsMethod) {
        if (!pointsMethod.hasEnoughLimit(order.getValueAsBigDecimal())) {
            return BigDecimal.ZERO;
        }
        BigDecimal orderValue = new BigDecimal(order.getValue());
        BigDecimal discountRate = new BigDecimal(pointsMethod.getDiscount()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        
        return orderValue.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateMinPointsForDiscount(Order order) {
        BigDecimal orderValue = new BigDecimal(order.getValue());
        return orderValue.multiply(PARTIAL_POINTS_THRESHOLD).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateFinalCost(BigDecimal orderValue, BigDecimal discount) {
        return orderValue.subtract(discount).setScale(2, RoundingMode.HALF_UP);
    }
}