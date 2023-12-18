package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class JF_Login extends JFrame {
	HGClientMain client;

	JPanel contentPane;
	JLabel Lb_Des;
	JTextField Tf_InputField;
	JButton Btn_GameExit, Btn_Connect, Btn_Join, Btn_RandomName;


	public JF_Login(HGClientMain client) {
		this.client = client;
		setTitle("로그인");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 770, 524);
		setResizable(false);


		ImageIcon backgroundImageIcon = new ImageIcon("img/login/background.jpg");
		contentPane = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				g.drawImage(backgroundImageIcon.getImage(), 0, 0, null);
				setOpaque(false);
				super.paintComponent(g);
			}
		};

		setContentPane(contentPane);
		contentPane.setLayout(null);

		Lb_Des = new JLabel("IP");
		Lb_Des.setForeground(new Color(251, 247, 201));
		Lb_Des.setHorizontalAlignment(SwingConstants.RIGHT);
		Lb_Des.setFont(new Font("맑은 고딕", Font.BOLD, 23));
		Lb_Des.setBounds(97, 342, 120, 41);
		contentPane.add(Lb_Des);

		Tf_InputField = new JTextField();
		Tf_InputField.setBackground(new Color(251, 247, 201));
		Tf_InputField.setColumns(10);
		Tf_InputField.setBounds(253, 342, 349, 41);
		contentPane.add(Tf_InputField);

		ImageIcon GameExit = new ImageIcon("img/login/GameExit.png");
		Image GameExitImage = GameExit.getImage().getScaledInstance(249, 96, Image.SCALE_SMOOTH);
		Btn_GameExit = new JButton(new ImageIcon(GameExitImage));
		Btn_GameExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			}
		});
		Btn_GameExit.setBounds(133, 413, 208, 82);
		Btn_GameExit.setBorderPainted(false);
		Btn_GameExit.setFocusPainted(false);
		Btn_GameExit.setContentAreaFilled(false);
		contentPane.add(Btn_GameExit);

		ImageIcon Connect = new ImageIcon("img/login/Connect.png");
		Image ConnectImage = Connect.getImage().getScaledInstance(249, 96, Image.SCALE_SMOOTH);
		Btn_Connect = new JButton(new ImageIcon(ConnectImage));
		Btn_Connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//ip 입력 형식 확인
				String ipRegex = "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b";
				if (!Tf_InputField.getText().matches(ipRegex)) {
					JOptionPane.showMessageDialog(client.jf_login, "입력을 확인하세요", "경고", JOptionPane.WARNING_MESSAGE);
				}

				//csUser 객체 생성 후 연결
				client.csUser = new CSUser(Tf_InputField.getText(), client);
				try {
					client.csUser.Connect();

					Btn_Connect.setVisible(false);
					Lb_Des.setText("닉네임");
					Btn_RandomName.setVisible(true);
					Btn_Join.setVisible(true);
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(client.jf_login, "연결 실패", "경고", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		Btn_Connect.setBounds(433, 413, 208, 82);
		Btn_Connect.setBorderPainted(false);
		Btn_Connect.setFocusPainted(false);
		Btn_Connect.setContentAreaFilled(false);
		contentPane.add(Btn_Connect);


		ImageIcon Join = new ImageIcon("img/login/Join.png");
		Image JoinImage = Join.getImage().getScaledInstance(249, 96, Image.SCALE_SMOOTH);
		Btn_Join = new JButton(new ImageIcon(JoinImage));
		Btn_Join.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (client.csUser.isNotOver(Tf_InputField.getText())) {
					client.csUser.Join(Tf_InputField.getText());
				} else{
					JOptionPane.showMessageDialog(client.jf_login, "중복된 닉네임입니다.", "경고", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		Btn_Join.setBounds(433, 413, 208, 82);
		Btn_Join.setBorderPainted(false);
		Btn_Join.setFocusPainted(false);
		Btn_Join.setContentAreaFilled(false);
		Btn_Join.setVisible(false);
		contentPane.add(Btn_Join);

		Btn_RandomName = new JButton("랜덤!");
		Btn_RandomName.setBackground(new Color(251, 247, 201));
		Btn_RandomName.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
		Btn_RandomName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Tf_InputField.setText(client.randomName.getRandomName());
			}
		});
		Btn_RandomName.setBounds(614, 346, 103, 37);
		Btn_RandomName.setVisible(false);
		contentPane.add(Btn_RandomName);


	}
}
