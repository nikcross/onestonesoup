package org.one.stone.soup.sds;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.one.stone.soup.core.FileHelper;
import org.one.stone.soup.core.JSONHelper;
import org.one.stone.soup.core.StringHelper;
import org.one.stone.soup.core.data.EntityTree;
import org.one.stone.soup.core.data.EntityTree.TreeEntity;
import org.one.stone.soup.core.data.KeyValuePair;
import org.one.stone.soup.process.CommandLineTool;

import sun.org.mozilla.javascript.internal.NativeJavaObject;
import sun.org.mozilla.javascript.internal.NativeObject;
import sun.org.mozilla.javascript.internal.Scriptable;


public class SimpleDeviceServer extends CommandLineTool implements Runnable{
	
	private ServerSocket serverSocket;
	private boolean running=false;
	private int port=8080;
	private String address="localhost";
	private String pageFile;
	private Map<String,Object> services;
	
	private class ServerThread implements Runnable {
		
		private Socket socket;
		
		public void process(Socket socket) {
			this.socket = socket;
			new Thread(this,"SDS Server Thread").start();
		}
		
		public void run() {
			
			try {
				processSocket(socket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private Authenticator authenticator;
	
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
		services = new HashMap<String,Object>();
		if(hasOption("N")==false) {
			start();
		}
	}
	
	public void start() {		
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
				new ServerThread().process(socket);
				
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	private void processSocket(Socket socket) throws IOException {
		
		try{
		EntityTree header = parseHeader(socket);
		if(header==null) {
			return;
		}
		if(header.getAttribute("resource").equals("/")) {
			sendPage(header,socket);
		} else if(header.getAttribute("resource").startsWith("/service?")) {
			processRequest(header,socket);
		} else if(header.getAttribute("resource").lastIndexOf(".")!=-1) {
			sendResource(header,socket);
		} else {
			send404(header,socket);
		}
		socket.close();
		} catch (Throwable t) {
			send500(socket,t);
		}
	}
	
	private EntityTree parseHeader(Socket socket) throws IOException {
		//BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()),1);
		InputStream iStream = socket.getInputStream();
		EntityTree header = new EntityTree("http-header");
		
		String line = readLine(iStream);
		if(line==null || line.length()==0) {
			socket.close();
			return null;
		}
		String[] parts = line.split(" "); 
		header.setAttribute("method",parts[0].trim().toLowerCase());
		header.setAttribute("resource",parts[1].trim());
		header.setAttribute("version",parts[2].trim().toLowerCase());
		
		line = readLine(iStream);
		while(line!=null && line.length()!=0){
			KeyValuePair kvp = KeyValuePair.parseKeyAndValue(line, ":");
			header.addChild( kvp.getKey() ).setValue( kvp.getValue() );
			
			line = readLine(iStream);
		}
		
		if(header.getAttribute("method").equals("post")) {
			int length = Integer.parseInt(header.getChild("Content-Length").getValue().trim());
			TreeEntity entity = header.addChild("post-data");
			
			int in = iStream.read();
			int index = 0;
			byte[] raw = new byte[length];
			while(in!=-1 && index<length)
			{
				raw[index]=(byte)in;
				index++;
				if(index==length) {
					break;
				}
				in = iStream.read();
			}
			entity.setValue( new String(raw) );
		}
		
		return header;
	}
	
	private String readLine(InputStream iStream) throws IOException {
		int in = iStream.read();
		StringBuilder line = new StringBuilder();
		while(in!=-1) {
			if(((char)in)=='\n') {
				break;
			}
			line.append((char)in);
			in = iStream.read();
		}
		return line.toString().trim();
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
		
		data.append( "File not found." );
		FileHelper.saveStringToOutputStream( data.toString(), socket.getOutputStream() );
	}
	
	private void send500(Socket socket,Throwable t) throws IOException {
		StringBuilder data = new StringBuilder();
		data.append("HTTP/1.1 500 Server Error\n");
		data.append("Server: Simple Sevice Server\n\n");
		
		data.append( "Server Error "+t.getMessage() );
		FileHelper.saveStringToOutputStream( data.toString(), socket.getOutputStream() );
		t.printStackTrace();
	}
	
	private void sendResource(EntityTree header,Socket socket) throws IOException {
		String request = header.getAttribute("resource");
		File root = new File(pageFile).getParentFile();
		File resource = new File(root.getAbsolutePath()+"/"+request);
		if(resource.exists()==false) {
			send404(header, socket);
		}
		String mimeType = Files.probeContentType(resource.toPath());
		if(mimeType==null) {
			if(resource.getName().indexOf(".")!=-1 && resource.getName().substring(resource.getName().lastIndexOf(".")+1).toLowerCase().equals("js")) {
				mimeType="application/javascript";
			}
		}
		
		StringBuilder data = new StringBuilder();
		data.append("HTTP/1.1 200 OK\n");
		data.append("Server: Simple Sevice Server\n");
		data.append("Connection: close\n");
		data.append("Content-Length: "+resource.length()+"\n");
		data.append("Content-Type: "+mimeType+"\n\n");
		
		socket.getOutputStream().write(data.toString().getBytes());;
		
		//copy file to out
		FileInputStream in = new FileInputStream( resource );
		
		try{
			byte[] buffer = new byte[1024];
			int len;
			while ((len = in.read(buffer)) != -1) {
				socket.getOutputStream().write(buffer, 0, len);
			}
		} finally {
			in.close();
		}
		
		socket.close();
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
		
		String method = header.getAttribute("method").toLowerCase();
		if(method.equals("post")) {
			String postData = header.getChild("post-data").getValue();
			String[] postParameters = postData.split("&");
			for(String parameter: postParameters) {
				parts = parameter.split("=");
				try {
					parameters.addChild(parts[0]).setValue( URLDecoder.decode(parts[1],"UTF-8") );
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					parameters.addChild(parts[0]).setValue(parts[1]);
				}
			}
		}
		
		String serviceName = parameters.getChild("action").getValue();
		String serviceMethod = StringHelper.after(serviceName, ".");
		serviceName = StringHelper.before(serviceName, ".");
		String[] serviceValues = new String[]{};
		if(parameters.getChild("values")!=null) {
			serviceValues = parameters.getChild("values").getValue().split(",");
		}
		String[] newValues = new String[serviceValues.length];
		for(int i=0;i<serviceValues.length;i++) {
			String value = serviceValues[i];
			if(value.startsWith("$")) {
				newValues[i] = parameters.getChild(value.substring(1)).getValue();
			} else {
				newValues[i] = value;
			}
		}
		serviceValues = newValues;
		
		try {
			Object response = callServiceMethod(serviceName, serviceMethod, serviceValues, header, socket);
			String responseData = null;
			String mimeType = null;
			if(response instanceof String) {
				responseData = (String) response;
				mimeType="text/plain";
			} else {
				responseData = "response: "+JSONHelper.toJSON(response);
				mimeType="application/json";
			}
			
			StringBuilder responseHeader = new StringBuilder();
			responseHeader.append("HTTP/1.1 200 OK\n");
			responseHeader.append("Server: Simple Sevice Server\n");
			responseHeader.append("Connection: close\n");
			responseHeader.append("Content-Length: "+responseData.length()+"\n");
			responseHeader.append("Content-Type: "+mimeType+"\n\n");
			responseHeader.append(responseData);
			
			FileHelper.saveStringToOutputStream(responseHeader.toString(),socket.getOutputStream());
			socket.close();
			
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
	
	public interface Authenticator {
		public boolean canAccess(EntityTree header,Socket socket);
		public String whoIs(EntityTree header);
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
		
		if(service instanceof NativeObject) {
			return callJSServiceMethod((NativeObject)service, serviceMethod, serviceValues, header, socket);
		} else if(serviceValues.length==0) {
			return service.getClass().getMethod(serviceMethod,null).invoke(service);
		} else if(serviceValues.length==1) {
			return service.getClass().getMethod(serviceMethod,String.class).invoke(service,serviceValues[0]);
		} else {
			return service.getClass().getMethod(serviceMethod,String[].class).invoke(service,serviceValues);
		}
	}
	
	@SuppressWarnings("restriction")
	private Object callJSServiceMethod(NativeObject service,String serviceMethod,String[] serviceValues, EntityTree header,Socket socket) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Object response = null;
		if(serviceValues.length==0) {
			response = NativeObject.callMethod((Scriptable)service, serviceMethod, new Object[]{});
		} else if(serviceValues.length==1) {
			response = NativeObject.callMethod((Scriptable)service, serviceMethod, new Object[]{serviceValues[0]});
		} else {
			response = NativeObject.callMethod((Scriptable)service, serviceMethod, serviceValues);
		}
		
		if(response instanceof NativeJavaObject) {
			return ((NativeJavaObject)response).unwrap();
		} else {
			return response;
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
	
	public Authenticator getAuthenticator() {
		return authenticator;
	}
	
	public void setAuthenticator(Authenticator authenticator) throws Exception {
		if(authenticator != null) {
			throw new Exception("Authenticator already set.");
		}
		this.authenticator = authenticator;
	}
}
