package Server;

import Common.MessageTag;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

//*******************************************************************
// Name : GameRoom
// Type : Class
// Description : 게임이 진행되는 방을 나타내는 클래스
//               플레이어의 방 생성, 입장 드의 기능 구현.
//               Tag 단위로 들어오는 메시지를 처리.
//******************************************************************

public class GameRoom extends Thread{
    HalliGalliDeck deck; //처음 시작할 때 카드들
    List<HalliGalliCard> card_underbell; //종 밑에 있는 카드

    List<SCGameUser> allGu;   //게임 룸에 들어와있는 모든 유저
    List<SCGameUser> aliveGu; //

    String title;   //방의 이름
    int numofUser;  //방에 있는 모든 사람
    int aliveUser;  //방에 있는 살아있는 사람

    boolean isDrawCard = false;
    boolean isPushBell = false;
    int eventPlayerorder = 0;


    GameRoom(List<SCUser> lscu, String title, int numofUser) {
        this.title = title;
        this.numofUser = numofUser;
        this.aliveUser = numofUser;

        allGu = Collections.synchronizedList(new ArrayList<>());
        aliveGu = Collections.synchronizedList(new ArrayList<>());

        for(SCUser scu : lscu) {
            scu.myGRoom = this;
            scu.gamestart = true;

            allGu.add(new SCGameUser(scu, this));
        }

        Collections.shuffle(allGu);

        String msg = MessageTag.START+"//";

        for(SCGameUser gu : allGu){
            msg += gu.nickname + "@@";
        }

        SendAllUser(msg);

        deck = new HalliGalliDeck();
        deck.initializeDeck();
        deck.shuffleDeck();

        card_underbell = Collections.synchronizedList(new ArrayList<>());

        // 각 사용자들에게 카드 나눠줌
        if(numofUser == 2){
            for(SCGameUser gu : allGu){
                for(int i=0;i<10;i++){
                    gu.hand.add(deck.drawCard());
                }
            }
        }

        if(numofUser == 3){
            for(SCGameUser gu : allGu){
                for(int i=0;i<18;i++){
                    gu.hand.add(deck.drawCard());
                }
            }
            HalliGalliCard hc;
            while((hc = deck.drawCard()) != null)
                card_underbell.add(hc);
        }

        if(numofUser == 4){
            for(SCGameUser gu : allGu){
                for(int i=0;i<14;i++){
                    gu.hand.add(deck.drawCard());
                }
            }
        }

        //모든 게임유저에 대해 쓰레드를 시작
        for(SCGameUser gu : allGu) {
            aliveGu.add(gu);

            gu.start();
        }
    }

    @Override
    public void run() {
        int order = 0;
        String msg;

        synchronized (this) {
            try {
                while (true) {

                    msg = MessageTag.CTURN + "//" + order % aliveUser + "//" + aliveGu.get(order % aliveUser).nickname;
                    SendAllUser(msg);

                    SendAllUser(FloorCards());

                    SendAllUser(RemainCards());

                    while (true) {
                        this.sleep(10);

                        if (isDrawCard) {
                            System.out.println("DrawCard");
                            msg = MessageTag.DCARD + "//" + allGu.get(eventPlayerorder).nickname;
                            aliveGu.get(eventPlayerorder).drawHandCard();

                            SendAllUser(msg);
                            order++;

                            isDrawCard = false;
                            break;
                        }

                        this.sleep(10);

                        if (isPushBell) {
                            msg = MessageTag.PBELL + "//" + aliveGu.get(eventPlayerorder).nickname + "//";

                            if (isSuccessBell()) {
                                msg += MessageTag.SBELL + "";
                                for (SCGameUser gu : aliveGu) {
                                    while (!gu.floor.isEmpty())
                                        aliveGu.get(eventPlayerorder).hand.add(gu.floor.remove(0));
                                }
                                while (!card_underbell.isEmpty())
                                    aliveGu.get(eventPlayerorder).hand.add(card_underbell.remove(0));

                                Collections.shuffle(aliveGu.get(eventPlayerorder).hand);
                            } else {
                                msg += MessageTag.FBELL + "";
                                card_underbell.add(aliveGu.get(eventPlayerorder).hand.remove(0));
                            }

                            SendAllUser(msg);
                            order = eventPlayerorder;

                            //종이 울린 후 죽은 플레이어가 있는지 확인
                            msg = MessageTag.GDEAD + "//";
                            Iterator<SCGameUser> iterator = aliveGu.iterator();
                            while (iterator.hasNext()) {
                                SCGameUser gu = iterator.next();
                                if (gu.hand.isEmpty()) {
                                    msg += gu.nickname + "@@";

                                    iterator.remove();
                                    aliveUser--;
                                }
                            }
                            //죽은 플레이어가 있을 때 메시지를 보냄
                            if(msg.split("//").length >= 2)
                                SendAllUser(msg);

                            SendAllUser(FloorCards());
                            SendAllUser(RemainCards());

                            Thread.sleep(3000);

                            isPushBell = false;
                            break;
                        }
                    }

                    if(aliveUser == 1) {
                        msg = MessageTag.GEND +"";
                        SendAllUser(msg);

                        this.interrupt();
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("[Server] " + title + "Game 종료");
            }
        }
    }

    String FloorCards(){
        String msg = MessageTag.FCARD + "//";
        for(SCGameUser gu : allGu){
            if(gu.GetTopofFloor() == null)
                msg+="null@@";
            else
                msg+=gu.GetTopofFloor()+"@@";
        }
        return msg;
    }

    String RemainCards(){
        String msg = MessageTag.CCRAD + "//";
        for(SCGameUser gu : allGu){
            msg+=gu.hand.size()+"@@";
        }
        msg+=card_underbell.size();

        return msg;
    }

    boolean isSuccessBell(){
        int[] countCard = new int[4];

        for (SCGameUser gu : allGu) {
            if (!gu.floor.isEmpty()) {
                HalliGalliCard hc = gu.floor.get(gu.floor.size() - 1);
                countCard[hc.getFruit().ordinal()] += hc.getNumber();
            }
        }

        for(int cnt : countCard){
            if(cnt == 5)
                return true;
        }

        return false;
    }

    void SendAllUser(String msg){
        try {
            for (SCGameUser gameUser : allGu) {
                gameUser.dos.writeUTF(msg);
            }
        } catch (IOException e){
            System.out.println();
        }
    }

    void SendAliveUser(String msg){
        try {
            for (SCGameUser gameUser : aliveGu) {
                gameUser.dos.writeUTF(msg);
            }
        } catch (IOException e){
            System.out.println();
        }
    }
}
