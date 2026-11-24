package mfti.lessons.springPatterns.beans.department;

import mfti.lessons.springPatterns.beans.notification.Notifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Строительный отдел. Добавлен вторым классом, а не вторым if в чужом методе.
 * На занятии покажи: этот файл не пишет в переменные книжного отдела. Смешанный запрос его не затирает.
 */
@Component
@Order(2)
public class ConstructionDepartment implements Department {

    private final Notifier notifier;

    public ConstructionDepartment(Notifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public boolean matches(String body) {
        return body.contains("бульдозер");
    }

    @Override
    public String place(String body) {
        notifier.send("строительный отдел: " + body);
        return "принят на стройку: " + body;
    }
}
