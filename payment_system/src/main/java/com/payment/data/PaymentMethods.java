package com.payment.data;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentMethods {
    
    @JsonProperty("id")
    private String paymentId;
    private String discount;
    private String limit;

    private BigDecimal remainingLimit;

    public void postConstruct() {
        if (remainingLimit == null) {
            remainingLimit = getLimitAsBigDecimal();
        }
    }

    public int getDiscountAsInt() {
        return Integer.parseInt(discount);
    }

    public BigDecimal getLimitAsBigDecimal() {
        return new BigDecimal(limit);
    }

    public void useAmount(BigDecimal amount) {
        this.remainingLimit = this.remainingLimit.subtract(amount);
    }

    public boolean hasEnoughLimit(BigDecimal amount) {
        return this.remainingLimit.compareTo(amount) >= 0;
    }

    public PaymentMethods copy() {
        PaymentMethods copy = new PaymentMethods();
        copy.setPaymentId(this.paymentId);
        copy.setDiscount(this.discount);
        copy.setLimit(this.limit);
        copy.setRemainingLimit(this.remainingLimit);
        return copy;
    }


}
