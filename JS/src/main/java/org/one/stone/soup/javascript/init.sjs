out.println("SJS Version 0.0.2 alpha");
out.println("Initialising Library.");

js.mount("ClipBoard","org.one.stone.soup.core.ClipBoard");
js.mount("StringHelper","org.one.stone.soup.core.StringHelper");
js.mount("DirectoryHelper","org.one.stone.soup.core.DirectoryHelper");
js.mount("FileHelper","org.one.stone.soup.core.FileHelper");
js.mount("ImageHelper","org.one.stone.soup.core.ImageHelper");
js.mount("JSONHelper","org.one.stone.soup.core.javascript.JSONHelper");
js.mount("RegExBuilder","org.one.stone.soup.core.RegExBuilder");
js.mount("ZipHelper","org.one.stone.soup.core.ZipHelper");
js.mount("CSVHelper","org.one.stone.soup.core.data.CSVHelper");
js.mount("XmlHelper","org.one.stone.soup.core.data.XmlHelper");
js.mount("OS","org.one.stone.soup.javascript.helper.OperatingSystem");
js.mount("Web","org.one.stone.soup.javascript.helper.RemoteWebServiceAccess");
js.mount("FileChooser","org.one.stone.soup.javascript.helper.FileChooser");
js.mount("Popup","org.one.stone.soup.javascript.helper.Popup");
TimeTrigger = org.one.stone.soup.javascript.trigger.TimeTrigger.getInstance();;

Application = new function() {
	this.start = function(name) {
		out.println("here");
		var application = null;
		var config = eval( ""+FileHelper.loadFileAsString(Drive.getFileToRead(name+"/"+name+"-configuration.json")) );
		//Load apps it depends on if not already loaded
		if(config.dependencies) {
			for(i in config.dependencies) {
				dependency = config.dependencies[i];
				if(!eval(dependency)) {
					this.start( dependency );
				}
			}
		}
		if(config.initScript) {
			application = js.runScript( Drive.getFileToRead(name+"/"+config.initScript).getAbsolutePath() );
		} else {
			application = js.runScript( Drive.getFileToRead(name+"/"+name+"-initialise.sjs").getAbsolutePath() );
		}
	
		js.mountObject(name,application);
	}
}
//js.mountObject("Application",Application);

function help(obj) {
	if(typeof(obj)=="undefined") {
		js.help();
		out.println("  Math");
		out.println("  String");
	} else {
		if(obj==Math) {
			out.println("Math TODO!");
		} else if(obj==String) {
			out.println("String TODO!");
		} else if(obj==Number) {
			out.println("Number TODO!");
		} else if( typeof(obj)=='object' ) {
			if(obj.getClass) {
				js.help(obj);
			} else {
				for(var i in obj) { out.println("  "+obj[i]); };
			}
		} else if( typeof(obj)=='function' ) {
			out.println(""+obj);
		}
	}
}
js.runScript("library/Authentication/Authentication.sjs");

out.println("Library Initialised.");
help();

if(js.isHeadless()==true) {
	out.println("Please login using Authentication.login(userName,password);");
} else {
	out.println("Please log in");
	userName = Popup.requestInput("Please enter your user name");
	if(userName) {
		password = Popup.requestInput("Please enter your password");
	} else {
		userName="guest";
		password="password";
	}
	Authentication.login(userName,password);
}
out.println("Library Initialised.");
out.println("Ready");