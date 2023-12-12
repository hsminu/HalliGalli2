package Server;

//**
// Name : HalliGalliCard
// Type : Class
// Description : HalliGalli 게임에서 사용되는 카드를 나타냄.
//*******************************************************************

public class HalliGalliCard {
    /**
     * 카드에 가능한 과일 유형을 나타내는 열거형.
     */
    public enum Fruit {
        BANANA,
        LIME,
        GRAPE,
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
        return fruit + "#" + number;
    }
}