package com.payment.util;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.data.Order;
import com.payment.data.PaymentMethods;

import lombok.experimental.UtilityClass;


import java.io.File;
import java.io.IOException;
import java.util.List;

@UtilityClass
public class JsonParser {
    
    private final ObjectMapper objectMapper = createObjectMapper();
    
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
    
    public List<Order> loadOrders(String filePath) throws IOException {
        return objectMapper.readValue(new File(filePath), new TypeReference<List<Order>>() {});
    }
    

    public List<PaymentMethods> loadPaymentMethods(String filePath) throws IOException {
        List<PaymentMethods> methods = objectMapper.readValue(
                new File(filePath), 
                new TypeReference<List<PaymentMethods>>() {}
        );
        
        methods.forEach(PaymentMethods::postConstruct);
        
        return methods;
    }
}

