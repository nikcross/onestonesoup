package org.onestonesoup.ahoyahoy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.onestonesoup.core.data.EntityTree;
import org.onestonesoup.core.data.JsonHelper;

public class AhoyAhoy {

	public static void main(String[] args) {

	}

	private class AhoyAhoyServer implements Runnable {
		private boolean running = false;
		public void run() {
			running = true;
			while(running) {
				byte[] data = new byte[1000];
				DatagramPacket datagram = new DatagramPacket(data,data.length);
				try {
					ms.receive(datagram);
					String message = new String(data,0,datagram.getLength());
					respond(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			running = false;
		}
	}

	private AhoyAhoyServer ahoyAhoyServer;
	private MulticastSocket ms;
	private String address = "228.5.6.7";
	private InetAddress group;
	private int port = 1234;
	private String name;
	private List<String> types = new ArrayList<String>();
	private List<EntityTree> responses = Collections.synchronizedList(new ArrayList<EntityTree>());

	public AhoyAhoy(String name,String[] types) throws IOException {
		group = InetAddress.getByName(address);
		ms = new MulticastSocket( port );
		ms.joinGroup( group );
		ahoyAhoyServer = new AhoyAhoyServer();
		new Thread(ahoyAhoyServer).start();
	}

	public void ahoyAhoy(String getName,String[] getTypes, long time) throws IOException {
		EntityTree request = new EntityTree("message");
		request.addChild("type").setValue("ahoyahoy");
		request.addChild("name").setValue(getName);
		EntityTree.TreeEntity typesNode = request.addChild("types");
		typesNode.setAttribute("array", "true");
		typesNode.setAttribute("length", String.valueOf(getTypes.length));
		for (int i = 0; i < getTypes.length; i++) {
			typesNode.addChild(String.valueOf(i)).setValue(getTypes[i]);
		}
		String ahoyAhoy = JsonHelper.stringifyObject(request.getRoot());
		DatagramPacket packet = new DatagramPacket( ahoyAhoy.getBytes(),ahoyAhoy.length(),group,port );
		ms.send( packet );

		long start = System.currentTimeMillis();

		while(System.currentTimeMillis()-start < time) {
			synchronized(responses) {
				while(!responses.isEmpty()) {
					EntityTree response = responses.remove(0);
					System.out.println(JsonHelper.stringifyObject(response.getRoot()));
				}
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
	}

	private String getLocalIp() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (IOException e) {
			return "unknown";
		}
	}

	private String getLocalMac() {
		try {
			NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
			if (ni == null) return "unknown";
			byte[] mac = ni.getHardwareAddress();
			if (mac == null) return "unknown";
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				if (i > 0) sb.append(":");
				sb.append(String.format("%02X", mac[i]));
			}
			return sb.toString();
		} catch (IOException e) {
			return "unknown";
		}
	}

	public void respond(String message) throws IOException {
		EntityTree request = JsonHelper.parseObject("message", message);
		EntityTree.TreeEntity typeNode = request.getChild("type");
		if (typeNode == null) return;
		String type = typeNode.getValue();
		if (type.equals("here")) {
			//if id = last request
			//capture the response to return to requesting method call
			responses.add(request);
		} else if (type.equals("ahoyahoy")) {
			//if name match or type match
			//send information
			EntityTree response = new EntityTree("message");
			response.addChild("type").setValue("here");
			response.addChild("name").setValue(name);
			response.addChild("ip").setValue(getLocalIp());
			response.addChild("mac").setValue(getLocalMac());
			EntityTree.TreeEntity typesNode = response.addChild("types");
			typesNode.setAttribute("array", "true");
			typesNode.setAttribute("length", String.valueOf(types.size()));
			for (int i = 0; i < types.size(); i++) {
				typesNode.addChild(String.valueOf(i)).setValue(types.get(i));
			}
			String json = JsonHelper.stringifyObject(response.getRoot());
			DatagramPacket packet = new DatagramPacket( json.getBytes(),json.length(),group,port );
			ms.send( packet );
		}
	}
}
