package mfti.lessons.springPatterns.beans.demo;

import mfti.lessons.springPatterns.beans.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Контейнер вызывает {@link #run} сам, после того как создал все бины.
 * {@code main} этот метод не вызывает.
 */
@Component
public class DemoRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoRunner.class);

    private static final int THREADS = 8;
    private static final int PER_THREAD = 20;

    private final OrderService orders;
    private final SharedCounter counter;

    public DemoRunner(OrderService orders, SharedCounter counter) {
        this.orders = orders;
        this.counter = counter;
        log.info("контейнер создал DemoRunner, объект {}", System.identityHashCode(this));
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("--- заказ через внедрённый Notifier ---");
        orders.place("книга");

        log.info("--- один бин SharedCounter, объект {} ---", id(counter));
        runWave("без синхронизации", counter::addUnsafe);
        runWave("с синхронизацией", counter::addSafe);
    }

    private void runWave(String title, Runnable action) throws InterruptedException {
        counter.reset();
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        for (int i = 0; i < THREADS; i++) {
            pool.submit(() -> {
                log.info(
                        "поток {} работает с тем же бином {}",
                        Thread.currentThread().getName(),
                        id(counter)
                );
                for (int n = 0; n < PER_THREAD; n++) {
                    action.run();
                }
            });
        }
        pool.shutdown();
        if (!pool.awaitTermination(2, TimeUnit.MINUTES)) {
            throw new IllegalStateException("потоки не завершились");
        }
        int expected = THREADS * PER_THREAD;
        log.info(
                "{}: потоков {}, прибавлений на поток {}, ожидаем {}, в бине {}",
                title,
                THREADS,
                PER_THREAD,
                expected,
                counter.value()
        );
    }

    private static String id(Object bean) {
        return bean.getClass().getSimpleName() + "@" + System.identityHashCode(bean);
    }
}
