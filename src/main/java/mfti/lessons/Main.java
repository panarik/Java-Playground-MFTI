package mfti.lessons;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("Начинаем!");
        Thread.sleep(1000);

        System.out.println("Работа со строками");
        new TextComposer().print1("вывод текста");

        new MathComposer().sum1(1, 2);
        new TextComposer().print1(new MathComposer().sum2(1, 2));
        new MathComposer().sum2(1, 2);
    }

}