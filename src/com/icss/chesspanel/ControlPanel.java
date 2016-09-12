package com.icss.chesspanel;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Label;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 
 * 控制 Panel。此 Panel 上的按钮如其名，完成相应的功能。
 */
public class ControlPanel extends JPanel 
{
	public JLabel IPlabel = new JLabel("服务器 IP:", Label.LEFT);

	public JTextField inputIP = new JTextField("localhost", 10);

	public JButton connectButton = new JButton("连接服务器");
	public JButton creatGameButton = new JButton("新建游戏");
	public JButton joinGameButton = new JButton("进入游戏");
	public JButton cancelGameButton = new JButton("退出游戏");
	public JButton exitGameButton = new JButton("关闭程序");

	// 构造函数，负责 Panel 的初始布局。
	public ControlPanel() 
	{
		setLayout(new FlowLayout(FlowLayout.LEFT));
		setBackground(new Color(204, 204, 204));

		add(IPlabel);
		add(inputIP);
		add(connectButton);
		add(creatGameButton);
		add(joinGameButton);
		add(cancelGameButton);
		add(exitGameButton);
	}

}
