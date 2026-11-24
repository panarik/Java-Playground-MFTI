package mfti.lessons.springPatterns.beans.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Единственная реализация {@link Notifier} в этом проекте.
 * {@code @Component} говорит контейнеру: создай из этого класса бин.
 * Снимите аннотацию и запустите приложение ещё раз.
 * {@code javac} проект соберёт, Spring упадёт на старте: в конструктор {@link mfti.lessons.springPatterns.beans.service.OrderService}
 * нечего положить.
 */
@Component
public class EmailNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(EmailNotifier.class);

    public EmailNotifier() {
        log.info("контейнер создал EmailNotifier, объект {}", System.identityHashCode(this));
    }

    @Override
    public void send(String message) {
        log.info("отправка письма: {}", message);
    }
}
