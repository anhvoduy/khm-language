package com.brain.coffee.handler;

import com.brain.coffee.model.Product;
import com.brain.coffee.model.ProductEvent;
import com.brain.coffee.repository.IProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class ProductHandler {
    private IProductRepository repository;

    public ProductHandler(IProductRepository repository) {
        this.repository=repository;
    }

    public Mono<ServerResponse> getAll(ServerRequest request) {
        Flux<Product> productFlux = repository.findAll();

        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(productFlux, Product.class);
    }

    public Mono<ServerResponse> getById(ServerRequest request) {
        String id = request.pathVariable("id");

        Mono<Product> productMono = repository.findById(id);

        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return productMono
                .flatMap(product ->
                        ServerResponse.ok()
                                .contentType(APPLICATION_JSON)
                                .body(fromValue(product)))
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        Mono<Product> productMono = request.bodyToMono(Product.class);

        return productMono.flatMap(product ->
                ServerResponse.status(HttpStatus.CREATED)
                        .contentType(APPLICATION_JSON)
                        .body(repository.save(product), Product.class));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("id");

        Mono<Product> existProductMono = repository.findById(id);
        Mono<Product> productMono = request.bodyToMono(Product.class);

        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return productMono.zipWith(existProductMono,
                (product, existProduct) ->
                        new Product(existProduct.getId(), existProduct.getName(), existProduct.getPrice())
        ).flatMap(product -> ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(repository.save(product), Product.class)
        ).switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");

        Mono<Product> productMono = repository.findById(id);

        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return productMono
                .flatMap(product -> ServerResponse.ok().build(repository.delete(product)))
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> deleteAll(ServerRequest request) {
        return ServerResponse.ok().build(repository.deleteAll());
    }

    public Mono<ServerResponse> getProductEvents(ServerRequest request) {
        Flux<ProductEvent> eventFlux = Flux.interval(Duration.ofSeconds(1))
                .map(value -> new ProductEvent(value, "PRODUCT_EVENT"));

        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(eventFlux, ProductEvent.class);
    }
}
