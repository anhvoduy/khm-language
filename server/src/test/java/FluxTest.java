import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.Random;

public class FluxTest {
    @Test
    void fluxSimple() {
        Flux.just("A", "Z", "B", "C")
                .log()
                .subscribe();
    }

    @Test
    void fluxFromIterable() {
        Flux.fromIterable(Arrays.asList("A", "C", "B", "D"))
                .log()
                .subscribe();
    }

    @Test
    void fluxFromRange() {
        Flux.range(10, 3)
                .log()
                .subscribe();
    }

    @Test
    void fluxFromInterval() throws Exception {
        Flux.interval(Duration.ofSeconds(1))
                .log()
                .take(3)
                .subscribe();
        Thread.sleep(5000);
    }

    @Test
    void fluxRequest() {
        Flux.range(1, 5)
                .log()
                //.subscribe(null, null, null, s -> s.request(3))
                .subscribe(new BaseSubscriber<Integer>() {
                    int elementsToProcess = 3;
                    int counter = 0;

                    public void hookOnSubscribe(Subscription subscription) {
                        //super.hookOnSubscribe(subscription);
                        //subscription.request(3L);
                        System.out.println("Subcribed!!!");
                        request(elementsToProcess);
                    }

                    public void hookOnNext(Integer value) {
                        counter++;
                        if(counter == elementsToProcess) {
                            counter = 0;

                            Random random = new Random();
                            elementsToProcess = random.ints(1, 4)
                                    .findFirst()
                                    .getAsInt();

                            request(elementsToProcess);
                        }
                    }
                });
    }

    @Test
    void fluxLimitRate() {
        Flux.range(1, 5)
                .log()
                .limitRate(3)
                .subscribe();
    }
}
