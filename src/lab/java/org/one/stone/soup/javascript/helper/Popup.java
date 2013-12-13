package org.one.stone.soup.javascript.helper;

import javax.swing.JOptionPane;

public class Popup {

	public static void alert(String message) {
		JOptionPane.showMessageDialog(null, message);
	}
	
	public static Boolean confirm(String message) {
		int result = JOptionPane.showConfirmDialog(null, message);
		if(result==JOptionPane.YES_OPTION) {
			return true;
		} else if(result==JOptionPane.NO_OPTION) {
			return false;
		} else {
			return null;
		}
	}
	
	public static String requestInput(String message) {
		return JOptionPane.showInputDialog(null, message);
	}

	public static String requestChoice(String message,String[] options) {
		int result = JOptionPane.showOptionDialog(null, message, "", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		return options[result];
	}
}
