package com.icss.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

class ServerThread extends Thread 
{ 
	Socket clientSocket;
	
	Hashtable clientDataHash;// Socket 与 发送数据的流 的映射
	Hashtable clientNameHash;// Socket 与 用户名 的映射 
	Hashtable chessPeerHash;// 对弈的两个客户端用户名的映射	
	
	MessageServerPanel server;	
	boolean isClientClosed = false;	
	
	/**
	* 服务器端线程的构造函数，用于初始化一些对象。
	*/
	ServerThread(
					Socket clientSocket, 
					Hashtable clientDataHash, 
					Hashtable clientNameHash, 
					Hashtable chessPeerHash, 
					MessageServerPanel server
				) 
	{
		this.clientSocket = clientSocket;
		this.clientDataHash = clientDataHash; 
		this.clientNameHash = clientNameHash; 
		this.chessPeerHash = chessPeerHash;
		this.server = server;
	}
	
	public void run() 
	{ 
		DataInputStream inData; 
		synchronized (clientDataHash) 
		{
			server.statusLabel.setText("当前连接数:" + clientDataHash.size()); 
		}
		try
		{
			inData = new DataInputStream(clientSocket.getInputStream());
			firstCome();
			while (true) 
			{
				String message = inData.readUTF();//获取客户端发来的消息
				
				//用于调试
				System.out.println("服务器接收到消息： "+message);
				
				messageTransfer(message);	 //处理客户端发来的消息
			}
		} 
		catch (IOException esx) 
		{
		} 
		finally 
		{
			if (!isClientClosed) 
			{
				clientClose();
			}
		}
	}
	
 
	/**
	* 对客户端发来的消息处理的函数，处理后转发回客户端。
	* 处理消息的过程比较复杂，要针对很多种情况分别处理。
	*/
	public void messageTransfer(String message) 
	{ 		
		String clientName;
		String peerName;
		
		// 如果消息以“/”开头，表明是命令消息。
		if (message.startsWith("/")) 
		{
			//  如果消息以“/list”开头，则将其回馈到客户端以更新用户列表
			if (message.equals("/list")) 
			{ 
				Feedback(getUserList());
			}
			//  如果消息以"/creatgame [inchess]"开头，则修改 clientNameHash 映射
			//  和 chessPeerHash 映射。
			else if (message.startsWith("/creatgame [inchess]")) 
			{ 
				String chessServerName = message.substring(20); 
				
				synchronized (clientNameHash) 
				{
					clientNameHash.put(clientSocket, message.substring(11));
				}
				
				synchronized (chessPeerHash) 
				{
			 
					chessPeerHash.put(chessServerName, "wait");
				}
				
				Feedback("/yourname " + clientNameHash.get(clientSocket)); 
				
				chessPeerTalk(chessServerName, "/OK"); 
				
				publicTalk(getUserList());
			}
			//  如果消息以“/joingame”开头，则将消息的服务端名字和本地用户名提取出来，
			//  然后修改 clientNameHash 表和 chessPeerHash 表。
			else if (message.startsWith("/joingame ")) 
			{
				StringTokenizer userToken = new StringTokenizer(message, " "); 
				String getUserToken, serverName, selfName;
				String[] chessNameOpt = { "0", "0" };
				int getOptNum = 0;
				//  提取服务端用户名和本地用户名
				while (userToken.hasMoreTokens()) 
				{
					getUserToken = (String) userToken.nextToken(" ");
					if (getOptNum >= 1 && getOptNum <= 2) 
					{
						chessNameOpt[getOptNum - 1] = getUserToken;
					}
					getOptNum++;
				}
				serverName = chessNameOpt[0];
				selfName = chessNameOpt[1];
				//  如果有服务端在等待开始棋局
				if (chessPeerHash.containsKey(serverName) && chessPeerHash.get(serverName).equals("wait")) 
				{
					// 修改 Socket 和名字映射
					synchronized (clientNameHash) 
					{
						clientNameHash.put(clientSocket, ("[inchess]" + selfName));
					}
					
					// 修改 chessPeerHash 映射
					synchronized (chessPeerHash) 
					{
						chessPeerHash.put(serverName, selfName);
					} 
					
					publicTalk(getUserList()); 
					chessPeerTalk(selfName,("/peer " + "[inchess]" + serverName));
					chessPeerTalk(serverName,("/peer " + "[inchess]" + selfName));
				} 
				else 
				{
					chessPeerTalk(selfName, "/reject");
					try 
					{					 
						clientClose();
					} 
					catch (Exception ez) 
					{
					}
				}
			}
			//  如果消息以“/[inchess]”开头，表明是走棋消息，则获取要发送消息的用户名和发送的消息
			//  然后将走棋消息发送出去。
			else if (message.startsWith("/[inchess]")) 
			{
				int firstLocation = 0, lastLocation;		
				lastLocation = message.indexOf(" ", 0);		
				
				peerName = message.substring((firstLocation + 1), lastLocation);
				message = message.substring((lastLocation + 1));
				
				//调试用
				System.out.println("发给谁？-> " + peerName);
				System.out.println("发了什么消息？-> " + message);
				
				//将走棋消息发送给对方
				if (chessPeerTalk(peerName, message)) 
				{ 
					Feedback("/error");
				}
			}
			//  如果消息以“/giveup”开头，则判断是对弈双方哪方放弃了。
			else if (message.startsWith("/giveup ")) 
			{
				String chessClientName = message.substring(8);
				if (chessPeerHash.containsKey(chessClientName)
				&& !((String) chessPeerHash.get(chessClientName))
				.equals("wait")) 
				{
					// 如果服务方放弃，则发送消息“/youwin”表明对方获胜
					chessPeerTalk((String) chessPeerHash.get(chessClientName), "/youwin");
					synchronized (chessPeerHash) {
					chessPeerHash.remove(chessClientName);
					}
				}
				if (chessPeerHash.containsValue(chessClientName)) 
				{
					// 如果客户方放弃，也发送消息“/youwin”表明对方获胜
					chessPeerTalk((String) getHashKey(chessPeerHash, chessClientName), "/youwin");
					synchronized (chessPeerHash) {
					chessPeerHash.remove((String) getHashKey(chessPeerHash, chessClientName));
				}
				}
				}
			//  如果找不到发送消息的用户，则输出消息说“没有这个用户”
			else 
			{			 
				int firstLocation = 0, lastLocation;				
				
				lastLocation = message.indexOf(" ", 0);
				if (lastLocation == -1) 
				{ 
					Feedback("无效命令");
					return;
				} 
				else 
				{
					peerName = message.substring((firstLocation + 1), lastLocation);
					message = message.substring((lastLocation + 1));
					message = (String) clientNameHash.get(clientSocket) + ">"
					+ message;
					if (peerTalk(peerName, message)) 
					{ 
						Feedback("没有这个用户:" + peerName + "\n");
					}
				}	
			}
		}
		// 如果不以“/”开头，表明是普通消息，直接发送
		else 
		{
			message = clientNameHash.get(clientSocket) + ">" + message; 
			server.messageBoard.append(message + "\n"); 
			publicTalk(message);
			server.messageBoard.setCaretPosition(server.messageBoard.getText()
			.length());
		}
	}
	
	
	/**
	* 发送公共消息的函数，将消息向每个客户端都发送一份
	*/
	public void publicTalk(String publicTalkMessage) 
	{	
		synchronized (clientDataHash) 
		{
			for(Enumeration enu = clientDataHash.elements();enu.hasMoreElements();) 
			{
				DataOutputStream outData = (DataOutputStream) enu.nextElement();
				try 
				{
					outData.writeUTF(publicTalkMessage);
				} 
				catch(IOException es) 
				{
					es.printStackTrace();			 
				}
			}
		}
	}
	
	
	/**
	* 选择对象发送消息，参数 peerTalk 为发送的用户名，后面的参数为发送的消息
	*/
	public boolean peerTalk(String peerTalk, String talkMessage) 
	{
		//
		for (Enumeration enu = clientDataHash.keys(); enu.hasMoreElements();) 
		{ 
			Socket userClient = (Socket) enu.nextElement();
		//  找到发送消息的对象，获取它的输出流以发送消息
			if (	peerTalk.equals((String) clientNameHash.get(userClient))
					&& 
					!peerTalk.equals((String) clientNameHash.get(clientSocket))
				) 
			{
				synchronized (clientDataHash) 
				{
					DataOutputStream peerOutData = (DataOutputStream) clientDataHash.get(userClient);
				 
					try 
					{
						peerOutData.writeUTF(talkMessage);			 
					} 
					catch (IOException es) 
					{
						es.printStackTrace();
					}
				}
				Feedback(talkMessage);
				return (false);
			}
			//  如果是发给自己的，直接回馈
			else if (peerTalk.equals((String) clientNameHash.get(clientSocket))) 
			{ 
				Feedback(talkMessage);
				return (false);
			}
		}	
		return (true);
	}
	
	
	/**
	* 此函数也用于选择发送消息，但不能发送给自己。用于将走棋消息转发给对方
	*/
	public boolean chessPeerTalk(String chessPeerTalk, String chessTalkMessage) 
	{
		for (Enumeration enu = clientDataHash.keys(); enu.hasMoreElements();) 
		{ 
			Socket userClient = (Socket) enu.nextElement();
		
			if (	chessPeerTalk.equals((String) clientNameHash.get(userClient))
					&& 
					!chessPeerTalk.equals((String) clientNameHash.get(clientSocket))
				) 
			{
				synchronized (clientDataHash) 
				{
					DataOutputStream peerOutData = (DataOutputStream) clientDataHash.get(userClient);
			 
					try 
					{
						peerOutData.writeUTF(chessTalkMessage);
			 
					} 
					catch (IOException es) 
					{
						es.printStackTrace();
					}
				}
				return (false);
			}
		}
		return (true);
	}
	
	
	/**
	* 用于处理消息回馈的函数
	*/
	public void Feedback(String feedbackString) 
	{
		synchronized (clientDataHash) 
		{
			DataOutputStream outData = (DataOutputStream) clientDataHash.get(clientSocket);
			 
			try 
			{
				outData.writeUTF(feedbackString);			 
			} 
			catch (Exception eb) 
			{
				eb.printStackTrace();
			}
		}
	}
	
	
	/**
	*  获取用户列表的函数，此函数读取 clientNameHash 获取用户列表， 
	*  然后将其保存 在一个字符串 userList 中。
	*/
	public String getUserList() 
	{ 
		String userList = "/userlist"; 
	
		for ( Enumeration enu = clientNameHash.elements(); enu.hasMoreElements(); ) 
		{
			userList = userList + " " + (String) enu.nextElement();
		}
		return userList;
	}
	
	
	/**
	* 给出 HashTable 和值对象，获取相对应得键值的函数。
	*/
	public Object getHashKey(Hashtable targetHash, Object hashValue) 
	{ 
		Object hashKey;
		for ( Enumeration enu = targetHash.keys(); enu.hasMoreElements(); ) 
		{
			hashKey = (Object) enu.nextElement();
			if (hashValue.equals((Object) targetHash.get(hashKey)))
			{
				return (hashKey);
			}
		}
			return (null);
	}
	
	public void firstCome() 
	{
		publicTalk(getUserList());
		Feedback("/yourname " + (String) clientNameHash.get(clientSocket)); 
		Feedback("Java 五子棋聊天客户端");
	}
	
	
	/**
	* 用于和客户端断开的函数。
	*/
	public void clientClose() 
	{
		server.messageBoard.append("用户断开:" + clientSocket + "\n");
		// 如果是游戏客户端主机
		synchronized (chessPeerHash) 
		{
			if (chessPeerHash.containsKey(clientNameHash.get(clientSocket))) 
			{
				chessPeerHash.remove((String) clientNameHash.get(clientSocket));
			}
			if (chessPeerHash.containsValue(clientNameHash.get(clientSocket))) 
			{
				chessPeerHash.put(
									(String) getHashKey(chessPeerHash, 
									(String) clientNameHash.get(clientSocket)),
									"tobeclosed"
								);
			}
		}
		
		// 将保留的 HashTable 里的数据清除
		synchronized (clientDataHash) 
		{
			clientDataHash.remove(clientSocket);		 
		}
		
		synchronized (clientNameHash) 
		{
			clientNameHash.remove(clientSocket);
		}
		
		publicTalk(getUserList());
		
		// 计算当前连接数，并显示在状态框中 
		server.statusLabel.setText("当前连接数:" + clientDataHash.size()); 
		
		try 
		{
			clientSocket.close();
		} 
		catch (IOException exx) 
		{
		}
		
		isClientClosed = true;
	
	}
}