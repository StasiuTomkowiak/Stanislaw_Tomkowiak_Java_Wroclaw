import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.payment.data.Order;
import com.payment.data.PaymentMethods;
import com.payment.service.DiscountCalculator;

public class DiscountCalculatorTest {
    private DiscountCalculator calculator;
    private Order order;
    private PaymentMethods cardMethod;
    private PaymentMethods pointsMethod;
    
    @BeforeEach
    void setUp() {
        calculator = new DiscountCalculator();
        
        order = Order.builder()
                .orderId("TEST1")
                .value("100.00")
                .promotions(Arrays.asList("mZysk"))
                .build();
        
        cardMethod = PaymentMethods.builder()
                .paymentId("mZysk")
                .discount("10")
                .limit("200.00")
                .build();
        cardMethod.postConstruct();
        
        pointsMethod = PaymentMethods.builder()
                .paymentId("PUNKTY")
                .discount("15")
                .limit("100.00")
                .build();
        pointsMethod.postConstruct();
    }
    
    @Test
    void calculateFullCardDiscount_shouldReturnCorrectDiscount() {

        BigDecimal discount = calculator.calculateFullCardDiscount(order, cardMethod);
        
        assertEquals(new BigDecimal("10.00"), discount);
    }
    
    @Test
    void calculateFullCardDiscount_shouldReturnZeroWhenPromotionNotAvailable() {

        Order orderWithoutPromotion = Order.builder()
                .orderId("TEST2")
                .value("100.00")
                .promotions(Collections.emptyList())
                .build();

        BigDecimal discount = calculator.calculateFullCardDiscount(orderWithoutPromotion, cardMethod);
        
        assertEquals(BigDecimal.ZERO, discount);
    }
    
    @Test
    void calculatePartialPointsDiscount_shouldReturnTenPercentDiscount() {

        BigDecimal discount = calculator.calculatePartialPointsDiscount(order);
 
        assertEquals(new BigDecimal("10.00"), discount);
    }
    
    @Test
    void calculateFullPointsDiscount_shouldReturnCorrectDiscount() {

        BigDecimal discount = calculator.calculateFullPointsDiscount(order, pointsMethod);
        
        assertEquals(new BigDecimal("15.00"), discount);
    }
    
    @Test
    void calculateMinPointsForDiscount_shouldReturnTenPercentOfOrderValue() {

        BigDecimal minPoints = calculator.calculateMinPointsForDiscount(order);

        assertEquals(new BigDecimal("10.00"), minPoints);
    }
    
    @Test
    void calculateFinalCost_shouldSubtractDiscountFromOrderValue() {

        BigDecimal orderValue = new BigDecimal("100.00");
        BigDecimal discount = new BigDecimal("15.00");

        BigDecimal finalCost = calculator.calculateFinalCost(orderValue, discount);

        assertEquals(new BigDecimal("85.00"), finalCost);
    }
}
