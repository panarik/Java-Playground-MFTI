package mfti.spring.beans;

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

    private final List<Department> departments;

    public OrderController(List<Department> departments) {
        this.departments = departments;
    }

    @PostMapping(value = "/order", consumes = "text/plain", produces = "text/plain;charset=UTF-8")
    public String order(@RequestBody String body) {
        String text = body.strip();
        List<String> lines = new ArrayList<>();
        for (Department department : departments) {
            if (department.matches(text)) {
                lines.add(department.place(text));
            }
        }
        if (lines.isEmpty()) {
            return "отдел не найден";
        }
        return String.join("\n", lines);
    }
}
