package junit;

import com.brain.coffee.controller.ProductController;
import com.brain.coffee.model.Product;
import com.brain.coffee.model.ProductEvent;
import com.brain.coffee.repository.IProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class JUnit5ControllerTest {
    private WebTestClient client;

    private List<Product> productList;

    @Autowired
    private IProductRepository repository;

    @BeforeEach
    void beforeEach() {
        this.client = WebTestClient
                .bindToController(new ProductController(repository))
                .configureClient()
                .baseUrl("http://localhost:8085/api/product/")
                .build();

        this.productList = repository.findAll().collectList().block();
    }

    @Test
    void testGetAllProduct() {
        client.get()
                .uri("/list")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Product.class)
                .isEqualTo(productList);
    }

    @Test
    void testGetProductById() {
        Product product = productList.get(1);
        client.get()
                .uri("/{id}", product.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Product.class)
                .isEqualTo(product);
    }

    @Test
    void testGetProductEvent() {
        ProductEvent event = new ProductEvent(1L, "First Product Event");

        FluxExchangeResult<ProductEvent> result = client.get().uri("/event")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(ProductEvent.class);

        StepVerifier.create(result.getResponseBody())
                .expectNext(event)
                .expectNextCount(2)
                .consumeNextWith(e -> assertEquals(Long.valueOf(3), e.getEventId()))
                .thenCancel()
                .verify();
    }
}
