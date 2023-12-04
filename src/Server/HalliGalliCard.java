package Server;

import java.io.Serializable;

public class HalliGalliCard{
    public enum Fruit {
        BANANA,
        PLUM,
        LEMON,
        STRAWBERRY;

        public static Fruit getByIndex(int index) {
            return values()[index];
        }
    }

    private Fruit fruit;
    private int number;

    public HalliGalliCard(Fruit fruit, int number) {
        this.fruit = fruit;
        this.number = number;
    }

    public Fruit getFruit() {
        return fruit;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return fruit + "\\" + number;
    }
}