import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class WebClientAPI {
    private WebClient webClient;

    public WebClientAPI () {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8085/api/product")
                .build();
    }

    public static void main(String args[]) {
        WebClientAPI api = new WebClientAPI();

        api.getAllProduct()
                .thenMany(api.createProduct("Chivas", 45.50))
                .thenMany(api.getAllProduct())
                .take(1)
                .flatMap(p1 -> api.updateProduct(p1.getId(), "Coca Cola", 3.25))
                .thenMany(api.getAllProduct())
                .take(1)
                .flatMap(p1 -> api.deleteProduct(p1.getId()))
                .thenMany(api.getAllProduct())
                .thenMany(api.getAllEvent())
                .subscribeOn(Schedulers.newSingle("myThread"))
                .subscribe(System.out::println);

        /*try {
            Thread.sleep(5000);
        } catch (Exception e) {}*/
    }

    private Mono<ResponseEntity<Product>> createProduct(String name, double price) {
        return webClient
                .post()
                .uri("/add")
                .body(Mono.just(new Product(null, name, price)), Product.class)
                .exchangeToMono(response -> response.toEntity(Product.class))
                .doOnSuccess(obj -> System.out.println("--- POST:" + obj));
    }

    private Flux<Product> getAllProduct() {
        return webClient
                .get()
                .uri("/list")
                .retrieve()
                .bodyToFlux(Product.class)
                .doOnNext(obj -> System.out.println("--- GET:" + obj));
    }

    private Mono<Product> updateProduct(String id, String name, double price) {
        System.out.println("update id = " + id);
        return webClient
                .put()
                .uri("/{id}", id)
                .body(Mono.just(new Product(null, name, price)), Product.class)
                .retrieve()
                .bodyToMono(Product.class)
                .doOnSuccess(obj -> System.out.println("--- PUT:" + obj));
    }

    private Mono<Void> deleteProduct(String id) {
        System.out.println("delete id = " + id);
        return webClient
                .delete()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(obj -> System.out.println("--- DELETE:" + obj));
    }

    private Flux<ProductEvent> getAllEvent() {
        return webClient
                .get()
                .uri("/event")
                .retrieve()
                .bodyToFlux(ProductEvent.class);
    }
}
