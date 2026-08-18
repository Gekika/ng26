package dev.gekika.notification.listerner;

import dev.gekika.notification.event.OrderPlacedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class OrderPlacedListener {

    private static final Logger log = LoggerFactory.getLogger(OrderPlacedListener.class);

    /**
     * Fires whenever an OrderPlaced event lands in our queue. Spring
     * deserializes the JSON into the event record automatically.
     */
    @RabbitListener(queues = "${app.events.order-placed-queue}")
    public void onOrderPlaced(OrderPlacedEvent event) {
        // For now, just log — later this sends a real email/SMS.
        log.info("📧 Notification: order {} placed by user {} for total {}. Sending confirmation email...",
                event.orderId(), event.userId(), event.total());

        event.items().forEach(item ->
                log.info("   - {} x{} @ {}", item.productName(), item.quantity(), item.unitPrice()));
    }
}