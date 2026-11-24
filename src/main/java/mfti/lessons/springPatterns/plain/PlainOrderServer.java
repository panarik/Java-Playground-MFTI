package mfti.lessons.springPatterns.plain;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Сравнение со Spring: тот же заказ, но без контейнера и без отдельных бинов. Порт 8080.
 * Те же тела на порту 8081 принимает Spring: там отделы — разные бины, и смешанный запрос не затирается.
 * Два отдела дописывали один обработчик. Логика обоих живёт в {@link OrderHandler}.
 */
public class PlainOrderServer {

    /**
     * Поднять голый сервер из JDK и ждать запросы.
     * Нужен, чтобы на занятии открыть Postman и показать связанный код вживую.
     * Spring эти классы не создаёт: пакет другой, аннотаций нет.
     */
    public static void main(String[] args) throws IOException {

        // Одной строкой создаем сервер.
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        // Объект, который будет принимать запросы. Сервер хранит его и на каждый POST вызывает handler.handle.
        OrderHandler handler = new OrderHandler();

        // Присваиваем ручку.
        server.createContext("/order", handler);

        // Запускаем сервер.
        server.start();
        System.out.println("ждём POST http://localhost:8080/order");
    }
}
