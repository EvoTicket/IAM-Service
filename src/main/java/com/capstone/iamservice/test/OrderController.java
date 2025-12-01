package com.capstone.iamservice.test;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final RedisStreamProducer producer;
    private static final String STREAM_KEY = "order-events";

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderEvent order) {
        producer.sendMessage(STREAM_KEY, order);
        return ResponseEntity.ok("Order created and sent to stream");
    }

    @PostMapping("/with-key")
    public ResponseEntity<String> createOrderWithKey(
            @RequestParam String orderKey,
            @RequestBody OrderEvent order) {
        producer.sendMessageWithKey(STREAM_KEY, orderKey, order);
        return ResponseEntity.ok("Order created with key and sent to stream");
    }
}
