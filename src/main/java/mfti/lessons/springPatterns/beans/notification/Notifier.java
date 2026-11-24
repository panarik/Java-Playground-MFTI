package mfti.lessons.springPatterns.beans.notification;

/**
 * Контракт «уведомить пользователя».
 * Верхний уровень ({@link mfti.lessons.springPatterns.beans.service.OrderService}) зависит от этого интерфейса.
 * Конкретный способ отправки зависит от него же и подставляется контейнером.
 */
public interface Notifier {

    void send(String message);
}
