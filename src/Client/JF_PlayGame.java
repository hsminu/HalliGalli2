package Client;

import Common.MessageTag;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

//*******************************************************************
// Name : JF_PlayGame
// Type : Class
// Description : 할리갈리 게임 프레임
//               서버에서 메시지를 수신해 처리
//*******************************************************************

public class JF_PlayGame extends JFrame {
    HGClientMain client;
    Socket socket;

    JPanel contentPane;

    OutputStream os;
    DataOutputStream dos;
    InputStream is;
    DataInputStream dis;

    List<JL_PlayersCard> btnPlayers;
    List<JLabel> Lb_PlayersCardCount;

    JButton Btn_Bell, Btn_Draw, Btn_ExitRoom;
    JTextArea textArea;
    JScrollPane scrollPane;
    JScrollBar verticalScrollBar;
    JLabel Lb_UnderBellCardCount;

    GameControlThread gameControlThread;

    int Myorder;
    int numofPeople;
    boolean isDead = false;

    public JF_PlayGame(HGClientMain client) {
        this.client = client;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1000, 700);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        Btn_Bell = new JButton(new ImageIcon("img/bell.jpg"));
        Btn_Bell.setBorderPainted(false);
        Btn_Bell.setFocusPainted(false);
        Btn_Bell.setContentAreaFilled(false);
        Btn_Bell.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try{
                    dos.writeUTF(MessageTag.PBELL+"//"+Myorder);
                } catch (IOException ex){
                    ;
                }
            }
        });

        Btn_Bell.setBounds(460, 271, 100, 100);
        Btn_Bell.setEnabled(true);
        contentPane.add(Btn_Bell);

        Lb_UnderBellCardCount = new JLabel("0");
        Lb_UnderBellCardCount.setHorizontalAlignment(SwingConstants.CENTER);
        Lb_UnderBellCardCount.setFont(new Font("맑은 고딕", Font.PLAIN, 25));
        Lb_UnderBellCardCount.setBounds(544, 305, 87, 33);
        contentPane.add(Lb_UnderBellCardCount);

        Btn_Draw = new JButton("Draw");
        Btn_Draw.setFont(new Font("맑은 고딕", Font.PLAIN, 26));
        Btn_Draw.setBounds(650, 549, 149, 68);
        Btn_Draw.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    dos.writeUTF(MessageTag.DCARD+"//"+Myorder);
                } catch (IOException ex){
                    System.out.println(ex.toString());
                }
            }
        });
        contentPane.add(Btn_Draw);

        textArea = new JTextArea(10, 30);
        textArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        textArea.setEditable(false);
        textArea.setText("");

        scrollPane  = new JScrollPane(textArea);
        scrollPane.setBounds(32, 415, 248, 203);
        verticalScrollBar = scrollPane.getVerticalScrollBar();
        add(scrollPane);

        Btn_ExitRoom = new JButton("방 나가기");
        Btn_ExitRoom.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try{
                    dos.writeUTF(MessageTag.GEXIT+"");
                    ExitRoom();
                } catch (IOException ex){
                    System.out.println(ex.toString());
                }
            }
        });

        Btn_ExitRoom.setFont(new Font("한컴 고딕", Font.PLAIN, 20));
        Btn_ExitRoom.setBounds(803, 591, 171, 62);
        Btn_ExitRoom.setVisible(false);
        contentPane.add(Btn_ExitRoom);


        gameControlThread = new GameControlThread();
    }

    void Init(String[] players){
        setTitle(client.csUser.Name);

        this.socket = client.csUser.socket;
        try {
            os = socket.getOutputStream();
            dos = new DataOutputStream(os);
            is = socket.getInputStream();
            dis = new DataInputStream(is);

        } catch (IOException e){
            System.out.println(e.toString());
        }
        btnPlayers = Collections.synchronizedList(new ArrayList<>());
        Lb_PlayersCardCount = new ArrayList<>();

        int order=0;
        for(String player : players){
            btnPlayers.add(new JL_PlayersCard(player));

            JLabel jLabel = new JLabel("");
            jLabel.setHorizontalAlignment(SwingConstants.CENTER);
            jLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 25));
            Lb_PlayersCardCount.add(jLabel);

            if(client.csUser.Name.equals(player))
                Myorder = order;

            order++;
        }
        numofPeople = order;

        if(numofPeople == 2){
            btnPlayers.get(Myorder).setBounds(436, 428, 150, 225);
            Lb_PlayersCardCount.get(Myorder).setBounds(351, 620, 87, 33);

            btnPlayers.get(Myorder == 0 ? 1:0).setBounds(436, 10, 150, 225);
            Lb_PlayersCardCount.get(Myorder == 0 ? 1:0).setBounds(583, 10, 87, 33);
        }

        if(numofPeople == 3){
            btnPlayers.get(Myorder).setBounds(436, 428, 150, 225);
            Lb_PlayersCardCount.get(Myorder).setBounds(351, 620, 87, 33);

            btnPlayers.get((1+Myorder)%3).setBounds(643, 246, 225, 150);
            Lb_PlayersCardCount.get((1+Myorder)%3).setBounds(781, 393, 87, 33);

            btnPlayers.get((2+Myorder)%3).setBounds(135, 246, 225, 150);
            Lb_PlayersCardCount.get((2+Myorder)%3).setBounds(138, 213, 87, 33);
        }

        if(numofPeople == 4) {
                btnPlayers.get(Myorder).setBounds(436, 428, 150, 225);
            Lb_PlayersCardCount.get(Myorder).setBounds(351, 620, 87, 33);

            btnPlayers.get((1+Myorder)%4).setBounds(643, 246, 225, 150);
            Lb_PlayersCardCount.get((1+Myorder)%4).setBounds(781, 393, 87, 33);

            btnPlayers.get((2+Myorder)%4).setBounds(436, 10, 150, 225);
            Lb_PlayersCardCount.get((2+Myorder)%4).setBounds(583, 10, 87, 33);

            btnPlayers.get((3+Myorder)%4).setBounds(135, 246, 225, 150);
            Lb_PlayersCardCount.get((3+Myorder)%4).setBounds(138, 213, 87, 33);
        }

        for(JL_PlayersCard btn_player: btnPlayers) {
            contentPane.add(btn_player);
            btn_player.setEnabled(false);
        }

        for(JLabel jLabel : Lb_PlayersCardCount)
            contentPane.add(jLabel);

        client.ResetFrame();
        super.setVisible(true);
    }

    class GameControlThread extends Thread{
        public GameControlThread(){
            super.setName("GameControlThread");
        }

        @Override
        public void run(){
            textArea.append("GameStart!!\n");
            String msg;
            try {
                while (true) {
                    msg = dis.readUTF();
                    String[] m = msg.split("//");

                    if(m[0].equals(MessageTag.CTURN+"")) {
                        if(!isDead) {
                            Btn_Bell.setEnabled(true);
                            if (Integer.parseInt(m[1]) == Myorder) {
                                Btn_Draw.setEnabled(true);
                            } else {
                                Btn_Draw.setEnabled(false);
                            }
                        }
                        textArea.append(m[2] + "player turn\n");
                    }

                    if (m[0].equals(MessageTag.FCARD+"")) {
                        String[] cards = m[1].split("@@");
                        for(int i=0;i<numofPeople;i++){
                            btnPlayers.get(i).setText(cards[i]);
                        }
                    }

                    if (m[0].equals(MessageTag.CCRAD+"")){
                        String[] card_cnt = m[1].split("@@");
                        for(int i=0;i<numofPeople;i++){
                            Lb_PlayersCardCount.get(i).setText(card_cnt[i]+"");
                        }
                        Lb_UnderBellCardCount.setText(card_cnt[card_cnt.length-1]+"");
                    }

                    if(m[0].equals(MessageTag.PBELL+"")){
                        Btn_Bell.setEnabled(false);
                        Btn_Draw.setEnabled(false);
                        textArea.append(m[1] + "push bell" + m[2]+"\n");

                        textArea.append("3초 대기.. \n");
                    }

                    if(m[0].equals(MessageTag.DCARD+"")) {
                        textArea.append(m[1] + "draw\n");
                    }

                    //죽은 플레이어가 있을 때
                    if(m[0].equals(MessageTag.GDEAD+"")) {
                        if(m.length > 1) {
                            String[] players = m[1].split("@@");

                            for (String player : players) {
                                //만약 자신이면
                                if (client.csUser.Name.equals(player)) {
                                    Btn_Draw.setEnabled(false);
                                    Btn_Bell.setEnabled(false);
                                    Btn_ExitRoom.setVisible(true);
                                }

                                //죽은 플레이어 처리
                                Iterator<JL_PlayersCard> iterator = btnPlayers.iterator();
                                while (iterator.hasNext()) {
                                    JL_PlayersCard btn_player = iterator.next();
                                    if (btn_player.nickName.equals(player)) {
                                        btn_player.setText("Dead!!");
                                        iterator.remove(); // Use iterator to remove the current element
                                        numofPeople--;
                                    }
                                }
                                textArea.append(player + " dead!\n");
                            }
                        }
                    }

                    //게임이 끝났을 때
                    if(m[0].equals(MessageTag.GEND+"")){
                        Btn_Draw.setEnabled(false);
                        Btn_Bell.setEnabled(false);
                        Btn_ExitRoom.setVisible(true);
                    }

                    verticalScrollBar.setValue(verticalScrollBar.getMaximum());
                    this.sleep(10);
                }
            } catch (IOException e){
                System.out.println(e.toString());
            } catch (InterruptedException e){
                ;
            }
        }
    }

    void ExitRoom() {
        gameControlThread.interrupt();
        client.ResetFrame();

        client.jf_robby.setEnabled(true);
        client.jf_robby.setVisible(true);
    }


    private class JL_PlayersCard extends JLabel {
        String nickName;
        String Card;

        JL_PlayersCard(String nickName) {
            this.nickName = nickName;
        }
    }

    //유저들의 컴포넌트 저장
    private class ComponentsOfPlayers {
        //ToDo
        // 각 유저의 컴포넌트들을 저장하는 클래스를 만든다.
    }
}
