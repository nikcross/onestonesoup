js.runScript("src/lab/js/library/WebServer.js");
js.runScript("src/lab/js/library/OS.js");

webApp = new WebApp("bootLoader","localhost",9000,"src/lab/resources/sds/boot-loader.html");

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

webApp.start();

OS.openWebApp(webApp);