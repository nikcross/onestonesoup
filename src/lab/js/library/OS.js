OS = new function(){
	js.mount("system","org.one.stone.soup.core.process.ProcessWatch");
	this.openWebApp = function(webApp) {
		system.execute("cmd.exe /c start http://"+webApp.getAddress()+":"+webApp.getPort());
	}
	this.getLocalAddress = function() {
		return java.net.InetAddress.getLocalHost().getHostAddress();
	}
	this.getRoots = function() {
		roots = java.io.FileSystem.getFileSystem().listRoots();
		//TODO
	}
}