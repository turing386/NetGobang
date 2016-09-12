package com.icss.chesspanel;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * 
 * @author Administrator
 * 
 *	本线程类主要处理服务器转发的落子信息，将对方落子绘制到己方棋盘
 *
 */
public class ChessThread extends Thread 
{
	ChessPanel chesspad;
	
	public ChessThread(ChessPanel chesspad) 
	{
		this.chesspad = chesspad;
	}
	
	public void run() 
	{
		String message = "";
		try 
		{
			while (true) 
			{
				
				message = chesspad.inData.readUTF();
				
				//调试用
				System.out.println("chessThread run 收到消息：" + message);
				
				acceptMessage(message);
			}
		} 
		catch (IOException es) 
		{
		}
	}
	

	/**
	 * 发送消息
	 */
	public void sendMessage(String sndMessage) 
	{
		try 
		{
			//调试用
			System.out.println("chessThread  发送消息：" + sndMessage);
			chesspad.outData.writeUTF(sndMessage);			
			
		} 
		catch (Exception ea) 
		{
			System.out.println("chessThread.sendMessage:" + ea);
		}
	}

	/**
	 * 接收消息
	 */
	public void acceptMessage(String recMessage)
	{
		//如果收到的消息以“/chess”开头，表明收到服务器转发来的对方走棋信息，将其中的坐标信息和颜色信息提取出来
		if (recMessage.startsWith("/chess ")) 
		{
			//调试用
			System.out.println("chessThread 111收到消息：" + recMessage);
			
			StringTokenizer userToken = new StringTokenizer(recMessage, " "); 
			String chessToken;
			String[] chessOpt = { "-1", "-1", "0" };
			int chessOptNum = 0;
			
			//使用 Tokenizer 将空格分隔的字符串分成三段，分别是棋子X坐标，Y坐标，棋子颜色
			while (userToken.hasMoreTokens()) 
			{
				chessToken = (String) userToken.nextToken(" ");
				if (chessOptNum >= 1 && chessOptNum <= 3) 
				{
					chessOpt[chessOptNum - 1] = chessToken;	
			
				}
				chessOptNum++;
			}
			
			//将对方的走棋信息如棋子摆放的位置、棋子的颜色为参数，使用 netChessPaint函数绘制棋子
				
			chesspad.netChessPaint(	Integer.parseInt(chessOpt[0]), 
									Integer.parseInt(chessOpt[1]), 
									Integer.parseInt(chessOpt[2])
								   );

		} 
		
		else if (recMessage.startsWith("/yourname ")) 
		{
			chesspad.chessSelfName = recMessage.substring(10);
		} 
		
		else if (recMessage.equals("/error")) 
		{
			chesspad.statusText.setText("错误:没有这个用户，请退出程序，重新加入");
		}
	}

}
