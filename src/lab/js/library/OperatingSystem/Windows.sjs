OS = new function() {
	this.openWebApp = function(webApp) {
		js.mount("system","org.one.stone.soup.core.process.ProcessWatch");
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