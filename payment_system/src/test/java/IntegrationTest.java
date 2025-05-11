import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.payment.data.Order;
import com.payment.data.PaymentMethods;
import com.payment.service.DiscountCalculator;
import com.payment.service.PaymentOptimizer;
import com.payment.service.PaymentResult;
import com.payment.util.JsonParser;

class IntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void fullIntegrationTest() throws IOException {
        // Przygotowanie danych testowych
        String ordersJson = """
                [
                  {
                    "id": "ORDER1",
                    "value": "100.00",
                    "promotions": ["mZysk"]
                  },
                  {
                    "id": "ORDER2",
                    "value": "200.00",
                    "promotions": ["BosBankrut"]
                  },
                  {
                    "id": "ORDER3",
                    "value": "150.00",
                    "promotions": ["mZysk", "BosBankrut"]
                  },
                  {
                    "id": "ORDER4",
                    "value": "50.00"
                  }
                ]
                """;
        
        String paymentMethodsJson = """
                [
                  {
                    "id": "PUNKTY",
                    "discount": "15",
                    "limit": "100.00"
                  },
                  {
                    "id": "mZysk",
                    "discount": "10",
                    "limit": "180.00"
                  },
                  {
                    "id": "BosBankrut",
                    "discount": "5",
                    "limit": "200.00"
                  }
                ]
                """;

        Path ordersFile = tempDir.resolve("orders.json");
        Path paymentMethodsFile = tempDir.resolve("paymentmethods.json");
        
        Files.writeString(ordersFile, ordersJson);
        Files.writeString(paymentMethodsFile, paymentMethodsJson);

        List<Order> orders = JsonParser.loadOrders(ordersFile.toString());
        List<PaymentMethods> paymentMethods = JsonParser.loadPaymentMethods(paymentMethodsFile.toString());

        DiscountCalculator calculator = new DiscountCalculator();
        PaymentOptimizer optimizer = new PaymentOptimizer(calculator);
        PaymentResult result = optimizer.optimizePayments(orders, paymentMethods);

        Map<String, BigDecimal> paymentAmounts = result.getPaymentAmounts();
        
        assertEquals(3, paymentAmounts.size());

        assertEquals(new BigDecimal("165.00"), paymentAmounts.get("mZysk"));
        assertEquals(new BigDecimal("190.00"), paymentAmounts.get("BosBankrut"));
        assertEquals(new BigDecimal("100.00"), paymentAmounts.get("PUNKTY"));
    }
}