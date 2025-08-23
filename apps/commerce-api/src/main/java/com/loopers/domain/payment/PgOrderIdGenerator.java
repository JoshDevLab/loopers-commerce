package com.loopers.domain.payment;

public class PgOrderIdGenerator {
    public static String generate(Long orderId) {
        return "ORDER_" + orderId;
    }

    public static Long extractOrderId(String pgOrderId) {
        return Long.parseLong(pgOrderId.split("_")[1]);
    }
}
