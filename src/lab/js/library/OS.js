OS = {
	openWebApp: function(webApp) {
		js.mount("system","org.one.stone.soup.core.process.ProcessWatch");
		system.execute("cmd.exe /c start http://"+webApp.getAddress()+":"+webApp.getPort());
	}
}