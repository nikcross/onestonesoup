package org.onestonesoup.slab;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class BlockIt {

	public static void main(String[] args) {
		new BlockIt();
		
		System.exit(0);
	}
	
	public BlockIt() {
		JFrame frame = new JFrame();
		frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		frame.setUndecorated(true);
		frame.setVisible(true);
		frame.getContentPane().setBackground(Color.BLUE);
		frame.setOpacity((float)0.8);
		
		JLabel time = new JLabel();
		time.setForeground(Color.WHITE);
		time.setFont(new Font("Arial",Font.BOLD,100));
		
		frame.add(time);
		
		for(int i=0;i<10;i++) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			time.setText("T:"+new Date());
		}
		frame.setVisible(false);
	}
}
