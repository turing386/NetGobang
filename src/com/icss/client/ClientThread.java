package com.icss.client;

import java.io.IOException;
import java.util.StringTokenizer;


public class ClientThread extends Thread
{
	ChessClient chessclient;
	
	ClientThread(ChessClient chessclient) 
	{
		this.chessclient = chessclient;
	}
	
	public void run() 
	{
		String message = "";
		try 
		{
			while (true) 
			{
				message = chessclient.in.readUTF();
				System.out.println("clientThread 收到消息：" + message);
				acceptMessage(message);
			}
		} 
		catch (IOException es) 
		{
		}
	}


	/**
	* 客户端线程对接收到的信息进行处理的函数
	*/
	public void acceptMessage(String recMessage) 
	{
		if (recMessage.startsWith("/userlist ")) 
		{
			//  如果接收到的信息以"/userlist "开头，将其后的用户名提取出来，添加到
			//  输入信息 Panel 左边的用户列表中。
			StringTokenizer userToken = new StringTokenizer(recMessage, " ");
			int userNumber = 0;
			
			chessclient.userpad.userList.removeAll();
//			chessclient.inputpad.userChoice.removeAll(); 
//			chessclient.inputpad.userChoice.addItem("所有人"); 
			while (userToken.hasMoreTokens()) 
			{
				String user = (String) userToken.nextToken(" ");
				if (userNumber > 0 && !user.startsWith("[inchess]")) 
				{
					chessclient.userpad.userList.add(user);
//					chessclient.inputpad.userChoice.addItem(user);
				}
				userNumber++;
			}
//			chessclient.inputpad.userChoice.select("所有人");
		 
		}
		//  如果接收到的信息以"/yourname " 开头,将用户名显示在客户端对话框标题栏。
		else if (recMessage.startsWith("/yourname ")) 
		{
			chessclient.chessClientName = recMessage.substring(10);
			System.out.println("chessClientName " + chessclient.chessClientName);
			chessclient.setTitle(
									"五子棋客户端 " 
									+ "当前用户名:"
									+ chessclient.chessClientName
								);
		}
		
		// 如果接收到的信息以"/reject"开头，在状态栏显示拒绝加入游戏。
		else if (recMessage.equals("/reject")) 
		{
			try 
			{
				chessclient.chesspad.statusText.setText("不能加入游戏"); 
				chessclient.controlpad.cancelGameButton.setEnabled(false); 
				chessclient.controlpad.joinGameButton.setEnabled(true);
				chessclient.controlpad.creatGameButton.setEnabled(true);
			} 
			catch (Exception ef) 
			{
//				chessclient.chatpad.chatLineArea.setText("chessclient.chesspad.chessSocket.close 无法关闭");
			}
			chessclient.controlpad.joinGameButton.setEnabled(true);
		}
		
		// 如果接收到的信息以"/peer"开头,则记下对方的名字，然后进入等待状态
		else if (recMessage.startsWith("/peer ")) 
		{ 
			chessclient.chesspad.chessPeerName = recMessage.substring(6); 
			if (chessclient.isServer) //是创建游戏的客户端，则下黑子
			{
				chessclient.chesspad.chessColor = 1;
				chessclient.chesspad.isMouseEnabled = true;
				chessclient.chesspad.statusText.setText("请黑棋下子");
			}
			else if (chessclient.isClient) //是加入游戏的客户端，下白子
			{
				chessclient.chesspad.chessColor = -1;
				chessclient.chesspad.statusText.setText("已加入游戏，等待对方下子...");
			}
			 
		} 
		else if (recMessage.equals("/youwin")) 
		{ 	
			chessclient.isOnChess = false; 
			chessclient.chesspad.chessVictory(chessclient.chesspad.chessColor);
			chessclient.chesspad.statusText.setText("对方退出，请点放弃游戏退出连接");
			chessclient.chesspad.isMouseEnabled = false;
		} 
		else if (recMessage.equals("/OK")) 
		{
			chessclient.chesspad.statusText.setText("创建游戏成功，等待别人加入...");
		} 
		else if (recMessage.equals("/error")) 
		{
//			chessclient.chatpad.chatLineArea.append(" 传输错误：请退出程序，重新加入\n");
	
		} 
		else 
		{		 
//			chessclient.chatpad.chatLineArea.append(recMessage + "\n");
//			chessclient.chatpad.chatLineArea.setCaretPosition(chessclient.chatpad.chatLineArea.getText().length());
		}
	}	
}
