package com.icss.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;

import javax.swing.JLabel;
import javax.swing.JPanel;
/**
* 显示服务器及用户信息的 Panel 类
*/
public class MessageServerPanel extends Panel
{
	TextArea messageBoard = new TextArea("", 22, 50, TextArea.SCROLLBARS_VERTICAL_ONLY);
	
	JLabel statusLabel = new JLabel("当前连接数:", Label.LEFT); 
	JPanel boardPanel = new JPanel();// 主显示区 Panel
	JPanel statusPanel = new JPanel();// 连接状态 Panel
	 
	MessageServerPanel() 
	{
		setSize(350, 300);
		setBackground(new Color(204, 204, 204));
		setLayout(new BorderLayout()); 
		
		boardPanel.setLayout(new FlowLayout()); 
		boardPanel.setSize(210, 210);
		
		statusPanel.setLayout(new BorderLayout()); 
		statusPanel.setSize(210, 50); 
		
		boardPanel.add(messageBoard); 
		statusPanel.add(statusLabel, BorderLayout.WEST); 
		
		add(boardPanel, BorderLayout.CENTER);
		add(statusPanel, BorderLayout.NORTH);
	}

}
