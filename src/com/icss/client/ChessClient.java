package com.icss.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.skin.SubstanceOfficeBlue2007LookAndFeel;
import com.icss.chesspanel.*;


/**
 * 五子棋客户端框架
 * 用来初始化一些对象、布局和为按钮添加监听器。
 * 监听 连接服务器 新建游戏 加入游戏 等按钮 初始化游戏
 * 
 * @author Administrator
 *
 */
public class ChessClient extends Frame implements ActionListener{
	

	private static final long serialVersionUID = 1L;

	UserPanel userpad = new UserPanel();// 用户列表 Panel
	ControlPanel controlpad = new ControlPanel();// 控制 Panel 
	ChessPanel chesspad = new ChessPanel();// 棋盘 Panel
	
	Socket chatSocket;	
	
	DataInputStream in; 
	DataOutputStream out;
	String chessClientName = null; 
	String host = null;
	int port = 4331;	
	
	boolean isOnChat = false; // 是否在聊天	
	boolean isOnChess = false; //  是否在下棋	
	boolean isGameConnected = false; //  是否下棋的客户端连接	
	boolean isServer = false; //  是否建立游戏的主机	
	boolean isClient = false; //  是否加入游戏的客户端	
	
	JPanel southPanel = new JPanel(); 
	JPanel centerPanel = new JPanel(); 
	JPanel westPanel = new JPanel();	
	
	public static void main(String args[]) 
	{
		SubstanceLookAndFeel sa = new SubstanceOfficeBlue2007LookAndFeel();
		
		try 
		{ 
			UIManager.setLookAndFeel(sa);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		ChessClient ChessClient = new ChessClient(); 
		ChessClient.setResizable(false);
	}
	
	/**
	* 五子棋客户端框架的构造函数。
	*/
	public ChessClient() 
	{
		super("五子棋客户端");
		setLayout(new BorderLayout());
		host = controlpad.inputIP.getText();
		westPanel.setLayout(new BorderLayout());
		westPanel.add(userpad, BorderLayout.NORTH);
		westPanel.setBackground(new Color(204, 204, 204));

	

		chesspad.host = controlpad.inputIP.getText();

		centerPanel.add(chesspad, BorderLayout.CENTER);
		

		centerPanel.setBackground(new Color(204, 204, 204));

		controlpad.connectButton.addActionListener(this);
		controlpad.creatGameButton.addActionListener(this);
		controlpad.joinGameButton.addActionListener(this);
		controlpad.cancelGameButton.addActionListener(this);
		controlpad.exitGameButton.addActionListener(this);

		controlpad.creatGameButton.setEnabled(false);
		controlpad.joinGameButton.setEnabled(false);
		controlpad.cancelGameButton.setEnabled(false);

		southPanel.add(controlpad, BorderLayout.CENTER);
		southPanel.setBackground(new Color(204, 204, 204));

		// 添加窗口监听器，当窗口关闭时，关闭用于通讯的 Socket。
		addWindowListener(new WindowAdapter() 
		{
			public void windowClosing(WindowEvent e) 
			{
				if (isOnChat) 
				{
					try 
					{
						chatSocket.close();
					} 
					catch (Exception ed) 
					{
						System.exit(0);
					}
				}
				if (isOnChess || isGameConnected) 
				{
					try 
					{
						chesspad.chessSocket.close();
					} 
					catch (Exception ee)
					{
						System.exit(0);
					}
				}

			}
		});


		add(westPanel, BorderLayout.WEST);
		add(centerPanel, BorderLayout.CENTER);
		add(southPanel, BorderLayout.SOUTH);
		setSize(600, 500);
		setLocation(300, 150);
		setVisible(true);
		setResizable(true);
		validate();
	}
	
	/**
	* 和服务器建立连接并通信的函数。
	*/
	public boolean connectServer(String serverIP, int serverPort)throws Exception 
	{
		try 
		{
			chatSocket = new Socket(serverIP, serverPort);
			in = new DataInputStream(chatSocket.getInputStream());
			out = new DataOutputStream(chatSocket.getOutputStream());

			ClientThread clientthread = new ClientThread(this);
			clientthread.start();
			
			isOnChat = true;
			return true;
			
		} 
		catch (IOException ex) 
		{
			// chatpad.chatLineArea.setText("chessClient:connectServer:无法连接,建议重新启动程序 \n");
		}
		return false;
	}
	
	
	/**
	* 动作监听器，响应按钮点击动作。
	*/
	public void actionPerformed(ActionEvent e) 
	{
		// 如果点击的是“连接服务器”按钮，则用获取的服务器主机名连接服务器。
		if (e.getSource() == controlpad.connectButton) 
		{
			host = chesspad.host = controlpad.inputIP.getText();
			try 
			{
				if (connectServer(host, port)) 
				{
					// chatpad.chatLineArea.setText("");
					controlpad.connectButton.setEnabled(false);
					controlpad.creatGameButton.setEnabled(true);
					controlpad.joinGameButton.setEnabled(true);
					
					chesspad.statusText.setText("连接成功，请新建游戏或加入游戏");
				}

			} catch (Exception ei) 
			{
				// chatpad.chatLineArea.setText("controlpad.connectButton: 无法连接,建议重新启动程序\n");

			}
		}

		// 如果点击的是“新建游戏”按钮，设定用户状态、按钮状态，然后与服务器通信。
		if (e.getSource() == controlpad.creatGameButton) 
		{
			try 
			{
				// 未建立连接时的操作。
				if (!isGameConnected) 
				{
					if (chesspad.connectServer(chesspad.host, chesspad.port)) 
					{
						isGameConnected = true;
						isOnChess = true;
						isServer = true;
						
						controlpad.creatGameButton.setEnabled(false);
						controlpad.joinGameButton.setEnabled(false);
						controlpad.cancelGameButton.setEnabled(true);
						
						chesspad.chessthread.sendMessage(	"/creatgame "
															+ "[inchess]" 
															+ chessClientName
														);
					}
				}
				// 建立连接时的操作。
				else 
				{
					isOnChess = true;
					isServer = true;

					controlpad.creatGameButton.setEnabled(false);
					controlpad.joinGameButton.setEnabled(false);
					controlpad.cancelGameButton.setEnabled(true);
					
					chesspad.chessthread.sendMessage(	"/creatgame " 
														+ "[inchess]" 
														+ chessClientName
													);
				}
			} 
			catch (Exception ec) 
			{
				isGameConnected = false;
				isOnChess = false;
				isServer = false;
				controlpad.creatGameButton.setEnabled(true);
				controlpad.joinGameButton.setEnabled(true);
				controlpad.cancelGameButton.setEnabled(false);
				ec.printStackTrace();
				// chatpad.chatLineArea.setText("chesspad.connectServer 无法连接 \n"+
				// ec);
			}

		}

		// 如果点击的是“进入游戏”按钮，则先判断选定的加入的目标是否有效。
		// 如果选定的目标为空或正在下棋或为其本身，则认为目标无效。
		if (e.getSource() == controlpad.joinGameButton) 
		{
			String selectedUser = userpad.userList.getSelectedItem();
			
			if (		selectedUser == null 
					|| selectedUser.startsWith("[inchess]")
					|| selectedUser.equals(chessClientName)
				) 
			{
				chesspad.statusText.setText("必须先选定一个有效用户");
			} 
			else 
			{
				try 
				{
					// 如果未建立与服务器的连接，创建连接，设定用户的当前状态。
					// 此外还要对按钮作一些处理，将“新建游戏”按钮和“进入游戏”按钮设为不可用。
					if (!isGameConnected) 
					{
						if (chesspad.connectServer(chesspad.host, chesspad.port)) 
						{
							isGameConnected = true;
							isOnChess = true;
							isClient = true;
							
							controlpad.creatGameButton.setEnabled(false);
							controlpad.joinGameButton.setEnabled(false);
							controlpad.cancelGameButton.setEnabled(true);
							
							chesspad.chessthread.sendMessage(
																"/joingame "
																+ userpad.userList.getSelectedItem() 
																+ " "
																+ chessClientName
															);
						}
					}
					// 如果已建立连接，省去建立连接的操作。
					else 
					{
						isOnChess = true;
						isClient = true;
						
						controlpad.creatGameButton.setEnabled(false);
						controlpad.joinGameButton.setEnabled(false);
						controlpad.cancelGameButton.setEnabled(true);
						
						chesspad.chessthread.sendMessage(
																"/joingame "
																+ userpad.userList.getSelectedItem() 
																+ " "
																+ chessClientName
														);
					}

				} 
				catch (Exception ee) 
				{
					isGameConnected = false;
					isOnChess = false;
					isClient = false;
					controlpad.creatGameButton.setEnabled(true);
					controlpad.joinGameButton.setEnabled(true);
					controlpad.cancelGameButton.setEnabled(false);
					// chatpad.chatLineArea.setText("chesspad.connectServer 无法连接 \n"
					// + ee);
				}

			}
		}

		// 如果点击的是“退出游戏”按钮，同样要修改按钮状态。
		if (e.getSource() == controlpad.cancelGameButton) 
		{
			// 如果棋局正在进行，判定退出游戏的一方输
			if (isOnChess) 
			{
				chesspad.chessthread.sendMessage("/giveup " + chessClientName);
				chesspad.chessVictory(-1 * chesspad.chessColor);
				
				controlpad.creatGameButton.setEnabled(true);
				controlpad.joinGameButton.setEnabled(true);
				controlpad.cancelGameButton.setEnabled(false);
				
				chesspad.statusText.setText("请建立游戏或者加入游戏");
			}
			if (!isOnChess) 
			{
				controlpad.creatGameButton.setEnabled(true);
				controlpad.joinGameButton.setEnabled(true);
				controlpad.cancelGameButton.setEnabled(false);
				
				chesspad.statusText.setText("请建立游戏或者加入游戏");
			}
			
			isClient = isServer = false;
			
		}

		// 如果点击的是“关闭程序”按钮，则关闭正在进行通信的 Socekt 并退出游戏。
		if (e.getSource() == controlpad.exitGameButton) 
		{
			if (isOnChat) 
			{
				try 
				{
					chatSocket.close();
				} 
				catch (Exception ed) 
				{
				}
			}
			if (isOnChess || isGameConnected) 
			{
				try 
				{
					chesspad.chessSocket.close();
				} 
				catch (Exception ee) 
				{
				}
			}
			System.exit(0);
		}

	}
	
	

	public void keyTyped(KeyEvent e) 
	{
	}	
	
	public void keyReleased(KeyEvent e) 
	{
	}	
	
}
