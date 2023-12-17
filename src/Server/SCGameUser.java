package Server;

import Common.MessageTag;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SCGameUser extends Thread{
    HGServerMain server;
    Socket socket;

    SCUser scu;
    GameRoom myGRoom;

    OutputStream os;
    DataOutputStream dos;
    InputStream is;
    DataInputStream dis;

    String msg;            //수신 메시지를 저장할 필드
    String nickname;       //클라이언트의 닉네임을 저장할 필드

    List<HalliGalliCard> hand;
    List<HalliGalliCard> floor;

    SCGameUser(SCUser scu, GameRoom gameRoom){
        this.scu = scu;
        this.socket = scu.socket;
        this.server = scu.server;
        this.nickname = scu.nickname;
        this.myGRoom = gameRoom;

        super.setName(this.nickname + "Thread");

        hand = Collections.synchronizedList(new ArrayList<>());
        floor = Collections.synchronizedList(new ArrayList<>());

        try {
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

                    //벨이 눌렸을 때
                    if (m[0].equals(MessageTag.PBELL + "")) {
                        myGRoom.isPushBell = true;
                        myGRoom.eventPlayerorder = Integer.parseInt(m[1]);
                    }

                    //카드를 뽑았을 때
                    if (m[0].equals(MessageTag.DCARD + "")) {
                        myGRoom.isDrawCard = true;
                        myGRoom.eventPlayerorder = Integer.parseInt(m[1]);
                    }

                    //방을 나갔을떄
                    if(m[0].equals(MessageTag.GEXIT + "")){
                        myGRoom.allGu.remove(this);

                        synchronized (this.scu) {
                            this.scu.gamestart = false;
                            this.scu.notify();
                        }

                        this.interrupt();
                    }

                    this.sleep(10);
                }
            }
        } catch (IOException e){
            System.out.println(e.toString());
        } catch (InterruptedException e){
            System.out.println("[Server]" + this.nickname + "Game exit");
        }
    }
}
