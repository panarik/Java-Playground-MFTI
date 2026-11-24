package mfti.spring.beans;

/**
 * Контракт «уведомить пользователя».
 * Верхний уровень ({@link OrderService}) зависит от этого интерфейса.
 * Конкретный способ отправки зависит от него же и подставляется контейнером.
 */
public interface Notifier {

    void send(String message);
}
