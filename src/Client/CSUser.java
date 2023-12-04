package Client;

import Common.MessageTag;

import java.io.*;
import java.net.Socket;

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

    Thread reciveThread;


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
            client.display("JF_WaitRoom");
        } catch (IOException e){
            System.out.println("[Client] 연결 실패 > " + e.toString());
            throw e;
        }

        reciveThread = new Thread(()->{
            try{
                while(true) {
                    String msg = dis.readUTF();
                    String[] m = msg.split("//");

                    //방 목록
                    if(m[0].equals(MessageTag.VROOM+"")){
                        String[] rooms = m[1].split("@@");
                        client.jf_waitRoom.Model_RoomList.clear();
                        for(String room : rooms){
                            client.jf_waitRoom.Model_RoomList.addElement(room);
                        }
                    }

                    //접속 유저
                    if(m[0].equals(MessageTag.CUSER+"")){
                        String[] users = m[1].split("@@");
                        client.jf_waitRoom.Model_PlayerList.clear();
                        for(String user :users){
                            client.jf_waitRoom.Model_PlayerList.addElement(user);
                        }
                    }

                    //방 유저
                    if(m[0].equals(MessageTag.UROOM+"")){
                        String[] users = m[1].split("@@");
                        client.jf_readyRoom.JT_Players.setText("");
                        for(String user : users){
                            client.jf_readyRoom.JT_Players.append(user+"\n");
                        }

                        if(client.jf_readyRoom.isHead){
                            if(m[2].equals(MessageTag.OKAY+""))
                                client.jf_readyRoom.Btn_GameStart.setEnabled(true);
                            else if(m[2].equals(MessageTag.FAIL+""))
                                client.jf_readyRoom.Btn_GameStart.setEnabled(false);

                        }
                    }

                    if(m[0].equals(MessageTag.START+"")){
                        String[] players = m[1].split("@@");
                        client.jf_playGame.Init(players);
                        client.jf_playGame.gameControlThread.start();
                        client.jf_playGame.gameControlThread.join();
                    }
                }
            } catch(IOException e) {
                System.out.println("[Client] 입출력 오류 > " + e.toString());
            } catch (InterruptedException e){
                System.out.println(e.toString());
            }

        });
        reciveThread.start();
    }

    void sendServer(String msg) throws IOException{
        try{
            dos.writeUTF(msg);
        } catch (IOException e){
            System.out.println("[Client] 입출력 오류 > " + e.toString());
            throw e;
        }
    }

}
