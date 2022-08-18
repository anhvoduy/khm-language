import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public class MonoTest {

    @Test
    void firstMono() {
        Mono.just("A");
    }

    @Test
    void monoWithConsumer() {
        Mono.just("A")
                .log()
                .subscribe(s -> System.out.println("s:" + s));
    }

    @Test
    void monoWithDoOn() {
        Mono.just("A")
                .log()
                .doOnSubscribe(sub -> System.out.println("Subscribed:" + sub))
                .doOnRequest(req -> System.out.println("Request:" + req))
                .doOnSuccess(complete -> System.out.println("Complete:" + complete))
                .subscribe(System.out::println);
    }

    @Test
    void monoEmpty() {
        Mono.empty()
                .log()
                .subscribe(System.out::println);
    }

    @Test
    void monoEmptyCompleteConsumer() {
        Mono.empty()
                .log()
                .subscribe(System.out::println, null, () -> System.out.println("Done"));
    }
     @Test
    void monoErrorRuntimeException() {
        Mono.error(new Exception())
                .doOnError(e -> System.out.println("Error:" + e))
                .log()
                .subscribe();
    }

    @Test
    void monoErrorOnErrorResume() {
        Mono.error(new Exception())
                .onErrorResume(e -> {
                    System.out.println("Caught:" + e);
                    return  Mono.just("B");
                })
                .log()
                .subscribe();
    }
}
