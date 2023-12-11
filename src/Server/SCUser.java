package Server;

import Common.MessageTag;

import java.io.*;
import java.net.Socket;
import java.util.List;

//*******************************************************************
// Name : SCUser
// Type : Class
// Description : 클라이언트와의 소켓 통신을 담당하는 클래스.
//               플레이어의 방 생성, 입장 드의 기능 구현.
//               Tag 단위로 들어오는 메시지를 처리.
//******************************************************************

public class SCUser extends Thread{
    HGServerMain server;
    Socket socket;

    List<SCUser> allUsers;
    List<SCUser> waitUsers;

    List<Room> Rooms;

    //클라이언트와 서버 간의 데이터 통신을 위한 입출력 스트림을 정의
    OutputStream os;
    DataOutputStream dos;
    InputStream is;
    DataInputStream dis;

    String msg;            //수신 메시지를 저장할 필드
    String nickname;       //클라이언트의 닉네임을 저장할 필드

    Room myRoom;           //현재 입장해있는 방
    GameRoom myGRoom;      //현재 입장해있는 게임 방

    int userstate = 0;     //0:not ready, 1: ready, 2:head
    boolean gamestart = false;

    SCUser(Socket socket, HGServerMain server){
        this.socket = socket;
        this.server = server;

        allUsers = server.allUsers;
        waitUsers = server.waitUsers;

        Rooms = server.Rooms;
    }


    //*******************************************************************
    // Name : run()
    // Type : method
    // Description :  수신한 메시지를 처리
    //                요청 정보를 Tag단위로 파싱하여 각 요청에 대한 기능을 구현
    //*******************************************************************
    @Override
    public void run() {
        try{
            System.out.println("[Server] 클라이언트 접속 > " + this.socket.toString());

            os = this.socket.getOutputStream();
            dos = new DataOutputStream(os);
            is = this.socket.getInputStream();
            dis = new DataInputStream(is);

            while(true) {
                msg = dis.readUTF();

                String[] m = msg.split("//"); //읽은 데이터를 '//'로 파싱

                //유저 접속 요청
                if(m[0].equals(MessageTag.ACCESS+"")){
                    //중복된 이름 확인
                    boolean isOver = false;
                    for(SCUser scu : allUsers){
                        if(scu.nickname.equals(m[1])){
                            dos.writeUTF(MessageTag.ACCESS + "//"+MessageTag.FAIL);
                            isOver = true;
                            break;
                        }
                    }
                    if(isOver)
                        continue;

                    nickname = m[1];

                    synchronized (this) {
                        allUsers.add(this);
                        waitUsers.add(this);
                    }

                    dos.writeUTF(MessageTag.ACCESS + "//"+MessageTag.OKAY); //처리 완료 후 OKAY메시지 전송

                    sendWait(connectedUser()); //새 유저가 들어왔으므로 모든 유저에게 유저 목록 전송
                    if(Rooms.size() > 0){
                        sendWait(roomInfo());  //방이 하나 이상일 경우 모든 유저에게 방 목록을 전송
                    }
                }

                //방 생성 요청
                if(m[0].equals(MessageTag.CROOM+"")){
                    myRoom = new Room(m[1], m[2]); //방 이름, 최대 인원
                    myRoom.playercount++; //

                    synchronized (this) {
                        Rooms.add(myRoom);
                        userstate = 2; //방장

                        myRoom.scu.add(this);
                        waitUsers.remove(this);  //대기 유저에서 삭제
                    }

                    dos.writeUTF(MessageTag.CROOM+"//OKAY");
                    System.out.println("[Server] "+nickname + " : 방 '" + m[1] + "' 생성");

                    sendWait(roomInfo());
                    sendRoom(roomUser());
                }

                //방 접속 요청
                if(m[0].equals(MessageTag.EROOM+"")){ //m[1]
                    synchronized (this) {

                        for (Room room : Rooms) {
                            //방 이름이 같을 때
                            if (room.title.equals(m[1])) {

                                if (room.playercount < room.maxPerson) {
                                    myRoom = room;
                                    myRoom.playercount++;

                                    waitUsers.remove(this);
                                    myRoom.scu.add(this);

                                    sendWait(roomInfo());
                                    sendRoom(roomUser());

                                    dos.writeUTF(MessageTag.EROOM + "//" + MessageTag.OKAY);
                                    System.out.println("[Server] " + nickname + " : 방 '" + m[1] + "' 입장");
                                } else {
                                    dos.writeUTF(MessageTag.EROOM + "//" + MessageTag.FAIL + "::Over Person");
                                    System.out.println("[Server] " + nickname + ":인원 초과. 입장 불가능");
                                }
                            }

                            break;
                        }
                    }
                }

                //방을 나갔을 때
                if(m[0].equals(MessageTag.REXIT+"")){
                    /*
                    Todo: 사용자가 대기방을 나갔을 때 처리
                     */
                }


                //레디 요청
                if(m[0].equals(MessageTag.READY+"")){
                    //userstate 0: not ready, 1: ready
                    this.userstate = this.userstate == 0 ? 1 : 0;

                    myRoom.readycount += this.userstate == 0 ? -1 : 1;

                    sendRoom(roomUser());
                }

                //게임 시작 요청
                if(m[0].equals(MessageTag.START+"")){
                    GameRoom gr = new GameRoom(myRoom.scu, myRoom.title, myRoom.playercount);
                    gr.start();

                    Rooms.remove(myRoom);
                }

                //종료
                if(m[0].equals(MessageTag.PEXIT+"")){
                    disConnect();
                }



                //게임 시작일 때
                if(gamestart) {
                    //이 쓰레드를 대기시킴
                    synchronized (this){
                        this.wait();
                    }

                    //게임이 끝난 후 대기실 정보를 보냄
                    myRoom = null;
                    waitUsers.add(this);

                    sendWait(connectedUser());
                    sendWait(roomInfo());
                }

            }
        } catch (IOException e){
            System.out.println("[Server] 입출력 오류 > " + e.toString());

            //사용자 강제종료
            if(e.toString().equals("Connection reset")){
                disConnect();
            }

        } catch (InterruptedException e){
            System.out.println("[Server] Thread" + this.nickname + " 중지 > " + e.toString());;
        }
    }

    /* 현재 존재하는 방의 목록을 조회하는 메소드 */
    String roomInfo() {
        String msg = MessageTag.VROOM + "//";

        for(Room room : Rooms){
            msg+=room.title + ": " + room.playercount + "/" + room.maxPerson + "@@";
        }

        return msg;
    }

    /* 클라이언트가 입장한 방의 인원과 정보를 조회하는 메소드 */
    String roomUser() {
        String msg = MessageTag.UROOM + "//";

        for(int i=0; i<myRoom.scu.size(); i++) {
            msg += myRoom.scu.get(i).nickname;

            if(myRoom.scu.get(i).userstate == 0)
                msg += " : not ready";
            if(myRoom.scu.get(i).userstate == 1)
                msg+= " : ready";
            if(myRoom.scu.get(i).userstate == 2)
                msg += " : head";

            msg += "@@";
        }

        msg+="//";

        if(myRoom.scu.size() != 1 && myRoom.readycount == myRoom.scu.size())
            msg+=MessageTag.OKAY;
        else
            msg+=MessageTag.FAIL;

        return msg;
    }

    /* 접속한 모든 회원 목록을 조회하는 메소드 */
    String connectedUser() {
        String msg = MessageTag.CUSER + "//";

        for(int i=0; i<allUsers.size(); i++) {
            msg = msg + allUsers.get(i).nickname + "@@";
        }
        return msg;
    }

    /* 대기실에 있는 모든 회원에게 메시지 전송하는 메소드 */
    void sendWait(String m) {
        for(int i=0; i<waitUsers.size(); i++) {
            try {
                waitUsers.get(i).dos.writeUTF(m);
            } catch(IOException e) {
                waitUsers.remove(i--);
            }
        }
    }

    /* 방에 입장한 모든 회원에게 메시지 전송하는 메소드 */
    void sendRoom(String m) {
        for(int i=0; i<myRoom.scu.size(); i++) {
            try {
                myRoom.scu.get(i).dos.writeUTF(m);
            } catch(IOException e) {
                myRoom.scu.remove(i--);
            }
        }
    }

    //클라이언트와의 연결 종료
    void disConnect(){
        try {
            //객체 삭제
            allUsers.remove(this);
            if(waitUsers.contains(this))
                waitUsers.remove(this);

            sendWait(connectedUser());

            // input and output streams 닫음
            if (dos != null) {
                dos.close();
            }
            if (dis != null) {
                dis.close();
            }

            // socket 닫음
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            if(super.isAlive())
                super.interrupt();
        } catch (IOException e) {
            System.out.println("[Client] 연결 종류 오류: " + e.toString());
        }
    }

}
