package mfti.spring.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Бизнес-операция. Зависит от {@link Notifier}, а не от {@link EmailNotifier}:
 * в этом файле нет импорта конкретной реализации.
 * Контейнер сам находит бин нужного типа и передаёт его в конструктор.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final Notifier notifier;

    public OrderService(Notifier notifier) {
        this.notifier = notifier;
        log.info(
                "контейнер создал OrderService и положил в него {}, объект {}",
                notifier.getClass().getSimpleName(),
                System.identityHashCode(notifier)
        );
    }

    public void place(String item) {
        log.info("оформляем заказ «{}». Сервис обращается к интерфейсу Notifier", item);
        notifier.send("заказ оформлен: " + item);
    }
}
