package mfti.lessons.springPatterns.beans.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Один бин на весь контейнер. Все потоки получают эту же ссылку.
 * {@link #addUnsafe()} теряет приращения, {@link #addSafe()} — нет.
 * Пауза между чтением и записью только расширяет окно гонки,
 * чтобы потеря была видна на одном запуске.
 */
@Component
public class SharedCounter {

    private static final Logger log = LoggerFactory.getLogger(SharedCounter.class);

    private int value;

    public SharedCounter() {
        log.info("контейнер создал SharedCounter, объект {}", System.identityHashCode(this));
    }

    public void addUnsafe() {
        int seen = value;
        pause();
        value = seen + 1;
    }

    public synchronized void addSafe() {
        int seen = value;
        pause();
        value = seen + 1;
    }

    public synchronized int value() {
        return value;
    }

    public synchronized void reset() {
        value = 0;
    }

    private static void pause() {
        try {
            Thread.sleep(2);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
