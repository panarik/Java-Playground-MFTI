package mfti.lessons.springPatterns.plain;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Сравнение со Spring: тот же заказ, но без контейнера и без отдельных бинов. Порт 8080.
 * Те же тела на порту 8081 принимает Spring: там отделы — разные бины, и смешанный запрос не затирается.
 * Два отдела дописывали один обработчик. Логика обоих живёт здесь.
 */
public class PlainOrderServer {

    /**
     * Поднять голый сервер из JDK и ждать запросы.
     * Нужен, чтобы на занятии открыть Postman и показать связанный код вживую.
     * Spring этот класс не создаёт: пакет другой, аннотаций нет.
     */
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/order", PlainOrderServer::handle);
        server.start();
        System.out.println("ждём POST http://localhost:8080/order");
    }

    /**
     * Один запрос целиком: прочитать тело, решить отдел, отправить письмо, записать ответ.
     * Нужен как общий код двух отделов. Оба пишут в одну переменную reply.
     * По отдельности каждый запрос проходит. Вместе строительный отдел затирает ответ книжного.
     */
    private static void handle(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).strip();
        String reply = "отдел не найден";

        // Книжный отдел. Покажи запрос с телом «книга».
        // Срабатывает только этот if: одно письмо, ответ «принят в книжный».
        // Отдел проверял только своё слово и считал ветку готовой.
        if (body.contains("книга")) {
            BookDepartment book = new BookDepartment(body);
            new MailClient().send("книжный отдел: " + book.title());
            reply = "принят в книжный: " + book.title();
        }

        // Строительный отдел дописал свой if ниже, в тот же метод и в ту же reply.
        // Покажи запрос «бульдозер»: книжный if молчит, ответ «принят на стройку».
        // Потом покажи «книга про бульдозер». Оба if истинны, писем два,
        // а клиенту уходит только эта ветка: она стоит ниже и перезаписывает reply.
        // Книжный отдел своё условие не трогал. Его ответ стёрла чужая ветка.
        if (body.contains("бульдозер")) {
            ConstructionDepartment site = new ConstructionDepartment(body);
            new MailClient().send("строительный отдел: " + site.machine());
            reply = "принят на стройку: " + site.machine();
        }

        byte[] response = reply.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    /** Объект книжного отдела. Сюда разбирается тело, если в нём есть «книга». Своей логики у объекта нет: она осталась в handle. */
    record BookDepartment(String title) {
    }

    /** Объект строительного отдела. Сюда разбирается тело, если в нём есть «бульдозер». Разбор тоже остался в общем методе. */
    record ConstructionDepartment(String machine) {
    }

    /**
     * Письмо в консоль. Нужно, чтобы на смешанном запросе было видно оба срабатывания:
     * строк «письмо» две, а HTTP-ответ один — последний, записанный в reply.
     */
    static final class MailClient {
        void send(String message) {
            System.out.println("письмо: " + message);
        }
    }
}
