package dev.gekika.orders.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.gekika.orders.config.CatalogProperties;
import dev.gekika.orders.exception.ProductUnavailableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient(CatalogProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductView(
            @JsonProperty("id") UUID id,
            @JsonProperty("name") String name,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("stock") int stock,
            @JsonProperty("active") boolean active) {}

    public ProductView getProduct(UUID productId) {
        try {
            ProductView p = restClient.get()
                    .uri("/v1/catalog/products/{id}", productId)
                    .retrieve()
                    .body(ProductView.class);
            if (p == null || !p.active()) throw new ProductUnavailableException(productId);
            return p;
        } catch (RestClientResponseException ex) {
            throw new ProductUnavailableException(productId);
        }
    }
}