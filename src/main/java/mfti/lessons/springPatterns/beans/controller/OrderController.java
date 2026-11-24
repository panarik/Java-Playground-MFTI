package mfti.lessons.springPatterns.beans.controller;

import mfti.lessons.springPatterns.beans.department.Department;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Тот же POST /order, что у голого сервера на порту 8080. Этот слушает 8081.
 * Слов «книга» и «бульдозер» здесь нет: контроллер обходит бины {@link Department}.
 * На «книга про бульдозер» в ответ попадут оба отдела. Никто не перезаписывает чужую строку.
 */
@RestController
public class OrderController {

    // Список бинов Department. Его собрал контейнер при старте и положил в конструктор.
    // Сейчас здесь BookDepartment и ConstructionDepartment. Контроллер их по имени не знает.
    private final List<Department> departments;

    public OrderController(List<Department> departments) {
        this.departments = departments;
    }

    // Tomcat принял POST /order и отдал его Spring.
    // @PostMapping говорит: тело запроса положи в аргумент body, строку return отдай клиенту как текст.
    @PostMapping(value = "/order", consumes = "text/plain", produces = "text/plain;charset=UTF-8")
    public String order(@RequestBody String body) {

        // @RequestBody уже вытащил байты тела из HTTP и сделал из них String.
        String text = body.strip();
        List<String> lines = new ArrayList<>();

        // Обход бинов. В departments лежат объекты, которые Spring создал из классов с @Component.
        for (Department department : departments) {

            // Слово «книга» или «бульдозер» проверяет сам бин. Контроллер текст запроса только передаёт.
            if (department.matches(text)) {

                // place снова получает тот же текст. Бин шлёт письмо и возвращает свою строку ответа.
                lines.add(department.place(text));
            }
        }
        if (lines.isEmpty()) {
            return "отдел не найден";
        }
        // Spring берёт эту строку и пишет её в HTTP-ответ. Для «книга про бульдозер» здесь две строки.
        return String.join("\n", lines);
    }
}
