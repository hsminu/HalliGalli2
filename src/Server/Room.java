package Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//*******************************************************************
// Name : Room
// Type : Class
// Description : 할리갈리 게임 서버의 메인 클래스
//*******************************************************************
public class Room {
    List<SCUser> scu;
    String title;

    int maxPerson;
    int playercount = 0;
    int readycount = 1;

    Room(String title, String maxPerson){
        this.title = title;
        this.maxPerson = Integer.parseInt(maxPerson);
        scu = Collections.synchronizedList(new ArrayList<>());
    }
}
