package com.icss.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.UIManager;

import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.skin.SubstanceOfficeBlue2007LookAndFeel;

/**
 * 服务器端框架类
 * 
 * @author  
*/
public class ChessServer extends Frame implements ActionListener 
{ 
	JButton messageClearButton = new JButton("清除显示"); 	
	JButton serverStatusButton = new JButton("服务器状态");	
	JButton serverOffButton = new JButton("关闭服务器"); 
	
	Panel buttonPanel = new Panel();
	
	MessageServerPanel server = new MessageServerPanel(); 
	ServerSocket serverSocket;
	
	Hashtable clientDataHash = new Hashtable(50); 
	Hashtable clientNameHash = new Hashtable(50); 
	Hashtable chessPeerHash = new Hashtable(50);
	
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
		
		ChessServer ChessServer = new ChessServer();
	}
	
	
	/**
	*	服务器框架类的构造函数
	*/ 
	ChessServer() 
	{
		super("Java 五子棋服务器");
		setBackground(new Color(204, 204, 204));		
		
		buttonPanel.setLayout(new FlowLayout()); 
		
		messageClearButton.setSize(60, 25); 
		buttonPanel.add(messageClearButton); 
		messageClearButton.addActionListener(this); 
		
		serverStatusButton.setSize(75, 25); 
		buttonPanel.add(serverStatusButton); 
		serverStatusButton.addActionListener(this); 
		
		serverOffButton.setSize(75, 25); 
		buttonPanel.add(serverOffButton); 
		serverOffButton.addActionListener(this);		
		
		add(server, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		// 退出窗口的监听器
		addWindowListener(
							new WindowAdapter() 
							{
										public void windowClosing(WindowEvent e) 
										{ 
											System.exit(0);
										}
							}
						);
		
		setVisible(true);		 
		setSize(400, 450); 
		setResizable(false); 
		validate();
		
		try 
		{
			makeMessageServer(4331, server);
		} 
		catch (Exception e) 
		{
			System.out.println("e");
		}
	}
	
	
	
	
	/**
	* 初始化消息服务器的类
	*/
	public void makeMessageServer(int port, MessageServerPanel server)
	throws IOException 
	{ 
		Socket clientSocket;
		long clientAccessNumber = 1;
		this.server = server;
		
		try 
		{
			//  输出服务器的启动信息
			serverSocket = new ServerSocket(port);
			server.messageBoard.setText(
											"服务器开始于:"
											+ serverSocket.getInetAddress().getLocalHost() 
											+ ":"
											+ serverSocket.getLocalPort() + "\n"
										);
			
			while (true) 
			{
				clientSocket = serverSocket.accept();
				server.messageBoard.append("用户连接:" + clientSocket + "\n");
				
				DataOutputStream outData = new DataOutputStream(clientSocket.getOutputStream()); 
				clientDataHash.put(clientSocket, outData); 
				clientNameHash.put(clientSocket, ("player" + clientAccessNumber++));
				
				ServerThread thread = new ServerThread(clientSocket, clientDataHash, clientNameHash, chessPeerHash, server);
				thread.start();
			}
		} 
		catch (IOException ex) 
		{ 
			System.out.println("已经有服务器在运行. \n");
		}
	}
	 
	/**
	* 按钮的事件监听器，响应按钮点击事件
	*/
	public void actionPerformed(ActionEvent e) 
	{
		// 当点击“清除显示”按钮时，清除服务器状态信息
		if (e.getSource() == messageClearButton) 
		{
			server.messageBoard.setText("");
		}
		
		// 当点击“服务器状态”按钮时，显示服务器状态
		if (e.getSource() == serverStatusButton) 
		{
			try
			{
				server.messageBoard.append(	"服务器信息:"
											+ serverSocket.getInetAddress().getLocalHost() 
											+ ":"
											+ serverSocket.getLocalPort() 
											+ "\n"
										  );
			} 
			catch (Exception ee) 
			{ 
				System.out.println("serverSocket.getInetAddress().getLocalHost() error \n");
			}
		}
		
		// 当点击“关闭服务器”按钮时，退出程序
		if (e.getSource() == serverOffButton) 
		{ 
			System.exit(0);
		}
	}
	
}