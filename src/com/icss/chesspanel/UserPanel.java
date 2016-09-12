package com.icss.chesspanel;

import java.awt.BorderLayout;
import java.awt.List;

import javax.swing.JPanel;

/**
 * 
 * 用户列表 Panel。此 Panel 维护一个服务器的当前用户列表， 
 * 所有的用户名都将显示在列表中。
 */
public class UserPanel extends JPanel {
	
	public List userList = new List(5); // 可滚动的文本项列表
	
	public UserPanel() 
	{
		this.setLayout(new BorderLayout());
		for (int i = 0; i < 30; i++) 
		{
			userList.add(i + "." + "no player now");
		}

		this.add(userList, BorderLayout.CENTER); // 添加到面板的中间区域
	}
}
