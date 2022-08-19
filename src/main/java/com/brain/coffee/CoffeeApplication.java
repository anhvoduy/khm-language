package com.brain.coffee;

import com.brain.coffee.handler.ProductHandler;
import com.brain.coffee.model.Product;
import com.brain.coffee.repository.IProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class CoffeeApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoffeeApplication.class, args);
	}

	@Bean
	CommandLineRunner init(ReactiveMongoOperations operations, IProductRepository repository) {
		return args -> {
			Flux<Product> productFlux = Flux.just(
					new Product(null, "Ice Tea", 2.55),
					new Product(null, "Cafe Latte", 4.80),
					new Product(null, "Lemon Tree", 1.55))
					.flatMap(repository::save);

			productFlux
					.thenMany(repository.findAll())
					.subscribe(System.out::println);

			/*
			operations.collectionExists(Product.class)
					.flatMap(exists -> exists ? operations.dropCollection(Product.class): Mono.just(exists))
					.thenMany(v -> operations.createCollection(Product.class))
					.thenMany(productFlux)
					.thenMany(repository.findAll())
					.subscribe(System.out::println);
			*/
		};
	}

	@Bean
	RouterFunction<ServerResponse> routes(ProductHandler handler) {
		/*
		return route()
				.GET("product/event", accept(MediaType.TEXT_EVENT_STREAM), handler::getProductEvents)
				.GET("product/{id}", accept(MediaType.APPLICATION_JSON), handler::getById)
				.GET("product/list", accept(MediaType.APPLICATION_JSON), handler::getAll)
				.PUT("product/{id}", accept(MediaType.APPLICATION_JSON), handler::update)
				.POST("product/add", accept(MediaType.APPLICATION_JSON), handler::create)
				.DELETE("product/{id}", accept(MediaType.APPLICATION_JSON), handler::delete)
				.DELETE("product/all", accept(MediaType.APPLICATION_JSON), handler::deleteAll)
				.build();
		 */
		return route()
				.path("/product",
						builder -> builder.nest(accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON)).or(accept(TEXT_EVENT_STREAM)),
						nestedBuilder -> nestedBuilder
								.GET("/event", handler::getProductEvents)
								.GET("/{id}", handler::getById)
								.GET("/list", handler::getAll)
								.PUT("/{id}", handler::update)
								.POST("/{id}", handler::create)
						)
								.DELETE("/{id}", handler::delete)
								.DELETE("/all", handler::deleteAll)
				)
				.build();
	}
}
