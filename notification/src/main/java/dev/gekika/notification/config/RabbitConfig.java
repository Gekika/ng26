package dev.gekika.notification.config;

import dev.gekika.notification.event.EventProperties;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;



@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange ordersExchange(EventProperties p) {
        return new TopicExchange(p.exchange());
    }

    @Bean
    public Queue orderPlacedQueue(EventProperties p) {
        // durable = survives broker restart; messages wait here if we're down.
        return QueueBuilder.durable(p.orderPlacedQueue()).build();
    }

    @Bean
    public Binding orderPlacedBinding(Queue orderPlacedQueue, TopicExchange ordersExchange,
                                      EventProperties p) {
        // Bind our queue to the exchange for the "order.placed" routing key.
        return BindingBuilder.bind(orderPlacedQueue)
                .to(ordersExchange)
                .with(p.orderPlacedRoutingKey());
    }

    @Bean
    public MessageConverter jsonConverter() {
        return new JacksonJsonMessageConverter();
    }
}