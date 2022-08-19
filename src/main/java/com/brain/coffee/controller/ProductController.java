package com.brain.coffee.controller;

import com.brain.coffee.model.Product;
import com.brain.coffee.model.ProductEvent;
import com.brain.coffee.repository.IProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("api/product")
public class ProductController {
    private IProductRepository repository;

    public ProductController(IProductRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/list")
    public Flux<Product> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Product>> getById(@PathVariable(value = "id") String id) {
        return repository.findById(id)
                .map(product -> ResponseEntity.ok(product))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Product> create(@RequestBody Product product) {
        return repository.save(product);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Product>> update(@PathVariable(value = "id") String id, @RequestBody Product product) {
        return repository.findById(id)
                .flatMap(existProduct -> {
                    existProduct.setName(product.getName());
                    existProduct.setPrice(product.getPrice());
                    return repository.save(existProduct);
                })
                .map(updatedProduct -> ResponseEntity.ok(updatedProduct))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable(value = "id") String id) {
        return repository.findById(id)
                .flatMap(existProduct ->
                    repository.delete(existProduct)
                            .then(Mono.just(ResponseEntity.ok().<Void>build()))
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/all")
    public Mono<Void> deleteAll() {
        return repository.deleteAll();
    }

    @GetMapping(value = "/event", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductEvent> getEvents() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(value -> new ProductEvent(value, "PRODUCT_EVENT"));
    }

}
