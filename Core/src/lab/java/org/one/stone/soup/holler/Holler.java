package org.one.stone.soup.holler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

import org.one.stone.soup.core.data.EntityTree;
import org.one.stone.soup.core.data.XmlHelper;
import org.one.stone.soup.core.data.XmlHelper.XmlParseException;

public class Holler {

	public static void main(String[] args) {
		
	}
	
	private class HollerServer implements Runnable {
		private boolean running = false;
		public void run() {
			running = true;
			while(running) {
				byte[] data = new byte[1000];
				DatagramPacket datagram = new DatagramPacket(data,data.length);
				try {
					ms.receive(datagram);
					String message = new String(data,0,datagram.getLength());
					try {
						respond(message);
					} catch (XmlParseException e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			running = false;
		}
	}
	
	private HollerServer hollerServer;
	private MulticastSocket ms;
	private String address = "228.5.6.7";
	private InetAddress group;
	private int port = 1234;
	private String name;
	private List<String> types = new ArrayList<String>();
	private List<EntityTree> responses = new ArrayList<EntityTree>();
	
	public Holler(String name,String[] types) throws IOException {
		group = InetAddress.getByName(address);
		ms = new MulticastSocket( port );
		ms.joinGroup( group );
		hollerServer = new HollerServer();
		new Thread(hollerServer).start();
	}
	
	public void holler(String getName,String[] getTypes, long time) throws IOException {
		EntityTree request = new EntityTree("holler");
		request.addChild("name").setValue(getName);
		for(String type: getTypes) {
			request.addChild("type").setValue(type);
		}
		String holler = XmlHelper.toXml(request);
		DatagramPacket packet = new DatagramPacket( holler.getBytes(),holler.length(),group,port );
		ms.send( packet );
		
		long start = System.currentTimeMillis();
		
		while(time < System.currentTimeMillis()-start) {
			while(responses.isEmpty()==true) {
				EntityTree response = responses.remove(0);
				System.out.println(XmlHelper.toXml(response));
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
	}
	
	public void respond(String message) throws XmlParseException, IOException {
		EntityTree request = XmlHelper.parseElement(message);
		if(request.getName().equals("here")) {
			//if id = last request
			//capture the response to return to requesting method call
			responses.add(request);
		} else if(request.getName().equals("holler")) {
			//if name match or type match
			//send information
			EntityTree response = new EntityTree("here");
			response.addChild("name").setValue(name);
			for(String type: types) {
				response.addChild("type").setValue(type);
			}
			String holler = XmlHelper.toXml(response);
			DatagramPacket packet = new DatagramPacket( holler.getBytes(),holler.length(),group,port );
			ms.send( packet );
		}
	}
}
