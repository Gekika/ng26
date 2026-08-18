package dev.gekika.cart.client;

import dev.gekika.cart.config.CatalogProperties;
import dev.gekika.cart.exception.ProductUnavailableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Talks to the Catalog service over HTTP. This is a SYNCHRONOUS call:
 * Cart blocks until Catalog answers. Used at add-to-cart time to confirm
 * the product exists/active and to snapshot its price and name.
 */
@Component
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient(CatalogProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();
    }

    /** What Catalog returns for a product — we map its JSON into this. */
    public record ProductView(
            UUID id, String name, BigDecimal price, int stock, boolean active) {}

    /**
     * Fetch a product from Catalog. Throws ProductUnavailableException if
     * the product doesn't exist (404) or Catalog is unreachable.
     */
    public ProductView getProduct(UUID productId) {
        try {
            ProductView product = restClient.get()
                    .uri("/v1/catalog/products/{id}", productId)
                    .retrieve()
                    .body(ProductView.class);

            if (product == null || !product.active()) {
                throw new ProductUnavailableException(productId);
            }
            return product;

        } catch (RestClientResponseException ex) {
            // 404 from Catalog = product doesn't exist.
            throw new ProductUnavailableException(productId);
        }
    }
}