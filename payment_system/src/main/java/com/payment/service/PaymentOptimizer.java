package com.payment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.payment.data.Order;
import com.payment.data.PaymentMethods;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PaymentOptimizer {
    
    private final DiscountCalculator discountCalculator;
    private static final String POINTS_ID = "PUNKTY";
    
    public PaymentResult optimizePayments(List<Order> orders, List<PaymentMethods> paymentMethods) {
   
        List<PaymentMethods> availableMethods = paymentMethods.stream()
                .map(PaymentMethods::copy)
                .collect(Collectors.toList());
        
        Optional<PaymentMethods> pointsMethod = availableMethods.stream()
                .filter(m -> POINTS_ID.equals(m.getPaymentId()))
                .findFirst();
        
        Map<Order, BigDecimal> discountPotential = calculateDiscountPotential(orders, availableMethods, pointsMethod.orElse(null));
        
        List<Order> sortedOrders = orders.stream()
                .sorted(Comparator.comparing(discountPotential::get).reversed())
                .collect(Collectors.toList());
        
        PaymentResult result = new PaymentResult();
        
        for (Order order : sortedOrders) {
            PaymentStrategy bestStrategy = findBestStrategy(order, availableMethods, pointsMethod.orElse(null));
            
            for (Map.Entry<PaymentMethods, BigDecimal> payment : bestStrategy.getPayments().entrySet()) {
                PaymentMethods method = payment.getKey();
                BigDecimal amount = payment.getValue();
                
                method.useAmount(amount);
                result.addPayment(method.getPaymentId(), amount);
            }
        }
        
        return result;
    }
    
    private Map<Order, BigDecimal> calculateDiscountPotential(
            List<Order> orders, 
            List<PaymentMethods> availableMethods, 
            PaymentMethods pointsMethod) {
        
        Map<Order, BigDecimal> potential = new HashMap<>();
        
        for (Order order : orders) {
            BigDecimal orderValue = new BigDecimal(order.getValue());
            BigDecimal maxDiscount = BigDecimal.ZERO;
            
            if (pointsMethod != null) {
                BigDecimal pointsDiscount = discountCalculator.calculateFullPointsDiscount(order, pointsMethod);
                maxDiscount = maxDiscount.max(pointsDiscount);
                
                BigDecimal partialPointsDiscount = discountCalculator.calculatePartialPointsDiscount(order);
                maxDiscount = maxDiscount.max(partialPointsDiscount);
            }
            
            for (PaymentMethods cardMethod : availableMethods) {
                if (POINTS_ID.equals(cardMethod.getPaymentId())) continue;
                
                BigDecimal cardDiscount = discountCalculator.calculateFullCardDiscount(order, cardMethod);
                maxDiscount = maxDiscount.max(cardDiscount);
            }
            
            BigDecimal discountPercent;
            if (orderValue.compareTo(BigDecimal.ZERO) > 0) {
                discountPercent = maxDiscount.divide(orderValue, 4, RoundingMode.HALF_UP);
            } else {
                discountPercent = BigDecimal.ZERO;
            }
            
            BigDecimal scaleFactor = new BigDecimal("0.0001");
            BigDecimal valueComponent = orderValue.multiply(scaleFactor);
            
            potential.put(order, discountPercent.add(valueComponent));
        }
        
        return potential;
    }
    
    private PaymentStrategy findBestStrategy(
            Order order, 
            List<PaymentMethods> availableMethods, 
            PaymentMethods pointsMethod) {
        
        List<PaymentStrategy> strategies = new ArrayList<>();
        BigDecimal orderValue = new BigDecimal(order.getValue());

        if (pointsMethod != null && pointsMethod.hasEnoughLimit(orderValue)) {
            BigDecimal discount = discountCalculator.calculateFullPointsDiscount(order, pointsMethod);
            BigDecimal finalCost = discountCalculator.calculateFinalCost(orderValue, discount);
            
            PaymentStrategy pointsStrategy = new PaymentStrategy(order);
            pointsStrategy.addPayment(pointsMethod, finalCost);
            pointsStrategy.setTotalDiscount(discount);
            pointsStrategy.setFinalCost(finalCost);
            strategies.add(pointsStrategy);
            
        }
        
        for (PaymentMethods cardMethod : availableMethods) {
            if (POINTS_ID.equals(cardMethod.getPaymentId())) continue;
            
            BigDecimal discount = discountCalculator.calculateFullCardDiscount(order, cardMethod);
            BigDecimal finalCost = discountCalculator.calculateFinalCost(orderValue, discount);
            
            if (cardMethod.hasEnoughLimit(finalCost)) {
                PaymentStrategy cardStrategy = new PaymentStrategy(order);
                cardStrategy.addPayment(cardMethod, finalCost);
                cardStrategy.setTotalDiscount(discount);
                cardStrategy.setFinalCost(finalCost);
                strategies.add(cardStrategy);
                
            }
        }

        if (pointsMethod != null) {
            BigDecimal minPointsRequired = discountCalculator.calculateMinPointsForDiscount(order);
            
            if (pointsMethod.hasEnoughLimit(minPointsRequired)) {
                BigDecimal discount = discountCalculator.calculatePartialPointsDiscount(order);
                BigDecimal finalCost = discountCalculator.calculateFinalCost(orderValue, discount);
                

                List<PaymentMethods> sortedCards = availableMethods.stream()
                        .filter(m -> !POINTS_ID.equals(m.getPaymentId()))
                        .sorted((c1, c2) -> {
                            BigDecimal d1 = discountCalculator.calculateFullCardDiscount(order, c1);
                            BigDecimal d2 = discountCalculator.calculateFullCardDiscount(order, c2);
                            return d2.compareTo(d1); 
                        })
                        .collect(Collectors.toList());

                tryMixedStrategy(order, pointsMethod, sortedCards, minPointsRequired, 
                               finalCost.subtract(minPointsRequired), discount, finalCost, strategies);

                if (pointsMethod.getRemainingLimit().compareTo(minPointsRequired) > 0 && 
                    pointsMethod.getRemainingLimit().compareTo(finalCost) < 0) {
                    
                    BigDecimal maxPoints = pointsMethod.getRemainingLimit();
                    tryMixedStrategy(order, pointsMethod, sortedCards, maxPoints, 
                                   finalCost.subtract(maxPoints), discount, finalCost, strategies);
                }
            }
        }
 
        return strategies.stream()
                .max(Comparator
                     .comparing(PaymentStrategy::getTotalDiscount)
                     .thenComparing(PaymentStrategy::getPointsUsed))
                .orElseThrow(() -> new IllegalStateException(
                    "Cant find strategy: " + order.getOrderId()));
    }
    
    private void tryMixedStrategy(
            Order order, 
            PaymentMethods pointsMethod,
            List<PaymentMethods> sortedCards,
            BigDecimal pointsAmount,
            BigDecimal cardAmount,
            BigDecimal discount,
            BigDecimal finalCost,
            List<PaymentStrategy> strategies) {
        
        for (PaymentMethods cardMethod : sortedCards) {
            if (cardMethod.hasEnoughLimit(cardAmount)) {
                PaymentStrategy mixedStrategy = new PaymentStrategy(order);
                mixedStrategy.addPayment(pointsMethod, pointsAmount);
                mixedStrategy.addPayment(cardMethod, cardAmount);
                mixedStrategy.setTotalDiscount(discount);
                mixedStrategy.setFinalCost(finalCost);
                strategies.add(mixedStrategy);

                break;
            }
        }
    }
}