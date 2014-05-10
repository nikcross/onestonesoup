package org.one.stone.soup.slab;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.one.stone.soup.process.CommandLineTool;

public class Ahoy extends CommandLineTool implements Runnable{


	public static void main(String[] args) throws UnknownHostException, IOException {
		new Ahoy(args);
	}
	
	private static final int MAX_MESSAGE_SIZE = 2000;
	
	private MulticastSocket ms;
	private String address = "228.1.2.3";
	private int port = 1234;
	private InetAddress group;
	private String packetLoad;
	
	public Ahoy(String[] args) {
		super(args);
		start();
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public int getMaximumArguments() {
		return 100;
	}

	@Override
	public String getUsage() {
		return "ahoy -S(erver) -C(lient) [type ,type, ...]";
	}

	@Override
	public void process() {
	}
	
	public void start() {
		createPacketLoad();
		
		try {
			group = InetAddress.getByName(address);
			ms = new MulticastSocket( port );
			ms.joinGroup( group );
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(hasOption("S")) {
			new Thread(this).start();
		} else if(hasOption("C")) {
			String data = "ahoy "+packetLoad;
			DatagramPacket packet = new DatagramPacket(data.getBytes(),data.length(),group,port);
			try {
				ms.send(packet);
				System.out.println("Client packet sent "+data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void run() {
		
		while(true) {
			byte[] data = new byte[MAX_MESSAGE_SIZE];
			DatagramPacket packet = new DatagramPacket(data,data.length);
			try {
				System.out.println("Server waiting for packet");
				ms.receive(packet);
				String message = new String(packet.getData(),0,packet.getLength());
				processMessage(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void processMessage(String message) {
		System.out.println("Packet received "+message);
		
		List<String> localParts = Arrays.asList(packetLoad.split(" "));
		String[] messageParts = message.split(" ");
		
		if(messageParts.length<2 || messageParts[0].equals("ahoy")==false) {
			return;
		}
		if(messageParts[messageParts.length-1].equals(localParts.get(localParts.size()-1))) {
			return;
		}
		
		int score = 0;
		for(int i=1;i<messageParts.length-1;i++) {
			if(localParts.contains(messageParts[i])) {
				score++;
			}
		}
		if(score==messageParts.length-2) {
			String data = "yoha "+packetLoad;
			DatagramPacket packet = new DatagramPacket(data.getBytes(),data.length(),group,port);
			try {
				ms.send(packet);
				System.out.println("Packet sent "+data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void createPacketLoad() {
		
		packetLoad="";
		for(String element: getParameters()) {
			if(element.length()>0) {
				element+=" ";
			}
			packetLoad+=element;
		}
		try {
			packetLoad+=InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		System.out.println("Packet load "+packetLoad);
	}
}
