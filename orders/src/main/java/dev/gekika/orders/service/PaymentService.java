package dev.gekika.orders.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * STUB. Phase 1 pretends payment always succeeds. Later this becomes a
 * real call to a Payment service / gateway. Isolated behind this class so
 * swapping it doesn't touch OrderService.
 */
@Service
public class PaymentService {
    public boolean charge(UUID userId, BigDecimal amount) {
        // Always "succeeds" for now.
        return true;
    }
}