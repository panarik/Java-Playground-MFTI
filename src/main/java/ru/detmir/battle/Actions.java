package ru.detmir.battle;

public interface Actions {

    void move();

    void shoot();

    default void rootAction() {
        System.out.println("Действие по умолчанию.");
        System.out.println("Обычно не используется так как антипаттерн");
    }

}
