package Client;

import Common.MessageTag;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

//*******************************************************************
// Name : CSUser
// Type : Class
// Description : 서버와의 소켓 통신을 담당하는 클래스.
//               로그인 후 소켓, 이름 등 사용자 정보를 담고있음.
//               게임 시작 전 통신을 담당.
//               쓰레드로 Tag 단위로 들어오는 메시지를 처리.
//*******************************************************************

public class CSUser {
    HGClientMain client;
    Socket socket;

    String Name;
    String SERVER_IP;
    int SERVER_PORT = 12345;

    OutputStream os;
    DataOutputStream dos;
    InputStream is;
    DataInputStream dis;

    Thread receiveThread;   //수신 쓰레드


    CSUser(String SERVER_IP, HGClientMain client){
        this.SERVER_IP = SERVER_IP;
        this.client = client;
    }

    void Connect() throws IOException{
        try{
            socket = new Socket(SERVER_IP, SERVER_PORT);

            os = socket.getOutputStream();
            dos = new DataOutputStream(os);
            is = socket.getInputStream();
            dis = new DataInputStream(is);

            System.out.println("[Client] 서버 접속 완료 > "+socket.toString());

        } catch (IOException e){
            System.out.println("[Client] 연결 실패 > " + e.toString());
            throw e;
        }
    }

    boolean isNotOver(String name){
        try {
            dos.writeUTF(MessageTag.OVER+"//"+name);

            String msg = dis.readUTF();
            if(msg.equals(MessageTag.OVER+"//"+MessageTag.OKAY)){
                return true;
            } else if(msg.equals(MessageTag.OVER+"//"+MessageTag.FAIL)){
                return false;
            }
        } catch (IOException e){
            System.out.println("[Client] 입출력 오류 > " + e.toString());
        }

        return false;
    }

    void Join(String name) {

        if(!isNotOver(name))
            return;

        this.Name = name;
        try{
            dos.writeUTF(MessageTag.ACCESS+"//"+this.Name);
        } catch (IOException e){
            System.out.println("[Client] 입출력 오류 > " + e.toString());
        }

        client.jf_robby.setVisible(true);
        client.jf_login.setVisible(false);

        receiveThread = new Thread(() -> {
            try {
                while (true) {
                    String msg = dis.readUTF(); //메시지 수신
                    String[] m = msg.split("//");

                    // 방 목록 업데이트
                    if (m[0].equals(MessageTag.VROOM + "")) {
                        if(m.length > 1) {
                            String[] rooms = m[1].split("@@");
                            client.jf_robby.Model_RoomList.clear();

                            for (String room : rooms) {
                                client.jf_robby.Model_RoomList.addElement(room);
                            }
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

                    if(m[0].equals(MessageTag.CROOM+"")){
                        if(m[1].equals(MessageTag.OKAY+"")){
                            client.jf_robby.setEnabled(false);
                            client.jf_readyRoom.setVisible(true);
                            client.jf_readyRoom.Init(true);
                        } else if(m[1].equals(MessageTag.FAIL+"")){
                            JOptionPane.showMessageDialog(client.jf_robby, "방 중복 확인", "경고", JOptionPane.WARNING_MESSAGE);
                        }
                    }

                    //방 입장 성공 여부
                    if(m[0].equals(MessageTag.EROOM+"")){
                        if(m[1].equals(MessageTag.OKAY+"")){
                            client.jf_robby.setEnabled(false);
                            client.jf_readyRoom.setVisible(true);
                            client.jf_readyRoom.Init(false);
                        } else{
                            JOptionPane.showMessageDialog(client.jf_robby, "방 인원 초과", "경고", JOptionPane.WARNING_MESSAGE);
                        }
                    }

                    // 게임 시작
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

    //서버와의 연결 종료
    void disConnect() {
        try {
            dos.writeUTF(MessageTag.PEXIT + "");
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

            // receiveThread 종료
            if (receiveThread != null && receiveThread.isAlive()) {
                receiveThread.interrupt();
            }

        } catch (IOException e) {
            System.out.println("[Client] 연결 종류 오류: " + e.toString());
        }
    }

}
