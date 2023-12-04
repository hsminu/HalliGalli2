package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//*******************************************************************
// Name : HGServerMain
// Type : Class
// Description : 할리갈리 게임 서버의 메인 클래스
//******************************************************************

public class HGServerMain {
    ServerSocket ss = null;  // 들어오는 연결을 수락하는 ServerSocket

    List<SCUser> allUsers;   // 모든 연결된 사용자를 저장하는 리스트
    List<SCUser> waitUsers;  // 게임 시작을 기다리는 사용자를 저장하는 리스트

    List<Room> Rooms;        // 활성화된 게임 방을 저장하는 리스트

    private final int PORT = 12345;  // 서버가 수신 대기 중인 포트 번호

    // 서버 시작 메인 메서드
    public static void main(String[] args){
        HGServerMain server = new HGServerMain();

        // 동시성을 처리하기 위한 동기화된 리스트
        server.allUsers = Collections.synchronizedList(new ArrayList<>());
        server.waitUsers = Collections.synchronizedList(new ArrayList<>());
        server.Rooms = Collections.synchronizedList(new ArrayList<>());

        try{
            server.ss = new ServerSocket(server.PORT);
            System.out.println("[Server] 서버 소켓이 준비되었습니다.");

            // 서버는 계속해서 들어오는 연결을 수락
            while(true){
                Socket socket = server.ss.accept();
                SCUser c = new SCUser(socket, server);

                // 각 연결된 사용자에 대해 새로운 스레드를 시작
                c.start();
            }
        } catch (SocketException e){
            System.out.println("[Server] 서버 소켓 오류 > " + e.toString());
        } catch (IOException e){
            System.out.println("[Server] 입출력 오류 > " + e.toString());
        }
    }
}
