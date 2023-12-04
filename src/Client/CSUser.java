package Client;

import Common.MessageTag;

import java.io.*;
import java.net.Socket;

//*******************************************************************
// Name : CSUser
// Type : Class
// Description : 서버와의 소켓 통신을 담당하는 클래스.
//               게임 시작 전 통신을 담당.
//               Tag 단위로 들어오는 메시지를 처리.
//*******************************************************************

public class CSUser {
    HGClientMain client;
    Socket socket;

    String Name;
    String SERVER_IP;
    int SERVER_PORT;

    OutputStream os;
    DataOutputStream dos;
    InputStream is;
    DataInputStream dis;

    Thread receiveThread;  //수신 쓰레드

    CSUser(String name, String SERVER_IP, int SERVER_PORT, HGClientMain client){
        this.Name = name;
        this.SERVER_IP = SERVER_IP;
        this.SERVER_PORT = SERVER_PORT;
        this.client = client;
    }

    void Connect() throws IOException{
        try{
            socket = new Socket(SERVER_IP, SERVER_PORT);

            os = socket.getOutputStream();
            dos = new DataOutputStream(os);
            is = socket.getInputStream();
            dis = new DataInputStream(is);

            dos.writeUTF(MessageTag.ACCESS +"//"+Name);

            String msg = dis.readUTF();
            if(!msg.equals(MessageTag.ACCESS+"//OKAY"))
                throw new IOException("연결 오류");


            System.out.println("[Client] 서버 접속 완료 > "+socket.toString());

            client.jf_robby.setVisible(true);
            client.jf_login.setVisible(false);
        } catch (IOException e){
            System.out.println("[Client] 연결 실패 > " + e.toString());
            throw e;
        }

        receiveThread = new Thread(() -> {
            try {
                while (true) {
                    String msg = dis.readUTF();
                    String[] m = msg.split("//");

                    // 방 목록 업데이트
                    if (m[0].equals(MessageTag.VROOM + "")) {
                        String[] rooms = m[1].split("@@");
                        client.jf_robby.Model_RoomList.clear();
                        for (String room : rooms) {
                            client.jf_robby.Model_RoomList.addElement(room);
                        }
                    }

                    // 접속 유저 목록 업데이트
                    if (m[0].equals(MessageTag.CUSER + "")) {
                        String[] users = m[1].split("@@");
                        client.jf_robby.Model_PlayerList.clear();
                        for (String user : users) {
                            client.jf_robby.Model_PlayerList.addElement(user);
                        }
                    }

                    // 방 유저 목록 업데이트
                    if (m[0].equals(MessageTag.UROOM + "")) {
                        String[] users = m[1].split("@@");
                        client.jf_readyRoom.JT_Players.setText("");

                        for (String user : users) {
                            client.jf_readyRoom.JT_Players.append(user + "\n");
                        }

                        // 방장 여부 확인 및 게임 시작 버튼 상태 업데이트
                        if (client.jf_readyRoom.isHead) {
                            if (m[2].equals(MessageTag.OKAY + ""))
                                client.jf_readyRoom.Btn_GameStart.setEnabled(true);
                            else if (m[2].equals(MessageTag.FAIL + ""))
                                client.jf_readyRoom.Btn_GameStart.setEnabled(false);
                        }
                    }

                    // 게임 시작 메시지 처리
                    if (m[0].equals(MessageTag.START + "")) {
                        String[] players = m[1].split("@@");
                        client.jf_playGame.Init(players);
                        client.jf_playGame.gameControlThread.start();

                        // 게임 쓰레드가 종료될 때까지 대기
                        client.jf_playGame.gameControlThread.join();
                        System.out.println("awake");
                    }
                }
            } catch (IOException e) {
                System.out.println("[Client] 입출력 오류 > " + e.toString());
            } catch (InterruptedException e) {
                System.out.println(e.toString());
            }
        });
        receiveThread.start();

    }

    /**
     * 서버로 메시지를 전송하는 메서드
     *
     * @param msg 전송할 메시지
     * @throws IOException
     */
    void sendServer(String msg) throws IOException{
        try{
            dos.writeUTF(msg);
        } catch (IOException e){
            System.out.println("[Client] 입출력 오류 > " + e.toString());
            throw e;
        }
    }

}
