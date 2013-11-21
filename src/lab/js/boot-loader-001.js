js.runScript("src/lab/js/library/OS.js");

js.runScript("src/lab/js/library/WebApp.js");
webApp = new WebApp("bootLoader",OS.getLocalAddress(),9000,"src/lab/resources/sdsbuilder/boot-loader.html");

js.runScript("src/lab/js/library/Drive.js");
devDrive = new Drive("src/lab/js/drive/Development","devDriveService");

js.mount("mq","org.one.stone.soup.core.container.TransientMessageQueue");
mq.postMessage("mq started");

<<<<<<< HEAD
//webApp = new WebApp("bootLoader",OS.getLocalAddress(),9000,"src/lab/resources/sdsbuilder/boot-loader.html");
//webApp = new WebApp("bootLoader","localhost",9000,"src/lab/resources/sdsbuilder/boot-loader.html");
=======
webApp = new WebApp("bootLoader",OS.getLocalAddress(),9000,"src/lab/js/sds/boot-loader.html");
>>>>>>> refs/remotes/origin/master

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

OS.openWebApp(webApp);
