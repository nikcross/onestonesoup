out.println("SJS Version 0.0.1 alpha");

js.mount("ClipBoard","org.one.stone.soup.core.ClipBoard");
js.mount("StringHelper","org.one.stone.soup.core.StringHelper");
js.mount("DirectoryHelper","org.one.stone.soup.core.DirectoryHelper");
js.mount("FileHelper","org.one.stone.soup.core.FileHelper");
js.mount("ImageHelper","org.one.stone.soup.core.ImageHelper");
js.mount("JSONHelper","org.one.stone.soup.core.JSONHelper");
js.mount("RegExBuilder","org.one.stone.soup.core.RegExBuilder");
js.mount("ZipHelper","org.one.stone.soup.core.ZipHelper");
js.mount("CSVHelper","org.one.stone.soup.core.data.CSVHelper");
js.mount("XmlHelper","org.one.stone.soup.core.data.XmlHelper");
js.mount("OperatingSystem","org.one.stone.soup.javascript.helper.OperatingSystem");
js.mount("RemoteWebServiceAccess","org.one.stone.soup.javascript.helper.RemoteWebServiceAccess");
js.mount("TimeTrigger","org.one.stone.soup.javascript.helper.TimeTrigger");

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
				for(var i in obj) { println("  "+obj[i]); };
			}
		} else if( typeof(obj)=='function' ) {
			out.println(""+obj);
		}
	}
}