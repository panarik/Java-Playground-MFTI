package mfti.lessons.basics;

public class TextComposer {


    // вывод строки
    public void print1(String input) {
        printRoot(input);
    }

    // вывод в консоль пример 2 printf
    public void print2() {

        // доп логика
        printRoot("результат доп логики");

    }

    // контроллер
    private void printRoot(String input) {

        // вся логика
        System.out.printf("вывод контроллера: %s\n", input);
    }

}
