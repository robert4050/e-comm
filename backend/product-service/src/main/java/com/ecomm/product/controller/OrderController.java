package com.ecomm.product.controller;

import com.ecomm.product.dto.OrderDto;
import com.ecomm.product.dto.request.CreateOrderRequest;
import com.ecomm.product.entity.Order;
import com.ecomm.product.kafka.ProductEventPublisher;
import com.ecomm.product.repository.OrderRepository;
import com.ecomm.product.service.TwilioSmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderRepository orderRepository;
    private final ProductEventPublisher eventPublisher;
    private final TwilioSmsService twilioSmsService;

    public OrderController(OrderRepository orderRepository,
                           ProductEventPublisher eventPublisher,
                           TwilioSmsService twilioSmsService) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.twilioSmsService = twilioSmsService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders(@RequestParam(required = false) String email) {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        if (email != null && !email.isBlank()) {
            orders = orders.stream()
                    .filter(o -> o.getCustomerEmail().equalsIgnoreCase(email.trim()))
                    .collect(Collectors.toList());
        }
        List<OrderDto> dtos = orders.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable String id) {
        return orderRepository.findById(id)
                .map(order -> ResponseEntity.ok(mapToDto(order)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@RequestBody CreateOrderRequest request) {
        String orderId = "ord-" + UUID.randomUUID().toString().substring(0, 8);
        String orderNumber = "ORD-" + ((int) (10000 + Math.random() * 90000));
        String deliveryOtp = String.format("%06d", new Random().nextInt(1000000));

        Order order = new Order();
        order.setId(orderId);
        order.setOrderNumber(orderNumber);
        order.setCustomerName(request.getCustomerName() != null ? request.getCustomerName() : "Valued Customer");
        order.setCustomerEmail(request.getCustomerEmail() != null ? request.getCustomerEmail() : "customer@e-comm.local");
        order.setCustomerPhone(request.getCustomerPhone() != null && !request.getCustomerPhone().isBlank()
                ? request.getCustomerPhone() : "+91 98765 43210");
        order.setItemsCount(request.getItemsCount() != null ? request.getItemsCount() : 1);
        order.setTotalPrice(request.getTotalPrice());
        order.setPaymentStatus("PAID");
        order.setPaymentMethod("RAZORPAY");
        order.setRazorpayPaymentId(request.getRazorpayPaymentId() != null ? request.getRazorpayPaymentId() : "pay_" + UUID.randomUUID().toString().substring(0, 10));
        order.setFulfilmentStatus("PROCESSING");
        order.setCarrier("Razorpay Express Handoff");
        order.setTrackingNumber("RPAY-" + ((int) (10000000 + Math.random() * 90000000)));
        order.setDeliveryOtp(deliveryOtp);
        order.setOtpVerified(false);

        Order saved = orderRepository.save(order);
        OrderDto dto = mapToDto(saved);

        eventPublisher.saveGenericToOutbox("OrderPlaced", "Order", saved.getId(), dto);
        log.info("Placed order {} with Razorpay Payment ID {} and OTP {}", saved.getOrderNumber(), saved.getRazorpayPaymentId(), saved.getDeliveryOtp());

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<OrderDto> confirmOrder(@PathVariable String id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        order.setFulfilmentStatus("CONFIRMED");
        if (order.getDeliveryOtp() == null || order.getDeliveryOtp().isBlank()) {
            order.setDeliveryOtp(String.format("%06d", new Random().nextInt(1000000)));
        }

        Order updated = orderRepository.save(order);
        OrderDto dto = mapToDto(updated);

        // Dispatch Twilio Delivery OTP SMS to Customer
        twilioSmsService.sendDeliveryOtp(updated.getCustomerPhone(), updated.getOrderNumber(), updated.getDeliveryOtp());

        eventPublisher.saveGenericToOutbox("OrderConfirmed", "Order", updated.getId(), dto);
        log.info("Admin confirmed order {}. Dispatched Delivery OTP {} to {}", updated.getOrderNumber(), updated.getDeliveryOtp(), updated.getCustomerPhone());

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateStatus(@PathVariable String id, @RequestBody(required = false) Map<String, String> body) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        String newStatus = (body != null) ? body.get("status") : null;
        if (newStatus != null && !newStatus.isBlank()) {
            order.setFulfilmentStatus(newStatus);
        } else {
            // Cycle status
            switch (order.getFulfilmentStatus()) {
                case "PROCESSING" -> order.setFulfilmentStatus("CONFIRMED");
                case "CONFIRMED" -> order.setFulfilmentStatus("SHIPPED");
                case "SHIPPED" -> order.setFulfilmentStatus("DELIVERED");
                default -> {}
            }
        }

        // When moving to SHIPPED or CONFIRMED, ensure OTP is dispatched via Twilio
        if ("SHIPPED".equalsIgnoreCase(order.getFulfilmentStatus()) || "CONFIRMED".equalsIgnoreCase(order.getFulfilmentStatus())) {
            if (order.getDeliveryOtp() == null || order.getDeliveryOtp().isBlank()) {
                order.setDeliveryOtp(String.format("%06d", new Random().nextInt(1000000)));
            }
            twilioSmsService.sendDeliveryOtp(order.getCustomerPhone(), order.getOrderNumber(), order.getDeliveryOtp());
        }

        if ("DELIVERED".equalsIgnoreCase(order.getFulfilmentStatus())) {
            order.setOtpVerified(true);
        }

        Order updated = orderRepository.save(order);
        OrderDto dto = mapToDto(updated);

        eventPublisher.saveGenericToOutbox("OrderStatusUpdated", "Order", updated.getId(), dto);
        log.info("Updated order {} status to {}", updated.getOrderNumber(), updated.getFulfilmentStatus());

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/verify-otp")
    public ResponseEntity<?> verifyDeliveryOtp(@PathVariable String id, @RequestBody Map<String, String> body) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        String enteredOtp = body.get("otp");
        if (enteredOtp == null || enteredOtp.trim().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Delivery verification OTP is required."));
        }

        String expectedOtp = order.getDeliveryOtp();
        if (expectedOtp != null && expectedOtp.trim().equalsIgnoreCase(enteredOtp.trim())) {
            order.setFulfilmentStatus("DELIVERED");
            order.setOtpVerified(true);
            Order updated = orderRepository.save(order);
            OrderDto dto = mapToDto(updated);

            eventPublisher.saveGenericToOutbox("OrderDeliveredViaOtp", "Order", updated.getId(), dto);
            log.info("Successfully verified Delivery OTP for order {}. Marked as DELIVERED.", updated.getOrderNumber());

            return ResponseEntity.ok(dto);
        } else {
            log.warn("Invalid Delivery OTP submitted for order {}. Expected: {}, Received: {}", order.getOrderNumber(), expectedOtp, enteredOtp);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid Delivery OTP. Please enter the valid 6-digit code received via Twilio SMS.",
                    "submitted", enteredOtp
            ));
        }
    }

    @PostMapping("/{id}/resend-otp")
    public ResponseEntity<?> resendDeliveryOtp(@PathVariable String id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        if (order.getDeliveryOtp() == null || order.getDeliveryOtp().isBlank()) {
            order.setDeliveryOtp(String.format("%06d", new Random().nextInt(1000000)));
            order = orderRepository.save(order);
        }

        twilioSmsService.sendDeliveryOtp(order.getCustomerPhone(), order.getOrderNumber(), order.getDeliveryOtp());

        return ResponseEntity.ok(Map.of(
                "message", "Delivery verification OTP successfully resent via Twilio to " + order.getCustomerPhone(),
                "orderNumber", order.getOrderNumber(),
                "customerPhone", order.getCustomerPhone(),
                "otp", order.getDeliveryOtp()
        ));
    }

    private OrderDto mapToDto(Order order) {
        return new OrderDto(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getCustomerPhone(),
                order.getItemsCount(),
                order.getTotalPrice(),
                order.getPaymentStatus(),
                order.getPaymentMethod(),
                order.getRazorpayPaymentId(),
                order.getFulfilmentStatus(),
                order.getCarrier(),
                order.getTrackingNumber(),
                order.getDeliveryOtp(),
                order.getOtpVerified(),
                order.getCreatedAt() != null ? order.getCreatedAt() : Instant.now()
        );
    }
}
