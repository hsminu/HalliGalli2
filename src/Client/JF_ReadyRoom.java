package Client;

import Common.MessageTag;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class JF_ReadyRoom extends JFrame {
    HGClientMain client;

    JTextArea JT_Players;
    JButton Btn_Ready, Btn_GameStart;

    boolean isHead = false;

    public JF_ReadyRoom(HGClientMain client) {
        this.client = client;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 510, 300);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        setContentPane(contentPane);
        contentPane.setLayout(null);

        JT_Players = new JTextArea();
        JT_Players.setFont(new Font("한컴 고딕", Font.PLAIN, 20));
        JT_Players.setEditable(false);
        JT_Players.setBounds(10, 10, 474, 159);
        contentPane.add(JT_Players);

        JButton Btn_ExitRoom = new JButton("방 나가기");
        Btn_ExitRoom.setFont(new Font("한컴 고딕", Font.PLAIN, 20));
        Btn_ExitRoom.setBounds(44, 190, 171, 62);
        contentPane.add(Btn_ExitRoom);

        Btn_Ready = new JButton("레디");
        Btn_Ready.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
        Btn_Ready.setBounds(295, 190, 171, 62);
        Btn_Ready.setVisible(true);
        Btn_Ready.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    client.csUser.sendServer(MessageTag.READY + "");
                } catch (IOException ex){
                    JOptionPane.showMessageDialog(client.jf_readyRoom, "통신 오류", "경고", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        contentPane.add(Btn_Ready);

        Btn_GameStart = new JButton("게임 시작");
        Btn_GameStart.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
        Btn_GameStart.setBounds(295, 190, 171, 62);
        Btn_GameStart.setVisible(true);
        Btn_GameStart.setEnabled(false);
        Btn_GameStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    client.csUser.sendServer(MessageTag.START + "");
                } catch (IOException ex){
                    JOptionPane.showMessageDialog(client.jf_readyRoom, "통신 오류", "경고", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        contentPane.add(Btn_GameStart);
    }

    void Init(boolean isHead) {
        this.isHead = isHead;
        if(isHead) {
            Btn_Ready.setVisible(false);
            Btn_GameStart.setVisible(false);
        }
    }
}
