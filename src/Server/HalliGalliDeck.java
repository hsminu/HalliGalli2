package Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//*******************************************************************
// Name : HalliGalliDeck
// Type : Class
// Description : 할리갈리 게임에 필요한 카드 덱을 나타내는 클래스
//******************************************************************

public class HalliGalliDeck {
    private List<HalliGalliCard> deck;

    public void initializeDeck() {
        deck = Collections.synchronizedList(new ArrayList<>());

        addCards(1, 5);
        addCards(2, 3);
        addCards(3, 3);
        addCards(4, 2);
        addCards(5, 1);
    }

    private void addCards(int numberOfFruits, int count) {
        for(int i=0;i<count;i++){
            for(int j=0;j<4;j++)
                deck.add(new HalliGalliCard(HalliGalliCard.Fruit.getByIndex(j), numberOfFruits));
        }
    }

    public void shuffleDeck() {
        Collections.shuffle(deck);
    }

    public HalliGalliCard drawCard() {
        if (!deck.isEmpty()) {
            return deck.remove(0); // 맨 위의 카드를 뽑아서 반환하고 덱에서 제거
        }
        return null; // 덱이 비어있으면 null 반환
    }

}
