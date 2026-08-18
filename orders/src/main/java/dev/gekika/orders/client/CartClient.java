package dev.gekika.orders.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.gekika.orders.config.CartProperties;
import dev.gekika.orders.exception.CartUnavailableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class CartClient {

    private final RestClient restClient;

    public CartClient(CartProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CartView(
            @JsonProperty("cartId") UUID cartId,
            @JsonProperty("userId") UUID userId,
            @JsonProperty("items") List<CartItemView> items,
            @JsonProperty("total") BigDecimal total) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CartItemView(
            @JsonProperty("itemId") UUID itemId,
            @JsonProperty("productId") UUID productId,
            @JsonProperty("productName") String productName,
            @JsonProperty("unitPrice") BigDecimal unitPrice,
            @JsonProperty("quantity") int quantity,
            @JsonProperty("lineTotal") BigDecimal lineTotal) {}

    public CartView getCart(String bearerToken) {
        try {
            return restClient.get()
                    .uri("/v1/cart")
                    .header("Authorization", bearerToken)
                    .retrieve()
                    .body(CartView.class);
        } catch (RestClientException ex) {
            throw new CartUnavailableException("Could not read cart: " + ex.getMessage());
        }
    }

    public void clearCart(String bearerToken) {
        try {
            restClient.delete()
                    .uri("/v1/cart")
                    .header("Authorization", bearerToken)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            // Non-fatal: order is already placed.
        }
    }
}