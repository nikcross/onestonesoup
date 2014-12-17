package org.onestonesoup.core;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.StringReader;

public class ClipBoard {

	public static String fromClipboard() {   
		   
		 try{   
		    Clipboard clipB = Toolkit.getDefaultToolkit().getSystemClipboard();   
		   
		    Transferable dataT = clipB.getContents(null);   
		/*  DataFlavor[] flavors = dataT.getTransferDataFlavors();  
		    for(int loop=0;loop<flavors.length;loop++)  
		    {  
		        System.out.println( flavors[loop].getMimeType()+" : "+flavors[loop].getHumanPresentableName() );  
		    }*/   
		    StringBuffer data = new StringBuffer();   
		    Object dataTr = dataT.getTransferData(DataFlavor.stringFlavor);   
		   
		    if(dataTr instanceof String)   
		    {   
		        data.append(dataTr.toString());   
		    }   
		    else if(dataTr instanceof StringReader)   
		    {   
		    StringReader dataR = (StringReader)dataTr;   
		   
		    int in = dataR.read();   
		   
		    while(in!=-1)   
		    {   
		        data.append((char)in);   
		   
		        in = dataR.read();   
		    }   
		    }   
		    return data.toString();   
		 }   
		 catch(Exception e)   
		 {   
		     return null;   
		 }   
		}   
		public static void toClipboard(String data) {   
		   
		    Clipboard clipB = Toolkit.getDefaultToolkit().getSystemClipboard();   
		   
		    StringSelection dataS = new StringSelection(data);   
		   
		    clipB.setContents(dataS,dataS);   
		} 
}

