package Server;

import Common.MessageTag;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//*******************************************************************
// Name : GameRoom
// Type : Class
// Description : 게임이 진행되는 방을 나타내는 클래스
//               플레이어의 방 생성, 입장 드의 기능 구현.
//               Tag 단위로 들어오는 메시지를 처리.
//******************************************************************

public class GameRoom extends Thread{

    HalliGalliDeck deck;
    List<HalliGalliCard> card_underbell;

    List<GameUser> allGu;
    List<GameUser> aliveGu;
    List<GameUser> deadGu;

    String title;
    int numofUser;
    int aliveUser;

    boolean isDrawCard = false;
    boolean isPushBell = false;
    int eventPlayerorder = 0;


    GameRoom(List<SCUser> lscu, String title, int numofUser) {
        this.title = title;
        this.numofUser = numofUser;
        this.aliveUser = numofUser;

        allGu = Collections.synchronizedList(new ArrayList<>());
        deadGu = Collections.synchronizedList(new ArrayList<>());
        aliveGu = Collections.synchronizedList(new ArrayList<>());

        for(SCUser scu : lscu) {
            scu.myGRoom = this;
            scu.gamestart = true;

            allGu.add(new GameUser(scu));
        }

        Collections.shuffle(allGu);
        String msg = MessageTag.START+"//";

        for(GameUser gu : allGu){
            msg += gu.nickname + "@@";
        }
        SendAllUser(msg);

        deck = new HalliGalliDeck();
        deck.initializeDeck();
        deck.shuffleDeck();
        card_underbell = Collections.synchronizedList(new ArrayList<>());

        // 각 사용자들에게 카드 나눠줌
        if(numofUser == 2){
            for(GameUser gu : allGu){
                for(int i=0;i<28;i++){
                    gu.hand.add(deck.drawCard());
                }
            }
        }

        if(numofUser == 3){
            for(GameUser gu : allGu){
                for(int i=0;i<18;i++){
                    gu.hand.add(deck.drawCard());
                }
            }
            HalliGalliCard hc;
            while((hc = deck.drawCard()) != null)
                card_underbell.add(hc);
        }

        if(numofUser == 4){
            for(GameUser gu : allGu){
                for(int i=0;i<14;i++){
                    gu.hand.add(deck.drawCard());
                }
            }
        }

        for(GameUser gu : allGu) {
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
                                for (GameUser gu : aliveGu) {
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

                            msg = MessageTag.GDEAD + "//";
                            for (GameUser gu : aliveGu) {
                                if (gu.hand.isEmpty()) {
                                    msg += gu.nickname + "@@";

                                    aliveGu.remove(gu);
                                    deadGu.add(gu);
                                    aliveUser = aliveGu.size();
                                }
                            }
                            SendAllUser(msg);

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
                System.out.println("[Server] " + title + " 종료");
            }
        }
    }

    String FloorCards(){
        String msg = MessageTag.FCARD + "//";
        for(GameUser gu : allGu){
            if(gu.GetTopofFloor() == null)
                msg+="null@@";
            else
                msg+=gu.GetTopofFloor()+"@@";
        }
        return msg;
    }

    String RemainCards(){
        String msg = MessageTag.CCRAD + "//";
        for(GameUser gu : allGu){
            msg+=gu.hand.size()+"@@";
        }
        msg+=card_underbell.size();

        return msg;
    }

    boolean isSuccessBell(){
        int[] countCard = new int[4];

        for (GameUser gu : allGu) {
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
            for (GameUser gameUser : allGu) {
                gameUser.dos.writeUTF(msg);
            }
        } catch (IOException e){
            System.out.println();
        }
    }

    void SendAliveUser(String msg){
        try {
            for (GameUser gameUser : aliveGu) {
                gameUser.dos.writeUTF(msg);
            }
        } catch (IOException e){
            System.out.println();
        }
    }

    class GameUser extends Thread{
        HGServerMain server;
        Socket socket;

        SCUser scu;

        OutputStream os;
        DataOutputStream dos;
        InputStream is;
        DataInputStream dis;

        String msg;            //수신 메시지를 저장할 필드
        String nickname;       //클라이언트의 닉네임을 저장할 필드

        List<HalliGalliCard> hand;
        List<HalliGalliCard> floor;

        GameUser(SCUser scu){
            this.scu = scu;
            this.socket = scu.socket;
            this.server = scu.server;
            this.nickname = scu.nickname;

            super.setName(this.nickname + "Thread");

            hand = Collections.synchronizedList(new ArrayList<>());
            floor = Collections.synchronizedList(new ArrayList<>());

            try{
                os = this.socket.getOutputStream();
                dos = new DataOutputStream(os);
                is = this.socket.getInputStream();
                dis = new DataInputStream(is);
            } catch (IOException e){
                System.out.println(e.toString());
            }
        }

        public String GetTopofFloor(){
            if(!floor.isEmpty())
                return floor.get(floor.size()-1).toString();
            else
                return null;
        }

        public boolean drawHandCard() {
            if(!hand.isEmpty()){
                floor.add(hand.remove(0));
                return true;
            }
            else return false;
        }

        @Override
        public void run() {
            try {
                synchronized (this) {
                    while (true) {
                        msg = dis.readUTF();
                        String[] m = msg.split("//");

                        if (m[0].equals(MessageTag.PBELL + "")) {
                            isPushBell = true;
                            eventPlayerorder = Integer.parseInt(m[1]);
                        }

                        if (m[0].equals(MessageTag.DCARD + "")) {
                            isDrawCard = true;
                            eventPlayerorder = Integer.parseInt(m[1]);
                        }

                        if(m[0].equals(MessageTag.GEXIT + "")){
                            allGu.remove(this);

                            this.scu.gamestart = false;
                            this.scu.notify();
                            this.interrupt();
                        }

                        this.sleep(10);
                    }
                }
            } catch (IOException e){
                System.out.println(e.toString());
            } catch (InterruptedException e){
                ;
            }
        }
    }
}
