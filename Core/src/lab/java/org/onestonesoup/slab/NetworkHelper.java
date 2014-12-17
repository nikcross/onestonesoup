package org.onestonesoup.slab;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.onestonesoup.core.StringHelper;

public class NetworkHelper {

	public static void main(String[] args) throws SocketException {
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while(interfaces.hasMoreElements()) {
			NetworkInterface i = interfaces.nextElement();
			if(i.isVirtual()) {
				continue;
			}
			if(i.isLoopback()) {
				continue;
			}
			if(i.isPointToPoint()) {
				continue;
			}
			byte[] macAddress = i.getHardwareAddress();
			if(macAddress == null) {
				continue;
			}
			if(i.getInetAddresses().hasMoreElements()==false) {
				continue;
			}

			
			System.out.println(i.getDisplayName());
			System.out.println( " Name:"+i.getName() );
			System.out.println( " MAC Address:"+StringHelper.asHex(macAddress) );
			Enumeration<InetAddress> inetAddresses = i.getInetAddresses();
			while(inetAddresses.hasMoreElements()) {
				InetAddress inetAddress = inetAddresses.nextElement();
				System.out.println(" Internet Address:"+StringHelper.asHex(inetAddress.getAddress())+"<"+inetAddress.getHostName()+">");
			}
			System.out.println( "  Is Loop back:"+i.isLoopback() );
			System.out.println( "  Can Multicast:"+i.supportsMulticast() );
			System.out.println("");
		}
	}
}
