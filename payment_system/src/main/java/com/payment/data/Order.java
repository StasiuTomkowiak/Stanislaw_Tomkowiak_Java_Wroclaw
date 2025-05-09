package com.payment.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
public class Order {

    @JsonProperty("id")
    private String orderId;
    private String value;

    @Builder.Default
    private List<String> promotions = new ArrayList<>();
    
    public BigDecimal getValueAsBigDecimal() {
        return new BigDecimal(value);
    }

    public boolean hasPromotions(String promotionId) {
        return promotions != null && promotions.contains(promotionId);
    }


}
