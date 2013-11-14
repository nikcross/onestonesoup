package org.one.stone.soup.sds;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.one.stone.soup.core.FileHelper;
import org.one.stone.soup.core.JSONHelper;
import org.one.stone.soup.core.StringHelper;
import org.one.stone.soup.core.data.EntityTree;
import org.one.stone.soup.core.data.EntityTree.TreeEntity;
import org.one.stone.soup.core.data.KeyValuePair;
import org.one.stone.soup.process.CommandLineTool;


public class SimpleDeviceServer extends CommandLineTool implements Runnable{
	
	private ServerSocket serverSocket;
	private boolean running=false;
	private int port=8080;
	private String address="localhost";
	private String pageFile;
	private Map<String,Object> services;
	
	public static void main(String[] args) {
		new SimpleDeviceServer(args);
	}

	public SimpleDeviceServer(String[] args) {
		super(args);
	}

	public SimpleDeviceServer() {
		super(new String[]{"-N"});
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public int getMaximumArguments() {
		return 0;
	}

	@Override
	public String getUsage() {
		return "[-P=port] [-H=host] [-N=no start]";
	}
	
	@Override
	public void process() {
		if(hasOption("N")) {
			start();
		}
	}
	
	public void start() {
		services = new HashMap<String,Object>();
		
		new Thread(this,"SimpleDeviceServer").start();
	}
	
	public void run() {
		if(running==true) {
			return;
		}
		running = true;
		
		if(hasOption("P")) {
			port = Integer.parseInt(getOption("P"));
		}
		if(hasOption("H")) {
			address = getOption("H");
		}
	
		try{
			serverSocket = new ServerSocket(port,10,InetAddress.getByName(address));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			running = false;
			return;
		} catch (IOException e) {
			e.printStackTrace();
			running = false;
			return;
		}
		
		while(running==true) {
			
			try {
				Socket socket = serverSocket.accept();
				EntityTree header = parseHeader(socket);
				if(header.getAttribute("resource").equals("/")) {
					sendPage(header,socket);
				} else if(header.getAttribute("resource").startsWith("/service?")) {
					processRequest(header,socket);
				} else if(header.getAttribute("resource").startsWith("/favicon.ico")) {
					sendIcon(header,socket);
				} else {
					send404(header,socket);
				}
				socket.close();
				
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	private EntityTree parseHeader(Socket socket) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		EntityTree header = new EntityTree("http-header");
		
		String line = reader.readLine();
		String[] parts = line.split(" "); 
		header.setAttribute("method",parts[0].trim().toLowerCase());
		header.setAttribute("resource",parts[1].trim());
		header.setAttribute("version",parts[2].trim().toLowerCase());
		
		line = reader.readLine();
		while(line!=null && line.length()!=0){
			KeyValuePair kvp = KeyValuePair.parseKeyAndValue(line, ":");
			header.addChild( kvp.getKey() ).setValue( kvp.getValue() );
			
			line = reader.readLine();
		}
		
		if(header.getAttribute("method").equals("post")) {
			TreeEntity entity = header.addChild("post-data");
			StringBuilder postData = new StringBuilder();
			line = reader.readLine();
			while(line!=null) {
				postData.append(line);
				line = reader.readLine();
			}
			entity.setValue(postData.toString());
		}
		
		return header;
	}
	
	private void sendPage(EntityTree header,Socket socket) throws IOException {
		StringBuilder data = new StringBuilder();
		data.append("HTTP/1.1 200 OK\n");
		data.append("Server: Simple Sevice Server\n");
		data.append("Content-Length: "+new File(pageFile).length()+"\n");
		data.append("Content-Type: text/html\n\n");
		
		data.append( FileHelper.loadFileAsString(pageFile) );
		FileHelper.saveStringToOutputStream( data.toString(), socket.getOutputStream() );
	}
	
	private void send404(EntityTree header,Socket socket) throws IOException {
		StringBuilder data = new StringBuilder();
		data.append("HTTP/1.1 404 Not Found\n");
		data.append("Server: Simple Sevice Server\n\n");
		
		data.append( FileHelper.loadFileAsString(pageFile) );
		FileHelper.saveStringToOutputStream( data.toString(), socket.getOutputStream() );
	}
	
	private void sendIcon(EntityTree header,Socket socket) throws IOException {
		File icon = new File(new File(pageFile).getParentFile().getAbsolutePath()+"favicon.ico");
		if(icon.exists()==false) {
			send404(header, socket);
		}
		StringBuilder data = new StringBuilder();
		data.append("HTTP/1.1 200 OK\n");
		data.append("Server: Simple Sevice Server\n");
		data.append("Content-Length: "+icon.length()+"\n");
		data.append("Content-Type: text/html\n\n");
		
		data.append( FileHelper.loadFileAsString(icon) );
		FileHelper.saveStringToOutputStream( data.toString(), socket.getOutputStream() );
	}
	
	private void processRequest(EntityTree header,Socket socket) {
		String request = header.getAttribute("resource");
		request = StringHelper.after(request,"/service?");
		String[] parts = request.split("&");
		TreeEntity parameters = header.addChild("parameters");
		for(String part: parts) {
			KeyValuePair kvp = KeyValuePair.parseKeyAndValue(part);
			parameters.addChild(kvp.getKey()).setValue(kvp.getValue());
		}
		String serviceName = parameters.getChild("name").getValue();
		String serviceMethod = parameters.getChild("method").getValue();
		String[] serviceValues = parameters.getChild("values").getValue().split(",");

		try {
			Object response = callServiceMethod(serviceName, serviceMethod, serviceValues, header, socket);
			String responseData = null;
			if(response instanceof String) {
				responseData = (String) response;
			} else {
				JSONHelper.toJSON(response);
			}
			
			StringBuilder responseHeader = new StringBuilder();
			responseHeader.append("HTTP/1.1 200 OK\n");
			responseHeader.append("Server: Simple Sevice Server\n");
			responseHeader.append("Content-Length: "+responseData.length()+"\n");
			responseHeader.append("Content-Type: application/json\n\n");
			responseHeader.append(responseData);
			
			FileHelper.saveStringToOutputStream(responseData.toString(),socket.getOutputStream());
			
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public interface Factory {
		public Object newInstance();
	}
	
	private Object callServiceMethod(String serviceName,String serviceMethod,String[] serviceValues, EntityTree header,Socket socket) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		
		Object service = services.get(serviceName);
		if(service instanceof Factory) {
			service = ((Factory)service).newInstance();
			try{
				Method setter = service.getClass().getMethod("setHeader", EntityTree.class);
				setter.invoke(service,header);
			} catch(NoSuchMethodException e) {}
			try{
				Method setter = service.getClass().getMethod("setSocket", Socket.class);
				setter.invoke(service,socket);
			} catch(NoSuchMethodException e) {}
		}
		
		if(serviceValues.length==0) {
			return service.getClass().getMethod(serviceMethod,null).invoke(service);
		} else if(serviceValues.length==1) {
			return service.getClass().getMethod(serviceMethod,String.class).invoke(service,serviceValues[0]);
		} else {
			return service.getClass().getMethod(serviceMethod,String[].class).invoke(service,serviceValues);
		}
	}
	
	public void stop() {
		running = false;
	}
	
	public String[] listServices() {
		return services.keySet().toArray(new String[]{});
	}

	public Object getService(String key) {
		return services.get(key);
	}
	
	public void registerService(String key,String serviceClass) {
		Object service;
		try {
			service = Class.forName(serviceClass).newInstance();
			registerService(key, service);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}	
	
	public void registerService(String key,Object service) {
		services.put(key, service);
	}
	
	public void setPort(int port) throws Exception {
		if(running) {
			throw new Exception("Port set too late. Server already running.");
		}
		this.port = port;
	}

	public void setAddress(String address) throws Exception {
		if(running) {
			throw new Exception("Address set too late. Server already running.");
		}
		this.address = address;
	}

	public boolean isRunning() {
		return running;
	}
	
	public String getPageFile() {
		return pageFile;
	}
	
	public void setPageFile(String pageFile) {
		this.pageFile = pageFile;
	}
	
	public String getPort() {
		return ""+port;
	}
	
	public String getAddress() {
		return address;
	}
}
