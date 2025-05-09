package com.payment;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import com.payment.data.Order;
import com.payment.data.PaymentMethods;
import com.payment.util.JsonParser;

import lombok.extern.java.Log;

@Log
public class Main {
    
    public static void main(String[] args) {

        if (args.length != 2) {
            log.severe("Two arguments are required: path to orders.json file and path to paymentmethods.json file");
            System.out.println("Usage: java -jar app.jar orders.json paymentmethods.json");
            System.exit(1);
        }
        
        String ordersFilePath = args[0];
        String paymentMethodsFilePath = args[1];
        
        try {
            List<Order> orders = JsonParser.loadOrders(ordersFilePath);
            List<PaymentMethods> paymentMethods = JsonParser.loadPaymentMethods(paymentMethodsFilePath);
            
            log.info("Loaded " + orders.size() + " orders.");
            log.info("Loaded " + paymentMethods.size() + " payment methods.");
            

            
            System.out.println("Loaded orders:");
            orders.forEach(order -> System.out.println(order));
            
            System.out.println("\nLoaded payment methods:");
            paymentMethods.forEach(method -> System.out.println(method));
            
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error while loading JSON files", e);
            System.err.println("Error while loading files: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Unexpected error", e);
            System.err.println("An unexpected error occurred: " + e.getMessage());
            System.exit(1);
        }
    }
}