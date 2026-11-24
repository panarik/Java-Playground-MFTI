package mfti.lessons.springPatterns.beans.department;

import mfti.lessons.springPatterns.beans.notification.Notifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Книжный отдел. Свой бин, свой класс.
 * Слово «книга» написано только здесь. Контроллер этот класс не импортирует.
 */
@Component
@Order(1)
public class BookDepartment implements Department {

    private final Notifier notifier;

    public BookDepartment(Notifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public boolean matches(String body) {
        return body.contains("книга");
    }

    @Override
    public String place(String body) {
        notifier.send("книжный отдел: " + body);
        return "принят в книжный: " + body;
    }
}
