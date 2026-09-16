package ru.detmir.battle;

import java.util.List;

public class Start {

    public static void main(String[] args) {

        /**
         * Нужно создать объекты:
         * Танк
         * Робот
         *
         * Далее даем команду всем объектам:
         * - move()
         * - shoot()
         * 
         * Далее мы это делаем в цикле while (true) { }
         * - shoot() метод отнимает здоровье юнита health переменная
         * - move() выводит на консоль какое действие юнита
         * - проверка здоровья юнитов.
         *   - если здоровье юнита меньше 1 то break;
         */


        // воздаем мини армию из юнитов

        // По паренту:
        Vehicle tank = new Tank();
        Vehicle robot = new Robot();
        int resultDamage = tank.damage;

        // по интерфейсу
        Actions first = new Tank();
        Actions second = new Robot();

        // тут задаем по отдельности.
        first.move();
        second.move();
        first.rootAction();
        second.rootAction();

        // собираем армию
        List<Actions> army = List.of(first, second);

        // каждому даем приказ
        for (Actions unit : army) {
            unit.move();
            unit.rootAction();
        }

    }
}
