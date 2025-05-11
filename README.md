# Payment Optimization System

This Java application implements an efficient algorithm for optimizing payment methods to maximize discounts across multiple orders. The system can handle combinations of payment cards and loyalty points, applying various discount strategies to minimize the total payment amount.

## Overview

The Payment Optimization System solves the problem of selecting the best payment method or combination of methods for each order from a set of available payment methods, each with their own limits and discount rates. The goal is to maximize the total discount while ensuring all orders are fully paid.

### Features

- Support for multiple payment methods (cards and loyalty points)
- Various discount strategies:
  - Full payment with loyalty points
  - Full payment with a payment card
  - Partial payment with points (minimum 10%), remainder with a card
- Efficient greedy algorithm with heuristics for optimal performance

## Problem Description

In our online supermarket, customers can pay for orders using:
- Traditional payment methods (e.g., credit cards, bank transfers)
- Loyalty points

Each order can be paid:
- Entirely with one traditional payment method
- Entirely with loyalty points
- Partially with points and partially with one traditional payment method

### Discount Rules

1. Each order has a specific subset of promotions (payment methods) that can be applied.
2. If an entire order is paid with a specific bank card, a percentage discount is applied based on the payment method definition.
3. If at least 10% of the order value is paid with loyalty points, a 10% discount is applied to the entire order.
4. If the entire order is paid with loyalty points, the discount defined for the "PUNKTY" method is applied instead of the 10% discount.

The algorithm aims to optimize the distribution of payment methods across all orders to maximize the total discount while adhering to all rules and payment method limits.

## Technical Implementation

### Main Classes

- `Order`: Represents an order with its ID, value, and available promotions
- `PaymentMethods`: Represents a payment method with its ID, discount percentage, and limit
- `PaymentOptimizer`: Contains the main optimization algorithm
- `DiscountCalculator`: Calculates discounts for various payment strategies
- `PaymentStrategy`: Represents a strategy for paying a specific order
- `PaymentResult`: Stores the optimization result

### Algorithm

The optimization algorithm follows these steps:

1. Calculate the discount potential for each order based on available payment methods
2. Sort orders by discount potential (highest to lowest)
3. For each order, generate and evaluate all possible payment strategies
4. Choose the strategy with the highest discount for each order
5. Update the remaining limits of the payment methods and accumulate results

The algorithm prioritizes maximizing the discount percentage, and when discounts are equal, it prefers using loyalty points over cards.

### Performance

The algorithm has a time complexity of O(N * M), where:
- N is the number of orders
- M is the number of available payment methods

## Usage

### Input Format

The application takes two JSON files as input:

1. Orders JSON file (e.g., `orders.json`):
```json
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
  }
]
```

2. Payment Methods JSON file (e.g., `paymentmethods.json`):
```json
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
```

### Running the Application

```bash
java -jar payment-optimizer.jar /path/to/orders.json /path/to/paymentmethods.json
```

### Output

The application outputs the total amount spent with each payment method e.g.:

```
mZysk 165.00
BosBankrut 190.00
PUNKTY 100.00
```

## Building the Project

The project uses Maven for building and dependency management:

```bash
mvn clean package
```

This will create a fat JAR with all dependencies in the `target` directory.

## Dependencies

- Java 21
- Lombok (for reducing boilerplate code)
- Jackson (for JSON processing)
- JUnit 5 (for testing)
