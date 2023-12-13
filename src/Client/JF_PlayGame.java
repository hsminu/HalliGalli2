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
    ImageIcon backgroundImageIcon;

    OutputStream os;
    DataOutputStream dos;
    InputStream is;
    DataInputStream dis;

    List<ComponentsOfPlayer> Cmp_Players;

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
        setBounds(100, 100, 1295, 757);
        setResizable(false);

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

        int order = 0;
        Cmp_Players = new ArrayList<>();
        for(String player : players){
            Cmp_Players.add(new ComponentsOfPlayer(player, order));
            if(client.csUser.Name.equals(player))
                Myorder = order;

            order++;
        }
        numofPeople = order;

        InitGui();

        client.ResetFrame();
        super.setVisible(true);
    }

    //*******************************************************************
    // Name : GameControlThread
    // Type : Class
    // Description : 메시지를 수신해 처리
    //               CSUser의 ReceiveThread에서 Tag:START를 받으면 이 쓰레드를 run하도록 함.
    //*******************************************************************

    class GameControlThread extends Thread{
        public GameControlThread(){
            super.setName("GameControlThread");
        }

        @Override
        public void run(){
            textArea.append("게임 시작!!\n");
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
                        textArea.append(m[2] + " 플레이어 차례\n");
                    }

                    if (m[0].equals(MessageTag.FCARD+"")) {
                        String[] cards = m[1].split("@@");

                        for(int i=0;i<numofPeople;i++){
                            Cmp_Players.get(i).setCard(cards[i]);
                        }
                    }

                    if (m[0].equals(MessageTag.CCRAD+"")){
                        String[] card_cnt = m[1].split("@@");

                        for(int i=0;i<numofPeople;i++){
                            Cmp_Players.get(i).Lb_remainCard.setText("남은 카드 : "+card_cnt[i]);
                        }
                        Lb_UnderBellCardCount.setText(card_cnt[card_cnt.length-1]+"");
                    }

                    if(m[0].equals(MessageTag.PBELL+"")){
                        Btn_Bell.setEnabled(false);
                        Btn_Draw.setEnabled(false);

                        if(m[2].equals(MessageTag.SBELL+"")){
                            textArea.append(m[1]+" 플레이어 종 누르기 성공 \n");

                        }
                        else{
                            textArea.append(m[1]+" 플레이어 종 누르기 실패 \n");
                        }

                        textArea.append("3초 대기.. \n");
                    }

                    if(m[0].equals(MessageTag.DCARD+"")) {
                        //textArea.append(m[1] + "draw\n");
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

                                ComponentsOfPlayer dcmp = null;
                                for(ComponentsOfPlayer cmp : Cmp_Players){
                                    if(cmp.nickname.equals(player)){
                                        cmp.PlayerDead();
                                        dcmp = cmp;
                                        numofPeople--;
                                    }
                                }
                                Cmp_Players.remove(dcmp);

                                //죽은 플레이어 처리
                                textArea.append(player + " dead!\n");
                            }
                        }
                    }

                    //게임이 끝났을 때
                    if(m[0].equals(MessageTag.GEND+"")){
                        textArea.append("게임 종료!! \n");
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

    //게임마다 이미
    void InitGui() {
        //배경 이미지를 플레이어 수에 따라 설정
        backgroundImageIcon = new ImageIcon("img/backgroundimg/background2.jpg");
        contentPane = new JPanel() {
            @Override
            public void paintComponent(Graphics g){
                g.drawImage(backgroundImageIcon.getImage(),0,0,null);
                setOpaque(false);
                super.paintComponent(g);
            }
        };

        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        //상태 메시지 스크롤펜
        textArea = new JTextArea(10, 30);
        textArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        textArea.setEditable(false);
        textArea.setText("");

        scrollPane  = new JScrollPane(textArea);
        scrollPane.setBounds(14, 487, 350, 220);
        verticalScrollBar = scrollPane.getVerticalScrollBar();
        contentPane.add(scrollPane);


        //종 버튼, 종 밑 카드 카운트
        ImageIcon bellIcon = new ImageIcon("img/bell.png");
        Image bellImage  = bellIcon.getImage().getScaledInstance(165, 165, Image.SCALE_SMOOTH);
        Btn_Bell = new JButton(new ImageIcon(bellImage));
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
        Btn_Bell.setEnabled(false);

        Lb_UnderBellCardCount = new JLabel("0");
        Lb_UnderBellCardCount.setHorizontalAlignment(SwingConstants.CENTER);
        Lb_UnderBellCardCount.setForeground(new Color(0, 128, 255));
        Lb_UnderBellCardCount.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        if(numofPeople == 2){
            Btn_Bell.setBounds(570, 276, 165, 165);
            Lb_UnderBellCardCount.setBounds(548, 400, 73, 51);
        } else{
            Btn_Bell.setBounds(570, 281, 165, 165);
            Lb_UnderBellCardCount.setBounds(554, 403, 73, 51);
        }
        contentPane.add(Btn_Bell);
        contentPane.add(Lb_UnderBellCardCount);

        //드로우 버튼
        Btn_Draw = new JButton("Draw");
        Btn_Draw.setBackground(new Color(255, 255, 255));
        Btn_Draw.setFont(new Font("맑은 고딕", Font.PLAIN, 26));
        Btn_Draw.setBounds(747, 627, 176, 80);
        Btn_Draw.setEnabled(false);
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

        //방 나가기 버튼
        Btn_ExitRoom = new JButton("방 나가기");
        Btn_ExitRoom.setFont(new Font("맑은 고딕", Font.PLAIN, 26));
        Btn_ExitRoom.setBackground(Color.WHITE);
        Btn_ExitRoom.setBounds(1093, 627, 176, 80);
        Btn_ExitRoom.setVisible(false);
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
        contentPane.add(Btn_ExitRoom);

        //중앙 플레이어 정보
        if(numofPeople == 2) {
            Cmp_Players.get(0).TF_InfoPlayer = new MyJTextField(1+"");
            Cmp_Players.get(0).TF_InfoPlayer.setBounds(268, 307, 155, 48);
            Cmp_Players.get(0).JL_InfoPlayer = new MyJLabel(true, 24, Cmp_Players.get(0).nickname);
            Cmp_Players.get(0).JL_InfoPlayer.setBounds(268, 365, 155, 39);

            Cmp_Players.get(1).TF_InfoPlayer = new MyJTextField(2+"");
            Cmp_Players.get(1).TF_InfoPlayer.setBounds(886, 307, 155, 48);
            Cmp_Players.get(1).JL_InfoPlayer = new MyJLabel(false, 24, Cmp_Players.get(1).nickname);
            Cmp_Players.get(1).JL_InfoPlayer.setBounds(886, 365, 155, 39);
        }

        else if(numofPeople == 3 || numofPeople == 4){
            Cmp_Players.get(0).TF_InfoPlayer = new MyJTextField(1+"");
            Cmp_Players.get(0).TF_InfoPlayer.setBounds(431, 299, 111, 33);
            Cmp_Players.get(0).JL_InfoPlayer = new MyJLabel(true, 19, Cmp_Players.get(0).nickname);
            Cmp_Players.get(0).JL_InfoPlayer.setBounds(431, 332, 111, 33);

            Cmp_Players.get(1).TF_InfoPlayer = new MyJTextField(2+"");
            Cmp_Players.get(1).TF_InfoPlayer.setBounds(431, 370, 111, 33);
            Cmp_Players.get(1).JL_InfoPlayer = new MyJLabel(true, 19, Cmp_Players.get(1).nickname);
            Cmp_Players.get(1).JL_InfoPlayer.setBounds(431, 403, 111, 33);


            if(numofPeople==3){
                Cmp_Players.get(2).TF_InfoPlayer = new MyJTextField(3+"");
                Cmp_Players.get(2).TF_InfoPlayer.setBounds(761, 326, 111, 33);
                Cmp_Players.get(2).JL_InfoPlayer = new MyJLabel(false, 19, Cmp_Players.get(2).nickname);
                Cmp_Players.get(2).JL_InfoPlayer.setBounds(761, 359, 111, 33);
            }
            else{
                Cmp_Players.get(2).TF_InfoPlayer = new MyJTextField(3+"");
                Cmp_Players.get(2).TF_InfoPlayer.setBounds(761, 299, 111, 33);
                Cmp_Players.get(2).JL_InfoPlayer = new MyJLabel(false, 19, Cmp_Players.get(2).nickname);
                Cmp_Players.get(2).JL_InfoPlayer.setBounds(761, 332, 111, 33);


                Cmp_Players.get(3).TF_InfoPlayer = new MyJTextField(4+"");
                Cmp_Players.get(3).TF_InfoPlayer.setBounds(761, 370, 111, 33);
                Cmp_Players.get(3).JL_InfoPlayer = new MyJLabel(false, 19, Cmp_Players.get(3).nickname);
                Cmp_Players.get(3).JL_InfoPlayer.setBounds(761, 403, 111, 33);
            }
        }

        //플레이어 정보 컴포넌트 위치 설정
        if(numofPeople == 2) {
            Cmp_Players.get(Myorder).Lb_playerCard.setBounds(575, 458, 150, 225);
            Cmp_Players.get(Myorder).Lb_remainCard.setBounds(396, 635, 167, 48);
            Cmp_Players.get(Myorder).Lb_numofPlayer.setBounds(737, 460, 92, 48);

            Cmp_Players.get(Myorder == 0 ? 1:0).Lb_playerCard.setBounds(575, 32, 150, 225);
            Cmp_Players.get(Myorder == 0 ? 1:0).Lb_remainCard.setBounds(737, 31, 167, 48);
            Cmp_Players.get(Myorder == 0 ? 1:0).Lb_numofPlayer.setBounds(452, 209, 92, 48);
        }

        if(numofPeople == 3) {
            Cmp_Players.get(Myorder).Lb_playerCard.setBounds(575, 477, 150, 225);
            Cmp_Players.get(Myorder).Lb_remainCard.setBounds(396, 654, 167, 48);
            Cmp_Players.get(Myorder).Lb_numofPlayer.setBounds(737, 477, 92, 48);

            Cmp_Players.get((1+Myorder)%3).Lb_playerCard.setBounds(961, 85, 150, 225);
            Cmp_Players.get((1+Myorder)%3).Lb_remainCard.setBounds(950, 27, 167, 48);
            Cmp_Players.get((1+Myorder)%3).Lb_numofPlayer.setBounds(971, 320, 92, 48);

            Cmp_Players.get((2+Myorder)%3).Lb_playerCard.setBounds(189, 85, 150, 225);
            Cmp_Players.get((2+Myorder)%3).Lb_remainCard.setBounds(183, 24, 167, 48);
            Cmp_Players.get((2+Myorder)%3).Lb_numofPlayer.setBounds(247, 319, 92, 48);
        }

        if(numofPeople == 4) {
            Cmp_Players.get(Myorder).Lb_playerCard.setBounds(570, 477, 150, 225);
            Cmp_Players.get(Myorder).Lb_remainCard.setBounds(396, 654, 167, 48);
            Cmp_Players.get(Myorder).Lb_numofPlayer.setBounds(737, 477, 92, 48);

            Cmp_Players.get((1+Myorder)%4).isRotated = true;
            Cmp_Players.get((1+Myorder)%4).Lb_playerCard.setBounds(967, 281, 225, 150);
            Cmp_Players.get((1+Myorder)%4).Lb_remainCard.setBounds(1025, 228, 167, 48);
            Cmp_Players.get((1+Myorder)%4).Lb_numofPlayer.setBounds(1100, 441, 92, 48);

            Cmp_Players.get((2+Myorder)%4).Lb_playerCard.setBounds(570, 10, 150, 225);
            Cmp_Players.get((2+Myorder)%4).Lb_remainCard.setBounds(732, 10, 167, 48);
            Cmp_Players.get((2+Myorder)%4).Lb_numofPlayer.setBounds(454, 187, 92, 48);

            Cmp_Players.get((3+Myorder)%4).isRotated = true;
            Cmp_Players.get((3+Myorder)%4).Lb_playerCard.setBounds(101, 281, 225, 150);
            Cmp_Players.get((3+Myorder)%4).Lb_remainCard.setBounds(101, 429, 167, 48);
            Cmp_Players.get((3+Myorder)%4).Lb_numofPlayer.setBounds(101, 223, 92, 48);
        }

        int num=1;
        for(ComponentsOfPlayer cmp : Cmp_Players){
            contentPane.add(cmp.Lb_playerCard);
            contentPane.add(cmp.Lb_remainCard);
            contentPane.add(cmp.TF_InfoPlayer);
            contentPane.add(cmp.JL_InfoPlayer);

            cmp.Lb_numofPlayer.setText("0"+num++);
            contentPane.add(cmp.Lb_numofPlayer);
        }

    }


    //유저들의 컴포넌트 저장
    private class ComponentsOfPlayer {
        String nickname;
        int order;

        JLabel Lb_playerCard;
        JLabel Lb_remainCard;
        JLabel Lb_numofPlayer;

        MyJTextField TF_InfoPlayer;
        MyJLabel JL_InfoPlayer;

        boolean isRotated = false;

        ComponentsOfPlayer(String nickname, int order){
            this.nickname = nickname;
            this.order = order;

            Lb_playerCard = new JLabel();

            Lb_remainCard = new JLabel();
            Lb_remainCard.setHorizontalAlignment(SwingConstants.CENTER);
            Lb_remainCard.setForeground(new Color(230, 230, 0));
            Lb_remainCard.setFont(new Font("맑은 고딕", Font.BOLD, 24));

            Lb_numofPlayer = new JLabel();
            Lb_numofPlayer.setForeground(new Color(230, 230, 0));
            Lb_numofPlayer.setFont(new Font("맑은 고딕", Font.BOLD, 30));
            Lb_numofPlayer.setHorizontalAlignment(SwingConstants.CENTER);
        }

        //카드 이미지 설정
        void setCard(String card){
            if(card.equals("null")){
                Lb_playerCard.setIcon(null);
            }
            else {
                String[] m = card.split("#");
                if (isRotated) {
                    ImageIcon FruitImageIcon = new ImageIcon("img/FRUIT/" + m[0] + "/" + m[0] + "#" + m[1] + "#ROW.png");
                    Image FruitImage = FruitImageIcon.getImage().getScaledInstance(225, 150, Image.SCALE_SMOOTH);
                    Lb_playerCard.setIcon(new ImageIcon(FruitImage));
                } else {
                    ImageIcon FruitImageIcon = new ImageIcon("img/FRUIT/" + m[0] + "/" + m[0] + "#" + m[1] + "#COLUMN.png");
                    Image FruitImage = FruitImageIcon.getImage().getScaledInstance(150, 225, Image.SCALE_SMOOTH);
                    Lb_playerCard.setIcon(new ImageIcon(FruitImage));
                }
            }

        }

        void PlayerDead(){
            TF_InfoPlayer.setText("Dead");
            Lb_playerCard.setIcon(null);
        }
    }

    class MyJTextField extends JTextField{
        MyJTextField(String num){
            super.setHorizontalAlignment(SwingConstants.CENTER);
            super.setFont(new Font("맑은 고딕", Font.PLAIN, 24));
            super.setText("Player 0"+num);
            super.setEditable(false);
        }
    }

    class MyJLabel extends JLabel{
        MyJLabel(boolean isLeft, int fontsize, String nickname){
            super.setForeground(new Color(255, 255, 255));
            super.setFont(new Font("맑은 고딕", Font.PLAIN, fontsize));
            super.setHorizontalAlignment(isLeft == true ? SwingConstants.LEFT : SwingConstants.RIGHT);
            super.setText(nickname);
        }
    }
}
