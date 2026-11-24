package mfti.lessons.springPatterns.beans;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входа. {@code main} только отдаёт управление контейнеру.
 * {@code @SpringBootApplication} включает поиск компонентов в этом пакете и во вложенных.
 */
@SpringBootApplication
public class BeansApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeansApplication.class, args);
    }
}
