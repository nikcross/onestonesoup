try{
out.println("Boot Loader Started");
out.println("Loading Objects");
js.runScript("src/lab/js/library/OS.js");
js.runScript("src/lab/js/library/WebApp.js");
js.runScript("src/lab/js/library/Drive.js");

devDrive = new Drive("src/lab/js/drive/Development");
testDrive = new Drive("src/lab/js/drive/Test");
drawDrive = new Drive("src/lab/js/drive/Draw/user");

js.mount("mq","org.one.stone.soup.core.container.TransientMessageQueue");
mq.postMessage("mq started");

//webApp = new WebApp("bootLoader",OS.getLocalAddress(),8888,"src/lab/js/sds/boot-loader.html");
webApp = new WebApp("bootLoader","localhost",8888,"src/lab/js/sds/boot-loader.html");
adminDrive = new Drive("src/lab/js/drive/Test/Admin");
webApp.setAuthentication(adminDrive,"users.json");

devDrive.createWebService(webApp,"devDriveService");
testDrive.createWebService(webApp,"testDriveService");
drawDrive.createWebService(webApp,"drawDriveService");

webApp.createUserService(
		"testService1",function() {
			var count = 1;
			this.getTheMagic = function() {
				count++;
				return "Magic1 321321"+count;
			}
		}
);

webApp.createGlobalService(
		"testService2",function() {
			var count = 1;
			this.getTheMagic = function() {
				count++;
				return "Magic1 321321"+count;
			}
		}
);

webApp.createGlobalService(
		"js",function() {
			this.run = function(script) {
				return eval(script);
			}
		}
);

webApp.createGlobalService(
		"mqS",function() {
			this.sendMessage = function(message) {
				mq.postMessage(message);
			}
			this.getMessages = function(time) {
				return mq.getMessagesSince(time);
			}
		}
);

webApp.start();

//OS.openWebApp(webApp);

} catch(e) {
	out.println( e+" on line "+e.lineNumber );
	js.exit();
}
