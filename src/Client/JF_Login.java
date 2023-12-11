package Client;

import java.awt.EventQueue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class JF_Login extends JFrame {
	HGClientMain client;

	JPanel contentPane;
	JTextField Tf_NickName;
	JTextField Tf_iP;
	JTextField Tf_port;


	public JF_Login(HGClientMain client) {
		this.client = client;
		setTitle("로그인");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 386, 280);
		setResizable(false);
		contentPane = new JPanel();

		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel Lb_NickName = new JLabel("닉네임");
		Lb_NickName.setFont(new Font("맑은 고딕", Font.PLAIN, 21));
		Lb_NickName.setHorizontalAlignment(SwingConstants.CENTER);
		Lb_NickName.setBounds(0, 0, 121, 51);
		contentPane.add(Lb_NickName);

		JLabel Lb_iP = new JLabel("ip");
		Lb_iP.setHorizontalAlignment(SwingConstants.CENTER);
		Lb_iP.setFont(new Font("맑은 고딕", Font.PLAIN, 21));
		Lb_iP.setBounds(0, 61, 121, 51);
		contentPane.add(Lb_iP);

		JLabel Lb_port = new JLabel("port");
		Lb_port.setHorizontalAlignment(SwingConstants.CENTER);
		Lb_port.setFont(new Font("맑은 고딕", Font.PLAIN, 21));
		Lb_port.setBounds(0, 122, 121, 51);
		contentPane.add(Lb_port);

		Tf_NickName = new JTextField();
		Tf_NickName.setBounds(120, 0, 240, 51);
		contentPane.add(Tf_NickName);
		Tf_NickName.setColumns(10);

		Tf_iP = new JTextField();
		Tf_iP.setColumns(10);
		Tf_iP.setBounds(120, 61, 240, 51);
		contentPane.add(Tf_iP);

		Tf_port = new JTextField();
		Tf_port.setColumns(10);
		Tf_port.setBounds(120, 122, 240, 51);
		contentPane.add(Tf_port);

		JButton Btn_GameExit = new JButton("게임종료");
		Btn_GameExit.setFont(new Font("맑은 고딕", Font.PLAIN, 19));
		Btn_GameExit.setBounds(36, 194, 132, 49);
		contentPane.add(Btn_GameExit);

		JButton Btn_GameConnect = new JButton("게임접속");
		Btn_GameConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(CheckInput()){
					client.csUser = new CSUser(Tf_NickName.getText(), Tf_iP.getText(), Integer.parseInt(Tf_port.getText()), client);
					try {
						client.csUser.Connect();
					} catch (IOException e2){
						JOptionPane.showMessageDialog(client.jf_login, "연결 실패", "경고", JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		});
		Btn_GameConnect.setFont(new Font("맑은 고딕", Font.PLAIN, 19));
		Btn_GameConnect.setBounds(210, 194, 132, 49);
		contentPane.add(Btn_GameConnect);
	}


	//w
	private boolean CheckInput(){
		if (Tf_NickName.getText().length() <= 1) {
			JOptionPane.showMessageDialog(this, "닉네임을 두 자리 이상 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		String ipRegex = "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b";
		if(Tf_iP.getText().matches(ipRegex) != true){
			JOptionPane.showMessageDialog(this, "올바른 ip형식을 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		String portRegex = "\\b(?:[0-9]{1,5})\\b";
		if(Tf_port.getText().matches(portRegex) != true){
			JOptionPane.showMessageDialog(this, "올바른 port형식을 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		return true;
	}
}
