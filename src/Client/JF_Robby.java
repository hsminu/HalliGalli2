package Client;

import Common.MessageTag;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;

//*******************************************************************
// Name : JF_Robby
// Type : Class
// Description :  로비 화면 프레임을 구현한 클래스.
//*******************************************************************

public class JF_Robby extends JFrame {
	HGClientMain client;
	private JPanel contentPane;

	JList JL_RoomList, JL_PlayerList;
	DefaultListModel Model_RoomList, Model_PlayerList;

	CreateRoomDialog createRoomDialog;

	public JF_Robby(HGClientMain client) {
		this.client = client;
		setTitle("대기실");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 817, 607);
		setResizable(false);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		createRoomDialog = new CreateRoomDialog(this);
		createRoomDialog.setVisible(false);

		setContentPane(contentPane);
		contentPane.setLayout(null);


		Model_RoomList = new DefaultListModel();
		JL_RoomList = new JList(Model_RoomList);
		JL_RoomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane SP_RoomList = new JScrollPane(JL_RoomList);
		SP_RoomList.setBounds(15, 40, 411, 519);
		contentPane.add(SP_RoomList);


		Model_PlayerList = new DefaultListModel();
		JL_PlayerList = new JList(Model_PlayerList);
		JL_PlayerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane SP_PlayerList = new JScrollPane(JL_PlayerList);
		SP_PlayerList.setBounds(472, 40, 319, 311);
		contentPane.add(SP_PlayerList);

		JButton Btn_CreateRoom = new JButton("방 생성하기");
		Btn_CreateRoom.setFont(new Font("한컴 고딕", Font.PLAIN, 20));
		Btn_CreateRoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createRoomDialog.setVisible(true);
			}
		});
		Btn_CreateRoom.setBounds(472, 371, 319, 56);
		contentPane.add(Btn_CreateRoom);

		JButton Btn_JoinRoom = new JButton("방 입장하기");
		Btn_JoinRoom.setFont(new Font("한컴 고딕", Font.PLAIN, 20));
		Btn_JoinRoom.setBounds(472, 437, 319, 56);
		Btn_JoinRoom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String[] s = ((String) JL_RoomList.getSelectedValue()).split(":");
				try {
					client.csUser.sendServer(MessageTag.EROOM + "//"+s[0]);

					client.jf_robby.setEnabled(false);
					client.jf_readyRoom.setVisible(true);
					client.jf_readyRoom.Init(false);
					client.jf_readyRoom.Btn_Ready.setVisible(true);
				} catch (IOException ex){
					JOptionPane.showMessageDialog(client.jf_robby, "통신 오류", "경고", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		contentPane.add(Btn_JoinRoom);

		JButton btnNewButton_2 = new JButton("게임 종료하기");
		btnNewButton_2.setFont(new Font("한컴 고딕", Font.PLAIN, 20));
		btnNewButton_2.setBounds(472, 503, 319, 56);
		contentPane.add(btnNewButton_2);

		JLabel lblNewLabel = new JLabel("====================== 방 목록 =========================");
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblNewLabel.setFont(new Font("굴림", Font.PLAIN, 15));
		lblNewLabel.setBounds(15, 15, 411, 15);
		contentPane.add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("================ 접속 인원 =================");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.LEFT);
		lblNewLabel_1.setFont(new Font("굴림", Font.PLAIN, 15));
		lblNewLabel_1.setBounds(472, 15, 319, 15);
		contentPane.add(lblNewLabel_1);
	}

	class CreateRoomDialog extends JDialog {
		private JTextField roomNameField;
		private JTextField numOfPeopleField;

		public CreateRoomDialog(JFrame parent) {
			super(parent, "방 생성하기", true);
			setSize(300, 200);
			setLocationRelativeTo(parent);

			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(3, 2));

			roomNameField = new JTextField();
			numOfPeopleField = new JTextField();

			panel.add(new JLabel("방 이름:"));
			panel.add(roomNameField);
			panel.add(new JLabel("최대 플레이어 수:"));
			panel.add(numOfPeopleField);

			JButton createButton = new JButton("방 생성");
			createButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String roomName = roomNameField.getText();
					String numOfPeopleStr = numOfPeopleField.getText();

					try {
						client.csUser.sendServer(MessageTag.CROOM +"//"+roomName+"//"+numOfPeopleStr);
						dispose();

						client.jf_robby.setEnabled(false);
						client.jf_readyRoom.setVisible(true);
						client.jf_readyRoom.Init(true);
						client.jf_readyRoom.Btn_GameStart.setVisible(true);
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(client.jf_robby, "통신 오류", "경고", JOptionPane.WARNING_MESSAGE);
					}
				}
			});

			JButton cancelButton = new JButton("취소");
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});

			panel.add(createButton);
			panel.add(cancelButton);

			add(panel);
		}
	}
}
