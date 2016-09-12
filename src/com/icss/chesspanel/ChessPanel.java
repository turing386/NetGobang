package com.icss.chesspanel;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * 
 * 客户端显示棋盘的 Panel。此 Panel 实现了鼠标监听器
 */
public class ChessPanel extends Panel implements MouseListener {
	
	public int chessPoint_x = -1, chessPoint_y = -1, chessColor = 1;

	int chessBlack_x[] = new int[200];//黑子的 x 坐标 
	int chessBlack_y[] = new int[200];//黑子的 y 坐标 
	int	chessWhite_x[] = new int[200];//白子的 x坐标 
	int chessWhite_y[] = new int[200];//白子的 y 坐标

	int chessBlackCount = 0;	//黑棋子数目
	int chessWhiteCount = 0;	//白棋子数目

	int chessBlackWin = 0;
	int chessWhiteWin = 0;

	public boolean isMouseEnabled = false;
	public boolean isWin = false;
	public boolean isInGame = false;

	public JLabel statusLabel = new JLabel("客户端状态");

	public JTextField statusText = new JTextField(" 请先连接服务器");
	// 显示客户端状态的文本框

	public Socket chessSocket;
	DataInputStream inData;
	DataOutputStream outData;
	
	public String chessSelfName = null;// 己方的名字
	public String chessPeerName = null;// 对方的名字

	public String host = null;

	public int port = 4331;

	public ChessThread chessthread = new ChessThread(this);

	/**
	 * 棋盘 Panel 的构造函数
	 */
	public ChessPanel() 
	{
		setSize(440, 440);
		setLayout(null);
		setBackground(new Color(204, 204, 204));
		addMouseListener(this);
		add(statusLabel);
		statusLabel.setBounds(30, 5, 70, 24);
		add(statusText);
		statusText.setBounds(100, 5, 300, 24);
		statusText.setEditable(false);
	}

	/**
	 * 和服务器通信的函数
	 */
	public boolean connectServer(String ServerIP, int ServerPort)
			throws Exception 
	{

		try 
		{

			// 用参数创建一个 Socket的实例来完成和服务器之间的信息交换
			chessSocket = new Socket(ServerIP, ServerPort);
			inData = new DataInputStream(chessSocket.getInputStream());
			outData = new DataOutputStream(chessSocket.getOutputStream());
			chessthread.start();
			return true;

		}
		catch (IOException ex) 
		{

			statusText.setText("chessPad:connectServer:无法连接 \n");
		}
		return false;
	}

	/**
	 * 一方获胜时的对棋局的处理
	 */
	public void chessVictory(int chessColorWin) 
	{
		// 清除所有的棋子
		this.removeAll();
		
		// 将保存所有黑棋和白棋的位置坐标的数组清空，为下一盘棋做准备。
		for (int i = 0; i <= chessBlackCount; i++) 
		{
			chessBlack_x[i] = 0;
			chessBlack_y[i] = 0;
		}
		
		for (int i = 0; i <= chessWhiteCount; i++) 
		{
			chessWhite_x[i] = 0;
			chessWhite_y[i] = 0;
		}
		
		chessBlackCount = 0;
		chessWhiteCount = 0;
		add(statusText);
		statusText.setBounds(40, 5, 360, 24);
		
		// 如果黑棋获胜，计算双方获胜盘数，将双方的战绩比在状态文本框显示出来。
		if (chessColorWin == 1) 
		{
			chessBlackWin++;
			statusText.setText(
								"黑棋胜,黑:白为"
								+ chessBlackWin 
								+ ":" 
								+ chessWhiteWin
								+ ",重新开局,等待白棋下子..."
								);
		}
		// 白棋获胜，同上。
		else if (chessColorWin == -1) 
		{
			chessWhiteWin++;
			statusText.setText(
								"白棋胜,黑:白为" 
								+ chessBlackWin 
								+ ":" 
								+ chessWhiteWin
								+ ",重新开局,等待黑棋下子..."
								);
		}
	}

	/**
	 * 将各个棋子的坐标保存在数组里
	 */
	public void getLocation(int a, int b, int color) 
	{

		if (color == 1) 
		{
			chessBlack_x[chessBlackCount] = a * 20;
			chessBlack_y[chessBlackCount] = b * 20;
			chessBlackCount++;
		} 
		else if (color == -1) 
		{
			chessWhite_x[chessWhiteCount] = a * 20;
			chessWhite_y[chessWhiteCount] = b * 20;
			chessWhiteCount++;
		}
	}
	
	/**
	 * 绘制棋盘，将棋盘绘制成 19*19 的格局并将棋盘上应有的五个点绘制上去。
	 */

	public void paint(Graphics g) 
	{
		
		//画19条横线
		for (int i = 40; i <= 400; i = i + 20) 
		{
			g.drawLine(40, i, 400, i);
		}
		//画19条竖线
		for (int j = 40; j <= 400; j = j + 20) 
		{
			g.drawLine(j, 40, j, 400);
		}	
		//画关键的五个点
		g.fillOval(97, 97, 6, 6);
		g.fillOval(337, 97, 6, 6);
		g.fillOval(97, 337, 6, 6);
		g.fillOval(337, 337, 6, 6);
		g.fillOval(217, 217, 6, 6);

	}


	/**
	 * 落子的时候在己方绘制棋子
	 */
	public void chessPaint(int chessPoint_a, int chessPoint_b, int color) 
	{
		
		chessPoint_black chesspoint_black = new chessPoint_black(this);
		chessPoint_white chesspoint_white = new chessPoint_white(this);

		if (color == 1 && isMouseEnabled) 
		
		{
			// 当黑子落子时，记下此子的位置
			getLocation(chessPoint_a, chessPoint_b, color);
			// 判断是否获胜
			isWin = checkWin(chessPoint_a, chessPoint_b, color);
			if (isWin == false) 
			{
				// 如果没有获胜，向对方发送落子信息，并绘制棋子
				String msg = "/" + chessPeerName + " /chess "+ chessPoint_a + " " + chessPoint_b + " " + color;
				
				chessthread.sendMessage(msg);	//发送落子信息给服务器，让服务器转发给对方
				
				System.out.println("发送落子信息给服务器 黑棋落子位置信息"+msg);
				
				//在己方棋盘绘制棋子
				this.add(chesspoint_black);
				
				chesspoint_black.setBounds(
											chessPoint_a * 20 - 7,
											chessPoint_b * 20 - 7, 
											16, 
											16
										  );
				
				// 在状态文本框显示行棋信息
				statusText.setText("黑(第" + chessBlackCount + "步)"
									+ chessPoint_a 
									+ " " 
									+ chessPoint_b 
									+ ",请白棋下子"
									);
				
				isMouseEnabled = false;
			} 
			else 
			{
				// 如果获胜，直接调用 chessVictory 完成后续工作
				chessthread.sendMessage("/" 
										+ chessPeerName 
										+ " /chess "
										+ chessPoint_a 
										+ " " 
										+ chessPoint_b 
										+ " " 
										+ color
										);
				
				this.add(chesspoint_black);
				
				chesspoint_black.setBounds(	chessPoint_a * 20 - 7,
											chessPoint_b * 20 - 7, 
											16, 
											16
										  );
				
				chessVictory(1);
				isMouseEnabled = false;
			}
		}
		// 白棋落子，同黑棋类似处理
		else if (color == -1 && isMouseEnabled) 
		{
			getLocation(chessPoint_a, chessPoint_b, color);
			isWin = checkWin(chessPoint_a, chessPoint_b, color);
			if (isWin == false) 
			{
				String msg = "/" + chessPeerName + " /chess "+ chessPoint_a + " " + chessPoint_b + " " + color;
				
				chessthread.sendMessage(msg);
				
				System.out.println("白棋落子位置信息"+msg);
				this.add(chesspoint_white);

				chesspoint_white.setBounds(	chessPoint_a * 20 - 7,
											chessPoint_b * 20 - 7,
											16, 
											16
										  );
				
				statusText.setText(	"白(第" + chessWhiteCount + "步)"
									+ chessPoint_a 
									+ " " 
									+ chessPoint_b 
									+ ",请黑棋下子"
								  );
				
				isMouseEnabled = false;
			} 
			else 
			{
				chessthread.sendMessage(	"/" + chessPeerName + " /chess "
											+ chessPoint_a 
											+ " " 
											+ chessPoint_b 
											+ " " 
											+ color
										);
				
				this.add(chesspoint_white);
				
				chesspoint_white.setBounds(	chessPoint_a * 20 - 7,
											chessPoint_b * 20 - 7, 
											16, 
											16
										  );
				
				chessVictory(-1);
				isMouseEnabled = false;
			}
		}
	}

	/**
	 * 接收服务器转发来的的落子信息，调用此函数绘制棋子
	 */
	public void netChessPaint(int chessPoint_a, int chessPoint_b, int color) 
	{
		chessPoint_black chesspoint_black = new chessPoint_black(this);
		chessPoint_white chesspoint_white = new chessPoint_white(this);
		getLocation(chessPoint_a, chessPoint_b, color);
		if (color == 1) 
		{
			isWin = checkWin(chessPoint_a, chessPoint_b, color);
			if (isWin == false) 
			{

				this.add(chesspoint_black);
				
				chesspoint_black.setBounds(	chessPoint_a * 20 - 7,
											chessPoint_b * 20 - 7, 
											16, 16
										  );
				
				statusText.setText(	"黑(第" + chessBlackCount + "步)"
									+ chessPoint_a 
									+ " " 
									+ chessPoint_b 
									+ ",请白棋下子"
								  );
				
				isMouseEnabled = true;
			} 
			else 
			{
				this.add(chesspoint_black);
				
				chesspoint_black.setBounds(	chessPoint_a * 20 - 7,
											chessPoint_b * 20 - 7, 
											16, 
											16
										   );
				chessVictory(1);
				isMouseEnabled = true;
			}
		} 
		else if (color == -1) 
		{
			isWin = checkWin(chessPoint_a, chessPoint_b, color);

			if (isWin == false) 
			{
				this.add(chesspoint_white);
				
				chesspoint_white.setBounds(	chessPoint_a * 20 - 7,
											chessPoint_b * 20 - 7, 
											16, 
											16
										  );
				
				statusText.setText(	"白(第" + chessWhiteCount + "步)"
									+ chessPoint_a 
									+ " " 
									+ chessPoint_b 
									+ ",请黑棋下子"
								  );
				
				isMouseEnabled = true;
			} 
			else 
			{
				chessthread.sendMessage(	"/" + chessPeerName + " /victory " + color);
				
				this.add(chesspoint_white);
				
				chesspoint_white.setBounds(	chessPoint_a * 20 - 7,
											chessPoint_b * 20 - 7,
											16, 
											16
										  );
				
				chessVictory(-1);
				isMouseEnabled = true;
			}
		}
	}

	/**
	 * 当鼠标按下时响应的动作。记下当前鼠标点击的位置，即当前落子的位置。
	 */
		
	public void mousePressed(MouseEvent e) 
	{
		System.out.println("调用mousePressed（）~~~");
		if (e.getModifiers() == InputEvent.BUTTON1_MASK) 
		{
			chessPoint_x = (int) e.getX();
			chessPoint_y = (int) e.getY();
			int a = (chessPoint_x + 10) / 20;
			int b = (chessPoint_y + 10) / 20;
			
			if (
					   chessPoint_x / 20 < 2 
					|| chessPoint_y / 20 < 2
					|| chessPoint_x / 20 > 19 
					|| chessPoint_y / 20 > 19
				) 
			{
				//鼠标点到棋盘外，不做处理
			} 
			else 
			{	
				System.out.println("调用chessPaint（）绘制棋子~~~");
				//将鼠标所点位置画上棋子
				chessPaint(a, b, chessColor);
				
			}
		}
	}
	


	public void mouseReleased(MouseEvent e) 
	{
	}

	public void mouseEntered(MouseEvent e) 
	{
	}

	public void mouseExited(MouseEvent e) 
	{
	}

	public void mouseClicked(MouseEvent e) 
	{
	}

	

	/**
	 * 依据五子棋的行棋规则判断某方获胜
	 */
	public boolean checkWin(int a, int b, int checkColor) 
	{
		int step = 1, chessLink = 1, chessLinkTest = 1, chessCompare = 0;
		
		if (checkColor == 1) 
		{
			chessLink = 1;
			for (step = 1; step <= 4; step++) 
			{
				for	(chessCompare =	0;	chessCompare <=	chessBlackCount;chessCompare++) 
				{
					if (	((a + step) * 20 == chessBlack_x[chessCompare])
							&& 
							((b * 20) == chessBlack_y[chessCompare])
						) 
					{
						chessLink = chessLink + 1;
						if (chessLink == 5) 
						{
							return (true);
						}
					}
				}
				if (chessLink == (chessLinkTest + 1))
				{
					chessLinkTest++;
				}				 
				else
				{
					break;
				}				
				
			}
			 
			for (step = 1; step <= 4; step++) 
			{
				for	(chessCompare = 0;chessCompare <= chessBlackCount;chessCompare++) 
				{
					if (	((a - step) * 20 == chessBlack_x[chessCompare])
							&&
							(b * 20 == chessBlack_y[chessCompare])
						) 
					{
						chessLink++;
						if (chessLink == 5) 
						{
							return (true);
						}
					}
				}
			 
				if (chessLink == (chessLinkTest + 1))
				{
					chessLinkTest++;
				}			 
				else
				{
					break;
				}
			}	
			 
			chessLink = 1;
			chessLinkTest = 1;
			for (step = 1; step <= 4; step++) 
			{
				for	(chessCompare =	0;	chessCompare <=	chessBlackCount;chessCompare++) 
				{
					if (
							(a * 20 == chessBlack_x[chessCompare])
							&& 
							((b + step) * 20 == chessBlack_y[chessCompare])
						) 
					{
						chessLink++;
						if (chessLink == 5) 
						{
							return (true);
						}
					}
				}
				
				if (chessLink == (chessLinkTest + 1))
				{
					chessLinkTest++;
				}				 
				else
				{
					break;
				}
			}
			 
			for (step = 1; step <= 4; step++) 
			{
				for	(chessCompare =	0;	chessCompare <=	chessBlackCount;chessCompare++) 
				{
					if (	(a * 20 == chessBlack_x[chessCompare])
							&& 
							((b - step) * 20 == chessBlack_y[chessCompare])) 
					{
						chessLink++;
						if (chessLink == 5) 
						{
							return (true);
						}
					}
				}
				
				if (chessLink == (chessLinkTest + 1))
				{
					chessLinkTest++;
				}				 
				else
				{
					break;
				}			
			
			}
			
			chessLink = 1;
			chessLinkTest = 1;
			for (step = 1; step <= 4; step++) 
			{
				for	(chessCompare =	0;	chessCompare <=	chessBlackCount;chessCompare++) 
				{				 
					if (	((a - step) * 20 == chessBlack_x[chessCompare])
							&& 
							((b + step) * 20 == chessBlack_y[chessCompare])) 
					{
						chessLink++;
						if (chessLink == 5) 
						{
							return (true);
						}
					}
				}
				if (chessLink == (chessLinkTest + 1))
				{
					chessLinkTest++;
				}
				 
				else
				{
					break;
				}
				
			
			}
			
			for (step = 1; step <= 4; step++) 
			{
				for	(chessCompare =	0;	chessCompare <=	chessBlackCount;chessCompare++) 
				{
					if (	((a + step) * 20 == chessBlack_x[chessCompare])
							&& 
							((b - step) * 20 == chessBlack_y[chessCompare])) 
					{
						chessLink++;
						if (chessLink == 5) 
						{
							return (true);
						}
					}
				}
				if (chessLink == (chessLinkTest + 1))
				{
					chessLinkTest++;
				}				 
				else
				{
					break;
				}
			}
			 
			 
			chessLink = 1;
			chessLinkTest = 1;
			
			for (step = 1; step <= 4; step++) 
			{
				for	(chessCompare =	0;	chessCompare <=	chessBlackCount;chessCompare++) 
				{
					if (	((a + step) * 20 == chessBlack_x[chessCompare])
							&& 
							((b + step) * 20 == chessBlack_y[chessCompare])) 
					{
						chessLink++;
						if (chessLink == 5) 
						{
							return (true);
						}
					}
				}
				if (chessLink == (chessLinkTest + 1))
				{
					chessLinkTest++;
				}
				else
				{
					break;
				}				
			
			}
			
			for (step = 1; step <= 4; step++) 
			{
				for	(chessCompare =	0;	chessCompare <=	chessBlackCount;chessCompare++) 
				{
					if (	((a - step) * 20 == chessBlack_x[chessCompare])
							&& 
							((b - step) * 20 == chessBlack_y[chessCompare])) 
					{
						chessLink++;
						if (chessLink == 5) 
						{
							return (true);
						}
					}
				}
				if (chessLink == (chessLinkTest + 1))
				{
					chessLinkTest++;
				} 
				else
				{
					break;
				}
			}
		 
		} 
		
		else if (checkColor == -1) 
		{
			chessLink = 1;
			for (step = 1; step <= 4; step++) 
			{
				for	(chessCompare =	0;	chessCompare <=	chessWhiteCount;chessCompare++) 
				{
					if (	((a + step) * 20 == chessWhite_x[chessCompare])
							&& 
							(b * 20 == chessWhite_y[chessCompare])) 
					{
						chessLink++;
						if (chessLink == 5) 
						{
							return (true);
						}
					}
				}
				if (chessLink == (chessLinkTest + 1))
				{
					chessLinkTest++;
				}
				 
				else
				{
					break;
				}
			}	
			 
			for (step = 1; step <= 4; step++) 
			{
				for	(chessCompare =	0;	chessCompare <=	chessWhiteCount;chessCompare++) 
				{
					if (	((a - step) * 20 == chessWhite_x[chessCompare])
							&& 
							(b * 20 == chessWhite_y[chessCompare]))
					{
						chessLink++;
						if (chessLink == 5) 
						{						 
							return (true);
						}
					}
				}
				if (chessLink == (chessLinkTest + 1))
				{
					chessLinkTest++;
				}
				else
				{
					break;
				}
			} 
			
			chessLink = 1;
			chessLinkTest = 1;
			for (step = 1; step <= 4; step++) 
			{
				for	(chessCompare =	0;	chessCompare <=	chessWhiteCount;chessCompare++) 
				{
					if (	(a * 20 == chessWhite_x[chessCompare])
							&& ((b + step) * 20 == chessWhite_y[chessCompare])
						) 
					{
						chessLink++;
						if (chessLink == 5) 
						{
							return (true);
						}
					}
				}
				if (chessLink == (chessLinkTest + 1))
				{
					chessLinkTest++;
				} 
				else
				{
					break;
				}
			}	
			
			for (step = 1; step <= 4; step++) 
			{
				for	(chessCompare =	0;	chessCompare <=	chessWhiteCount;chessCompare++)
				{
					if (	(a * 20 == chessWhite_x[chessCompare])
							&& ((b - step) * 20 == chessWhite_y[chessCompare])
						) 
					{
						chessLink++;
						if (chessLink == 5) 
						{
							return (true);
						}
					}
				}
				if (chessLink == (chessLinkTest + 1))
				{
					chessLinkTest++;
				}				 
				else
				{
					break;
				}
			
			}	 
			
			chessLink = 1;			 
			chessLinkTest = 1;
			for (step = 1; step <= 4; step++) 
			{
				for	(chessCompare =	0;	chessCompare <=	chessWhiteCount;chessCompare++) 
				{
					if (	((a - step) * 20 == chessWhite_x[chessCompare])
							&& ((b + step) * 20 == chessWhite_y[chessCompare])
						) 
					{
						chessLink++;
						if (chessLink == 5) 
						{
							return (true);
						}
					}
				}
				if (chessLink == (chessLinkTest + 1))
				{
					chessLinkTest++;
				}				 
				else
				{
					break;
				}		
			
			}
			 
			for (step = 1; step <= 4; step++) 
			{
				for	(chessCompare =	0;	chessCompare <=	chessWhiteCount;chessCompare++) 
				{
					if (	((a + step) * 20 == chessWhite_x[chessCompare])
							&& ((b - step) * 20 == chessWhite_y[chessCompare])
							) 
					{
						chessLink++;
						if (chessLink == 5) 
						{
							return (true);
						}
					}
				}
				if (chessLink == (chessLinkTest + 1))
				{
					chessLinkTest++;
				} 
				else
				{
					break;
				}
			}
			 	 
			chessLink = 1;
			chessLinkTest = 1;
			for (step = 1; step <= 4; step++) 
			{
				for	(chessCompare =	0;	chessCompare <=	chessWhiteCount;chessCompare++) 
				{
					if (	((a + step) * 20 == chessWhite_x[chessCompare])
							&& 
							((b + step) * 20 == chessWhite_y[chessCompare])
						) 
					{
						chessLink++;
						if (chessLink == 5) 
						{
							return (true);
						}
					}
				}
				if (chessLink == (chessLinkTest + 1))
				{
					chessLinkTest++;
				}				 
				else
				{
					break;
				}			
			
			}	
			
			for (step = 1; step <= 4; step++) 
			{
				for	(chessCompare =	0;	chessCompare <=	chessWhiteCount;chessCompare++) 
				{
					if (	((a - step) * 20 == chessWhite_x[chessCompare])
							&& ((b - step) * 20 == chessWhite_y[chessCompare])
						) 
					{
						chessLink++;
						if (chessLink == 5) 
						{
							return (true);
						}
					}
				}
				if (chessLink == (chessLinkTest + 1))
				{
					chessLinkTest++;
				}
				else
				{
					break;
				}
				
			}
		}
		return (false);
	}


}

/**
 * 表示黑子的类
 */

class chessPoint_black extends Canvas 
{
	ChessPanel chesspad = null;

	chessPoint_black(ChessPanel p) 
	{
		setSize(20, 20);
		chesspad = p;
	}

	public void paint(Graphics g) 
	{
		g.setColor(Color.black);
		g.fillOval(0, 0, 14, 14);
	}

}

/**
 * 表示白子的类
 */

class chessPoint_white extends Canvas 
{
	ChessPanel chesspad = null;

	chessPoint_white(ChessPanel p) 
	{
		setSize(20, 20);

		chesspad = p;
	}

	public void paint(Graphics g) 
	{
		g.setColor(Color.white);
		g.fillOval(0, 0, 14, 14);
	}

}
