package ru.detmir.battle;

public class Tank extends Vehicle implements Actions {

    public int getHealth() {
        return health;
    }

    @Override
    public void move() {
        System.out.println("ползет на гусеницах");
    }

    @Override
    public void shoot() {
        System.out.println("стреляет из пушки");
    }
}
